/*******************************************************************************
 *
 *  Copyright 2016 Net Wolf UK
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  
 *******************************************************************************/
package debrepo.teamcity.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.DebRepositoryConfigurations;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;

public abstract class DebRepositoryConfigurationManagerImpl implements DebRepositoryConfigurationManager, DebRepositoryManager {
	
	Map<String, DebRepositoryConfiguration> repositoryMetaData = new TreeMap<String, DebRepositoryConfiguration>();
	private final DebRepositoryConfigurationFactory myDebRepositoryConfigurationFactory;
	private final DebRepositoryConfigurationChangePersister myDebRepositoryConfigurationChangePersister;

	public DebRepositoryConfigurationManagerImpl(
			DebRepositoryConfigurationFactory debRepositoryConfigurationFactory, 
			DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister) {
		this.myDebRepositoryConfigurationFactory = debRepositoryConfigurationFactory;
		this.myDebRepositoryConfigurationChangePersister = debRepositoryConfigurationChangePersister;
	}

	@Override
	public void updateRepositoryConfigurations(DebRepositoryConfigurations repoConfigurations) {
		
		synchronized (repositoryMetaData) {
			
			/* Build a list of the existing entries.
			 * Any that don't exist in the new config will be deleted later.
			 */
			Map<String, Boolean> existingRepos = new TreeMap<>();
			for (String keyName : repositoryMetaData.keySet()) {
				existingRepos.put(keyName, false);
			}
			
			/* Apply the update to the config, flag repo as existing (add to existingRepos map)
			 * and initialise if a repo with that UUID didn't exist already.
			 */
			for (DebRepositoryConfiguration newConfig : repoConfigurations.getDebRepositoryConfigurations()) {
				repositoryMetaData.put(newConfig.getRepoName(), newConfig);
				existingRepos.put(newConfig.getRepoName(), true);
				if(!isExistingRepository(newConfig.getUuid())){
					initialisePackageStore(newConfig);
				}
			}
			
			/* Now remove old repos in the config map and their corresponding repos. */
			for (Entry<String,Boolean> existing  : existingRepos.entrySet()) {
				if (existing.getValue() == false) {
					Loggers.SERVER.info("DebRepositoryManagerImpl:updateRepositoryConfigurations :: Removing old repository '" + existing.getKey());
					removeRepository(repositoryMetaData.get(existing.getKey()).getUuid());
					repositoryMetaData.remove(existing.getKey());
				}
			}
			
		} /* End Syncronized block */
		
	}

	@Override
	public List<DebRepositoryConfiguration> getConfigurationsForProject(String projectId) {
		List<DebRepositoryConfiguration> configs = new ArrayList<>();
		for (DebRepositoryConfiguration config : this.repositoryMetaData.values())
			if (projectId.equals(config.getProjectId())) {
				configs.add(config);
			}
		return configs;
	}

	@Override
	public Set<DebRepositoryConfiguration> findConfigurationsForBuildType(String buildTypeId) {
		Set<DebRepositoryConfiguration> configs = new TreeSet<>();
		for (DebRepositoryConfiguration config : this.repositoryMetaData.values()) {
			for (DebRepositoryBuildTypeConfig buildType : config.getBuildTypes()) {
				if (buildTypeId.equals(buildType.getBuildTypeId())){
					configs.add(config);
				}
			}
		}
		return configs;
	}

	@Override
	public Set<DebRepositoryConfiguration> findConfigurationsForDebRepositoryEntity(DebPackage debPackage) {
		// iterate of the list of configs and check the filters match.
		Set<DebRepositoryConfiguration> configSet = new TreeSet<>();
		for (DebRepositoryConfiguration config : repositoryMetaData.values()) {
			for (DebRepositoryBuildTypeConfig bt : config.getBuildTypes()) {
				if (debPackage.getBuildTypeId().equals(bt.getBuildTypeId())){
					for (Filter filter : bt.getDebFilters()) {
						if (filter.matches(debPackage.getFilename())
							&& debPackage.getComponent().equals(filter.getComponent())
							&& debPackage.getDist().equals(filter.getDist())) 
						{
								configSet.add(config);
						}
					}
				}
			}
		}
		return configSet;
	}



	@Override
	public DebRepositoryConfiguration getDebRepositoryConfiguration(String debRepoUuid) {
		for (DebRepositoryConfiguration config : this.repositoryMetaData.values()) {
			if (debRepoUuid.equals(config.getUuid().toString())) {
				return myDebRepositoryConfigurationFactory.copyDebRepositoryConfiguration(config);
			}
		}
		return null;
	}
	
	@Override
	public DebRepositoryConfiguration getDebRepositoryConfigurationByName(String debRepoName) {
		if (this.repositoryMetaData.containsKey(debRepoName)) {
			return this.repositoryMetaData.get(debRepoName);
		}
		return null;
	}

	@Override
	public DebRepositoryActionResult addDebRepository(DebRepositoryConfiguration debRepositoryConfiguration) {
		if (this.repositoryMetaData.containsKey(debRepositoryConfiguration.getRepoName())) {
			return new DebRepositoryActionResult("Repository with that name already exists.", true, debRepositoryConfiguration, null);
		}
		
		repositoryMetaData.put(debRepositoryConfiguration.getRepoName(), debRepositoryConfiguration);
		if(!isExistingRepository(debRepositoryConfiguration.getUuid())) {
			initialisePackageStore(debRepositoryConfiguration);
		}
		try {
			this.myDebRepositoryConfigurationChangePersister.writeDebRespositoryConfigurationChanges(getRepositoryConfigurations());
			return new DebRepositoryActionResult("Added new repository", false, null, debRepositoryConfiguration);
		} catch (JAXBException e) {
			Loggers.SERVER.error("DebRepositoryManagerImpl:addDebRepository :: Failed to add new repository '" + debRepositoryConfiguration.getRepoName() + "(" + debRepositoryConfiguration.getUuid() + ")");
			Loggers.SERVER.debug(e);
			return new DebRepositoryActionResult("Failed to add new repository", true, null, debRepositoryConfiguration);
		}
	}

	private DebRepositoryConfigurations getRepositoryConfigurations() {
		DebRepositoryConfigurations configs = new DebRepositoryConfigurations();
		for (DebRepositoryConfiguration config : repositoryMetaData.values()){
			configs.add((DebRepositoryConfigurationJaxImpl) myDebRepositoryConfigurationFactory.copyDebRepositoryConfiguration(config));
		}
		return configs;
	}

	@Override
	public DebRepositoryActionResult removeDebRespository(DebRepositoryConfiguration debRepositoryConfiguration) {
		if (isExistingRepository(debRepositoryConfiguration.getUuid())) {
			for (Entry<String,DebRepositoryConfiguration> config : repositoryMetaData.entrySet()) {
				if (debRepositoryConfiguration.getUuid().toString().equals(config.getValue().getUuid().toString())){
					Loggers.SERVER.info("DebRepositoryManagerImpl:removeDebRespository :: Removing old repository '" + debRepositoryConfiguration.getRepoName() + "(" + debRepositoryConfiguration.getUuid() + ")");
					removeRepository(config.getValue().getUuid());
					repositoryMetaData.remove(config.getKey());
					try {
						this.myDebRepositoryConfigurationChangePersister.writeDebRespositoryConfigurationChanges(getRepositoryConfigurations());
						return new DebRepositoryActionResult("Respository removed", false, config.getValue(), null);
					} catch (JAXBException e) {
						Loggers.SERVER.error("DebRepositoryManagerImpl:removeDebRespository :: Failed to remove repository '" + debRepositoryConfiguration.getRepoName() + "(" + debRepositoryConfiguration.getUuid() + ")");
						Loggers.SERVER.debug(e);
						return new DebRepositoryActionResult("Failed to persist repository removal. Repository may re-appear after TeamCity restart", true, null, debRepositoryConfiguration);
					}
				}
			}
		}
		return new DebRepositoryActionResult("Respository not found", true, debRepositoryConfiguration, debRepositoryConfiguration);
	}

	@Override
	public DebRepositoryActionResult editDebRepositoryConfiguration(DebRepositoryConfiguration debRepositoryConfiguration) {
		try {
			final DebRepositoryConfiguration existingRepo = findRepoConfigWithUuid(debRepositoryConfiguration.getUuid());
			
			// The UUID and the name match an existing repo, just update it.
			if (existingRepo.getUuid().equals(debRepositoryConfiguration.getUuid()) && existingRepo.getRepoName().equals(debRepositoryConfiguration.getRepoName())){
				this.repositoryMetaData.put(debRepositoryConfiguration.getRepoName(), debRepositoryConfiguration);
				this.myDebRepositoryConfigurationChangePersister.writeDebRespositoryConfigurationChanges(getRepositoryConfigurations());
				return new DebRepositoryActionResult("Repository config updated", false, existingRepo, debRepositoryConfiguration);
			} else if (this.repositoryMetaData.containsKey(debRepositoryConfiguration.getRepoName())) {
				// We already have a repo with that name.
				return new DebRepositoryActionResult("Repository of that name already exists", true, debRepositoryConfiguration, debRepositoryConfiguration);
			} else {
				// Looks like the repo has changed names, and the name does not conflict.
				// Remove the old one and add the new one.
				this.repositoryMetaData.remove(existingRepo.getRepoName());
				this.repositoryMetaData.put(debRepositoryConfiguration.getRepoName(), debRepositoryConfiguration);
				this.myDebRepositoryConfigurationChangePersister.writeDebRespositoryConfigurationChanges(getRepositoryConfigurations());
				return new DebRepositoryActionResult("Repository config updated", false, existingRepo, debRepositoryConfiguration);
			}
		} catch (NonExistantRepositoryException nere) {
			return new DebRepositoryActionResult("Cannot update non-existant repository", true, debRepositoryConfiguration, debRepositoryConfiguration);
		} catch (JAXBException jaxbe) {
			Loggers.SERVER.error("DebRepositoryManagerImpl:editDebRepositoryConfiguration :: Failed to persist repository changes for '" + debRepositoryConfiguration.getRepoName() + "(" + debRepositoryConfiguration.getUuid() + ")");
			Loggers.SERVER.debug(jaxbe);
			return new DebRepositoryActionResult("Failed to persist repository change. Repository might revert after TeamCity restart", true, null, debRepositoryConfiguration);
		}
	}

	private DebRepositoryConfiguration findRepoConfigWithUuid(UUID uuid) throws NonExistantRepositoryException {
		for (DebRepositoryConfiguration config : repositoryMetaData.values()){
			if(uuid.equals(config.getUuid())){
				if (isExistingRepository(config.getUuid())){
					return config;
				} else {
					throw new NonExistantRepositoryException();
				}
			}
		}
		throw new NonExistantRepositoryException();
	}

	@Override
	public List<DebRepositoryConfiguration> getAllConfigurations() {
		List<DebRepositoryConfiguration> copiedConfigs = new ArrayList<>();
		for (DebRepositoryConfiguration config : repositoryMetaData.values()){
			copiedConfigs.add(myDebRepositoryConfigurationFactory.copyDebRepositoryConfiguration(config));
		}
		Collections.sort(copiedConfigs, new DebRepositoryConfigurationAlphabeticComparator());
		return copiedConfigs;
	}
	
	private static class DebRepositoryConfigurationAlphabeticComparator implements Comparator<DebRepositoryConfiguration> {
		@Override
		public int compare(DebRepositoryConfiguration o1, DebRepositoryConfiguration o2) {
			return o1.getRepoName().compareToIgnoreCase(o2.getRepoName());
		}
	}

}
