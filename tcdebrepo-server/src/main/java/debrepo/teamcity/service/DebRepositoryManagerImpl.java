/*******************************************************************************
 * Copyright 2016 Net Wolf UK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package debrepo.teamcity.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.springframework.stereotype.Service;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageEntityKey;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.DebRepositoryConfigurations;
import debrepo.teamcity.entity.DebRepositoryStatistics;
import debrepo.teamcity.entity.helper.XmlPersister;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

@Service
public class DebRepositoryManagerImpl implements DebRepositoryManager, DebRepositoryConfigurationManager  {
	
	Map<String, DebRepositoryConfiguration> repositoryMetaData = new TreeMap<String, DebRepositoryConfiguration>();
	Map<UUID, DebPackageStore> repositories = new TreeMap<UUID, DebPackageStore>();
	
	private final ProjectManager myProjectManager;
	private final XmlPersister<DebPackageStore, DebRepositoryConfiguration> myDebRepositoryDatabaseXmlPersister;
	private final DebRepositoryConfigurationFactory myDebRepositoryConfigurationFactory;
	private final DebRepositoryConfigurationChangePersister myDebRepositoryConfigurationChangePersister;
	
	public DebRepositoryManagerImpl(ProjectManager projectManager, 
									XmlPersister<DebPackageStore, DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister, 
									DebRepositoryConfigurationFactory debRepositoryConfigurationFactory, 
									DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister) {
		this.myProjectManager = projectManager;
		this.myDebRepositoryDatabaseXmlPersister = debRepositoryDatabaseXmlPersister;
		this.myDebRepositoryConfigurationFactory = debRepositoryConfigurationFactory;
		this.myDebRepositoryConfigurationChangePersister = debRepositoryConfigurationChangePersister;
	}
	
	@Override
	public DebPackageStore getPackageStore(String storeName) throws NonExistantRepositoryException {
		
		if (!repositoryMetaData.containsKey(storeName)) {
			throw new NonExistantRepositoryException();
		}
		return repositories.get(repositoryMetaData.get(storeName).getUuid());
	}
	

	@Override
	public boolean persist(UUID uuid) {
		try {
			return this.myDebRepositoryDatabaseXmlPersister.persistToXml(repositories.get(uuid));
		} catch (IOException e) {
			Loggers.SERVER.warn("DebRepositoryManagerImpl :: Failed to persist store to disk. UUID: " + uuid.toString());
			if (Loggers.SERVER.isDebugEnabled()) { Loggers.SERVER.debug(e); }
			return false;
		}
	}
	
	
	@Override
	public DebPackageStore initialisePackageStore(DebRepositoryConfiguration conf) {
		
		DebPackageStore newStore; 
		try {
			newStore = this.myDebRepositoryDatabaseXmlPersister.loadfromXml(conf);
		} catch (IOException ex) {
			Loggers.SERVER.info("DebRepositoryManagerImpl :: Failed to load existing repository from XML '" + conf.getRepoName() + "' for project '" + conf.getProjectId() + "'");
			if (Loggers.SERVER.isDebugEnabled()) {Loggers.SERVER.debug(ex);}
			newStore = new DebPackageStore();
			newStore.setUuid(conf.getUuid());
		}
		repositoryMetaData.put(conf.getRepoName(), conf);
		repositories.put(conf.getUuid(), newStore);
		Loggers.SERVER.info("DebRepositoryManagerImpl :: Initialised debrepo named '" + conf.getRepoName() + "' for project '" + conf.getProjectId() + "'");
		return repositories.get(repositoryMetaData.get(conf.getRepoName()).getUuid());
	}
	
	@Override
	public List<DebPackageStore> getPackageStoresForBuildType(String buildTypeid) throws NonExistantRepositoryException {
		SBuildType sBuildType = myProjectManager.findBuildTypeById(buildTypeid);
		List<SProject> projectPathList = this.myProjectManager.findProjectById(sBuildType.getProjectId()).getProjectPath();
		List<DebPackageStore> stores = new ArrayList<>();
		//Collections.reverse(projectPathList);
		for (SProject sProject : projectPathList) {
			for (DebRepositoryConfiguration meta : repositoryMetaData.values()){
				if (meta.getProjectId().equals(sProject.getProjectId()) && meta.containsBuildType(buildTypeid)){
					if (repositories.containsKey(meta.getUuid())){
						stores.add(repositories.get(meta.getUuid()));
					} else {
						throw new NonExistantRepositoryException();
					}
				}
			}
		}
		return stores;
	}
	
/*	@Override
	public List<DebPackageStore> getPackageStoresForDebPackage(DebPackageEntity entity) throws NonExistantRepositoryException {
		SBuildType sBuildType = myProjectManager.findBuildTypeById(entity.getSBuildTypeId());
		List<SProject> projectPathList = this.myProjectManager.findProjectById(sBuildType.getProjectId()).getProjectPath();
		List<DebPackageStore> stores = new ArrayList<>();
		//Collections.reverse(projectPathList);
		for (SProject sProject : projectPathList) {
			for (DebRepositoryConfiguration meta : repositoryMetaData.values()){
				if (meta.getProjectId().equals(sProject.getProjectId()) 
						&& meta.containsBuildTypeAndFilter(entity) ){
					if (repositories.containsKey(meta.getUuid())){
						stores.add(repositories.get(meta.getUuid()));
					} else {
						throw new NonExistantRepositoryException();
					}
				}
			}
		}
		return stores;
	}*/
	
	@Override
	public DebPackageStore getPackageStoreForProject(String projectId) throws NonExistantRepositoryException {
		for (DebRepositoryConfiguration meta : repositoryMetaData.values()){
			if (meta.getProjectId().equals(projectId)){
				if (repositories.containsKey(meta.getUuid())){
					return repositories.get(meta.getUuid());
				} else {
					throw new NonExistantRepositoryException();
				}
			}			
		}
		return null;
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
				if(!repositories.containsKey(newConfig.getUuid())){
					initialisePackageStore(newConfig);
				}
			}
			
			/* Now remove old repos in the config map and their corresponding repos. */
			for (Entry<String,Boolean> existing  : existingRepos.entrySet()) {
				if (existing.getValue() == false) {
					Loggers.SERVER.info("DebRepositoryManagerImpl:updateRepositoryConfigurations :: Removing old repository '" + existing.getKey());
					repositories.remove(repositoryMetaData.get(existing.getKey()).getUuid());
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
	public DebRepositoryStatistics getRepositoryStatistics(DebRepositoryConfiguration projectConfig, String repoURL) {
		return getRepositoryStatistics(projectConfig.getUuid().toString(), repoURL);
	}
	
	@Override
	public DebRepositoryStatistics getRepositoryStatistics(String uuid, String repoURL) {
		return new DebRepositoryStatistics(repositories.get(UUID.fromString(uuid)).size(), repoURL);
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
	public void addBuildPackage(DebRepositoryConfiguration config, DebPackageEntity debPackageEntity) {
		this.repositories.get(config.getUuid()).put(debPackageEntity.buildKey(), debPackageEntity);
		this.persist(config.getUuid());
	}

	@Override
	public Set<DebRepositoryConfiguration> findConfigurationsForDebRepositoryEntity(DebPackageEntity debPackageEntity) {
		// iterate of the list of configs and check the filters match.
		Set<DebRepositoryConfiguration> configSet = new TreeSet<>();
		for (DebRepositoryConfiguration config : repositoryMetaData.values()) {
			for (DebRepositoryBuildTypeConfig bt : config.getBuildTypes()) {
				if (debPackageEntity.getSBuildTypeId().equals(bt.getBuildTypeId())){
					for (Filter filter : bt.getDebFilters()) {
						if (filter.matches(debPackageEntity.getFilename())
							&& debPackageEntity.getComponent().equals(filter.getComponent())
							&& debPackageEntity.getDist().equals(filter.getDist())) 
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
	public Set<String> findUniqueArchByDistAndComponent(String repoName, String distName, String component)	throws NonExistantRepositoryException {
		DebPackageStore store = getPackageStore(repoName);
		Set<String> architectures = new TreeSet<>();
		for (DebPackageEntityKey e : store.keySet()) {
			if (distName.equals(e.getDist()) && component.equals(e.getComponent())) {
				architectures.add(e.getArch());
			}
		}
		return architectures;
	}

	@Override
	public Set<String> findUniqueComponentByDist(String repoName, String distName) throws NonExistantRepositoryException {
		DebPackageStore store = getPackageStore(repoName);
		Set<String> components = new TreeSet<>();
		for (DebPackageEntityKey e : store.keySet()) {
			if (distName.equals(e.getDist())) {
				components.add(e.getComponent());
			}
		}
		return components;
	}
	
	@Override
	public Set<String> findUniqueDist(String repoName) throws NonExistantRepositoryException {
		DebPackageStore store = getPackageStore(repoName);
		Set<String> dists = new TreeSet<>();
		for (DebPackageEntityKey e : store.keySet()) {
			dists.add(e.getDist());
		}
		return dists;
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
		if(!repositories.containsKey(debRepositoryConfiguration.getUuid())) {
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
		if (repositories.containsKey(debRepositoryConfiguration.getUuid())) {
			for (Entry<String,DebRepositoryConfiguration> config : repositoryMetaData.entrySet()) {
				if (debRepositoryConfiguration.getUuid().toString().equals(config.getValue().getUuid().toString())){
					Loggers.SERVER.info("DebRepositoryManagerImpl:removeDebRespository :: Removing old repository '" + debRepositoryConfiguration.getRepoName() + "(" + debRepositoryConfiguration.getUuid() + ")");
					repositories.put(config.getValue().getUuid(), null);
					repositories.remove(config.getValue().getUuid());
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
				if (repositories.containsKey(config.getUuid())){
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

	@Override
	public Set<String> findUniqueComponent(String repoName) throws NonExistantRepositoryException {
		DebPackageStore store = getPackageStore(repoName);
		Set<String> components = new TreeSet<>();
		for (DebPackageEntityKey e : store.keySet()) {
			components.add(e.getComponent());
		}
		return components;
	}

	@Override
	public Set<String> findUniquePackageNameByComponent(String repoName, String component) throws NonExistantRepositoryException {
		DebPackageStore store = getPackageStore(repoName);
		Set<String> packageNames = new TreeSet<>();
		for (DebPackageEntityKey e : store.keySet()) {
			if (component.equals(e.getComponent())) {
				packageNames.add(e.getPackageName());
			}
		}
		return packageNames;
	}

	@Override
	public List<DebPackageEntity> getUniquePackagesByComponentAndPackageName(String repoName, String component, String packageName) throws NonExistantRepositoryException {
		DebPackageStore store = getPackageStore(repoName);
		Map<String, DebPackageEntity> uniquePackages = new HashMap<>();
		for (DebPackageEntity deb : store.findAllForPackageNameAndComponent(packageName, component)) {
			uniquePackages.put(deb.getFilename(), deb);
		}
		return new ArrayList<DebPackageEntity>(uniquePackages.values());
	}
	
}
