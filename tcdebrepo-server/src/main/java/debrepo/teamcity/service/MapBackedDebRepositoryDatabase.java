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

import java.util.ArrayList;
import java.util.List;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageStore;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

public class MapBackedDebRepositoryDatabase implements DebRepositoryDatabase {
	
	private final DebRepositoryManager myDebRepositoryManager;
	private final ProjectManager myProjectManager;
	
	public MapBackedDebRepositoryDatabase(DebRepositoryManager debRepositoryManager, ProjectManager projectManager){
		this.myDebRepositoryManager = debRepositoryManager;
		this.myProjectManager = projectManager;
	}

	@Override
	public boolean addPackage(DebPackageEntity entity) {
		SBuildType sBuildType = myProjectManager.findBuildTypeById(entity.getSBuildTypeId());
		
		DebPackageStore store;
		try {
			store = this.myDebRepositoryManager.getPackageStoreForBuildType(sBuildType.getBuildTypeId());
		} catch (NonExistantRepositoryException e) {
			Loggers.SERVER.warn("MapBackedDebRepositoryPersistanceEngine: No repo found for project: " + sBuildType.getProjectId());
			Loggers.SERVER.debug(e);
			return false;
		}
		store.put(entity.buildKey(), entity);
		this.myDebRepositoryManager.persist(store.getUuid());
		return true;
	}

	@Override
	public boolean removePackage(DebPackageEntity entity) {
		SBuildType sBuildType = myProjectManager.findBuildTypeById(entity.getSBuildTypeId());
		
		DebPackageStore store;
		try {
			store = this.myDebRepositoryManager.getPackageStoreForBuildType(sBuildType.getBuildTypeId());
		} catch (NonExistantRepositoryException e) {
			Loggers.SERVER.warn("MapBackedDebRepositoryPersistanceEngine: No repo found for project: " + sBuildType.getProjectId());
			Loggers.SERVER.debug(e);
			return false;
		}
		boolean result = store.remove(entity.buildKey()) != null;
		this.myDebRepositoryManager.persist(store.getUuid());
		return result;  
	}

	@Override
	public List<DebPackageEntity> findPackageByName(String repoName, String packageName) {
		try {
			return this.myDebRepositoryManager.getPackageStore(repoName).findAllForPackageName(packageName);
		} catch (NonExistantRepositoryException e) {
			Loggers.SERVER.warn("MapBackedDebRepositoryPersistanceEngine: No repo found by name: " + repoName);
			return new ArrayList<>();
		}
	}

	@Override
	public List<DebPackageEntity> findPackageByNameAndVersion(String repoName, String packageName, String version) {
		try {
			return this.myDebRepositoryManager.getPackageStore(repoName).findAllForPackageNameAndVersion(packageName, version);
		} catch (NonExistantRepositoryException e) {
			Loggers.SERVER.warn("MapBackedDebRepositoryPersistanceEngine: No repo found by name: " + repoName);
			return new ArrayList<>();
		}
	}

	@Override
	public List<DebPackageEntity> findPackageByNameAndAchitecture(String repoName, String packageName, String arch) {
		try {
			return this.myDebRepositoryManager.getPackageStore(repoName).findAllForPackageNameAndArch(packageName, arch);
		} catch (NonExistantRepositoryException e) {
			Loggers.SERVER.warn("MapBackedDebRepositoryPersistanceEngine: No repo found by name: " + repoName);
			return new ArrayList<>();
		}
	}

	@Override
	public List<DebPackageEntity> findPackageByNameVersionAndArchitecture(String repoName, String packageName, String version, String arch) {
		try {
			return this.myDebRepositoryManager.getPackageStore(repoName).findAllForPackageNameVersionAndArch(packageName, version, arch);
		} catch (NonExistantRepositoryException e) {
			Loggers.SERVER.warn("MapBackedDebRepositoryPersistanceEngine: No repo found by name: " + repoName);
			return new ArrayList<>();
		}
	}

	@Override
	public List<DebPackageEntity> findAllByBuild(SBuild sBuild) {
		try {
			return this.myDebRepositoryManager.getPackageStoreForBuildType(sBuild.getBuildTypeId()).findAllForBuild(sBuild.getBuildId());
		} catch (NonExistantRepositoryException | NullPointerException e) {
			Loggers.SERVER.warn("MapBackedDebRepositoryPersistanceEngine: No repo found for sBuildType: " + sBuild.getBuildTypeId());
			return new ArrayList<>();
		}
	}

	@Override
	public List<DebPackageEntity> findAllByBuildType(SBuildType sBuildType) {
		try {
			return this.myDebRepositoryManager.getPackageStoreForBuildType(sBuildType.getBuildTypeId()).findAllForBuildType(sBuildType.getBuildTypeId());
		} catch (NonExistantRepositoryException | NullPointerException e) {
			Loggers.SERVER.warn("MapBackedDebRepositoryPersistanceEngine: No repo found for sBuildType: " + sBuildType.getBuildTypeId());
			return new ArrayList<>();
		}
	}

	@Override
	public List<DebPackageEntity> findAllByProject(SProject sProject) {
		return findAllByProjectId(sProject.getProjectId());
	}

	@Override
	public List<DebPackageEntity> findAllByProjectId(String projectId) {
		try {
			return this.myDebRepositoryManager.getPackageStoreForProject(projectId).findAll();
		} catch (NonExistantRepositoryException | NullPointerException e) {
			Loggers.SERVER.warn("MapBackedDebRepositoryPersistanceEngine: No repo found for project: " + projectId);
			return new ArrayList<>();
		}
	}

}
