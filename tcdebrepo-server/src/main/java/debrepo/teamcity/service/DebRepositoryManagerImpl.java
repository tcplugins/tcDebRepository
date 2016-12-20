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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.springframework.stereotype.Service;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageEntityKey;
import debrepo.teamcity.entity.DebPackageNotFoundInStoreException;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryStatistics;
import debrepo.teamcity.entity.helper.XmlPersister;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

@Service
public class DebRepositoryManagerImpl extends  DebRepositoryConfigurationManagerImpl implements DebRepositoryManager, DebRepositoryConfigurationManager {
	
	Map<UUID, DebPackageStore> repositories = new TreeMap<UUID, DebPackageStore>();
	
	private final ProjectManager myProjectManager;
	private final XmlPersister<DebPackageStore, DebRepositoryConfiguration> myDebRepositoryDatabaseXmlPersister;
	
	public DebRepositoryManagerImpl(ProjectManager projectManager, 
									XmlPersister<DebPackageStore, DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister,
									DebRepositoryConfigurationFactory debRepositoryConfigurationFactory, 
									DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister) {
		super(debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		this.myProjectManager = projectManager;
		this.myDebRepositoryDatabaseXmlPersister = debRepositoryDatabaseXmlPersister;

	}
	
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
	public DebRepositoryStatistics getRepositoryStatistics(DebRepositoryConfiguration projectConfig, String repoURL) {
		return getRepositoryStatistics(projectConfig.getUuid().toString(), repoURL);
	}
	
	@Override
	public DebRepositoryStatistics getRepositoryStatistics(String uuid, String repoURL) {
		DebRepositoryConfiguration config = getDebRepositoryConfiguration(uuid);
		int filterCount = 0;
		for (DebRepositoryBuildTypeConfig btConfig : config.getBuildTypes()) {
			filterCount = filterCount + btConfig.getDebFilters().size();
		}
		return new DebRepositoryStatistics(repositories.get(UUID.fromString(uuid)).size(), repoURL, filterCount);
	}
	
	@Override
	public void addBuildPackage(DebRepositoryConfiguration config, DebPackage debPackage) {
		DebPackageEntity debPackageEntity = DebPackageEntity.copy(debPackage);
		this.repositories.get(config.getUuid()).put(debPackageEntity.buildKey(), debPackageEntity);
		this.persist(config.getUuid());
	}
	
	@Override
	public Set<String> findUniqueArchByDistAndComponent(String repoName, String distName, String component)	throws NonExistantRepositoryException {
		DebPackageStore store = getPackageStore(repoName);
		Set<String> architectures = new TreeSet<>();
		for (DebPackageEntityKey e : store.keySet()) {
			if (distName.equals(e.getDist()) && component.equals(e.getComponent())) {
				architectures.add(e.getArch());
				if ("all".equalsIgnoreCase(e.getArch())){
					architectures.addAll(getDebRepositoryConfigurationByName(repoName).getArchitecturesRepresentedByAll());
				}
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
	public List<DebPackage> getUniquePackagesByComponentAndPackageName(String repoName, String component, String packageName) throws NonExistantRepositoryException {
		DebPackageStore store = getPackageStore(repoName);
		Map<String, DebPackageEntity> uniquePackages = new HashMap<>();
		for (DebPackageEntity deb : store.findAllForPackageNameAndComponent(packageName, component)) {
			uniquePackages.put(deb.getFilename(), deb);
		}
		return new ArrayList<DebPackage>(uniquePackages.values());
	}

	@Override
	public List<DebPackage> findAllByDistComponentArch(String repoName, String distName, String component, String archName) throws NonExistantRepositoryException {
		return getPackageStore(repoName).findAllByDistComponentArch(distName, component, archName, false);
	}
	
	@Override
	public List<DebPackage> findAllByDistComponentArchIncludingAll(String repoName, String distName, String component, String archName) throws NonExistantRepositoryException {
		return getPackageStore(repoName).findAllByDistComponentArch(distName, component, archName, true);
	}

	@Override
	public DebPackage findByUri(String repoName, String uri) throws NonExistantRepositoryException, DebPackageNotFoundInStoreException {
		return getPackageStore(repoName).findByUri(uri);
	}

	@Override
	public boolean isExistingRepository(String repoName) {
		try {
			getPackageStore(repoName);
			return true;
		} catch (NonExistantRepositoryException e) {
			return false;
		}
	}
	
	@Override
	public boolean isExistingRepository(UUID uuid) {
		return repositories.containsKey(uuid);
	}
	
	@Override
	public void removeRepository(UUID uuid) {
		repositories.put(uuid, null);
		repositories.remove(uuid);
	}
	
	@Override
	public void addBuildPackages(DebRepositoryConfiguration debRepositoryConfiguration, List<DebPackage> newPackages) throws NonExistantRepositoryException {
		DebPackageStore store = getPackageStore(debRepositoryConfiguration.getRepoName());
		for (DebPackage debPackage : newPackages) {
			DebPackageEntity debPackageEntity = DebPackageEntity.copy(debPackage);
			store.put(debPackageEntity.buildKey(), debPackageEntity);
		}
		persist(store.getUuid());
	}

	@Override
	public void removeBuildPackages(DebPackageRemovalBean packageRemovalBean) {
		try {
			DebPackageStore store = getPackageStore(packageRemovalBean.getDebRepositoryConfiguration().getRepoName());
			boolean updated = store.removePackagesForBuild(packageRemovalBean.getBuildId(), packageRemovalBean.getPackagesToKeep());
			if (updated) {
				persist(store.getUuid());
			}
		} catch (NonExistantRepositoryException e) {
			// If the repo does not exist, don't worry about removing stuff.
		}
	}
	
}
