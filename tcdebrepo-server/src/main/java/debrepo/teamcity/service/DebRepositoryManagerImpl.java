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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.springframework.stereotype.Service;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.archive.DebFileReader;
import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurations;
import debrepo.teamcity.entity.DebRepositoryStatistics;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.entity.helper.XmlPersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

@Service
public class DebRepositoryManagerImpl implements DebRepositoryManager, DebRepositoryConfigurationManager  {
	
	Map<String, DebRepositoryConfiguration> repositoryMetaData = new TreeMap<String, DebRepositoryConfiguration>();
	Map<UUID, DebPackageStore> repositories = new TreeMap<UUID, DebPackageStore>();
	
	private final ProjectManager myProjectManager;
	private final XmlPersister<DebPackageStore, DebRepositoryConfiguration> myDebRepositoryDatabaseXmlPersister;
	
	public DebRepositoryManagerImpl(ProjectManager projectManager, XmlPersister<DebPackageStore, DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister) {
		this.myProjectManager = projectManager;
		this.myDebRepositoryDatabaseXmlPersister = debRepositoryDatabaseXmlPersister;
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
	
	@Override
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
	}
	
	/*
	@Override
	public boolean registerBuildWithPackageStore(String storeName, String sBuildTypeId) throws NonExistantRepositoryException {
		if (!repositoryMetaData.containsKey(storeName)){
			throw new NonExistantRepositoryException();
		}
		boolean success = repositoryMetaData.get(storeName).addBuildType(sBuildTypeId);
		if (success){
			Loggers.SERVER.info("DebRepositoryManagerImpl :: Registered buildType '" + sBuildTypeId + "' with store '" + storeName + "'");
		} else {
			Loggers.SERVER.info("DebRepositoryManagerImpl :: buildType '" + sBuildTypeId + "' was already registered with store '" + storeName + "'");
		}
		return success;
	}
	
	@Override
	public boolean registerBuildWithProjectPackageStore(String projectId, String sBuildTypeId) throws NonExistantRepositoryException {
		for (Map.Entry<String,DebRepositoryConfiguration> entry : repositoryMetaData.entrySet()){
			if (entry.getValue().getProjectId().equals(projectId)){
				return registerBuildWithPackageStore(entry.getKey(), sBuildTypeId);
			}
		}
		return false;
	} */

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
	public void addBuildPackages(String buildTypeId, List<DebPackageEntity> debPackageEntities, DebFileReader debFileReader) {
		
			for (DebPackageEntity entity : debPackageEntities){
				boolean entityAdded = false;
				for (DebRepositoryConfiguration config : repositoryMetaData.values()) {
					if (config.containsBuildTypeAndFilter(entity)) {
						if (!entity.isPopulated()) {
							try {
								entity.populateMetadata(debFileReader.getMetaDataFromPackage(entity.getFilename()));
							} catch (IOException e) {
								Loggers.SERVER.warn("DebRepositoryManagerImpl :: Failed to read data from package: " 
													+ entity.getFilename() + "  Package will not be added to respository " 
													+ config.getRepoName());
								if (Loggers.SERVER.isDebugEnabled()) { Loggers.SERVER.debug(e); }
							}
						}
						if (entity.isPopulated()) {
							try {
								getPackageStore(config.getRepoName()).put(entity.buildKey(), entity);
								entityAdded = true;
								persist(config.getUuid());
							} catch (NonExistantRepositoryException e) {
								Loggers.SERVER.warn("DebRepositoryManagerImpl :: Failed to add package: " 
										+ entity.getFilename() + "  Non-existant respository " 
										+ config.getRepoName());
								if (Loggers.SERVER.isDebugEnabled()) { Loggers.SERVER.debug(e); }
							}
						}
					} else {
						if (Loggers.SERVER.isDebugEnabled()) { Loggers.SERVER.debug("DebRepositoryManagerImpl :: Not adding package: " 
								+ entity.getFilename() + " to repo: " + config.getRepoName() + " (" + config.getProjectId() + ")"); }
					}
					if (Loggers.SERVER.isDebugEnabled()) {
						Loggers.SERVER.debug("DebRepositoryManagerImpl :: Statistics" + getRepositoryStatistics(config, ""));
					}
				}
				if (Loggers.SERVER.isDebugEnabled()) {
					if (! entityAdded) {
						Loggers.SERVER.debug("DebRepositoryManagerImpl :: No repo found for package: " + entity.getFilename());
					}
				}
			}
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
	
}
