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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.springframework.stereotype.Service;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.helper.DebRepositoryDatabaseXmlPersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Service
public class DebRepositoryManagerImpl implements DebRepositoryManager  {
	
	Map<String, DebRepoMeta> repositoryMetaData = new TreeMap<String, DebRepositoryManagerImpl.DebRepoMeta>();
	Map<UUID, DebPackageStore> repositories = new TreeMap<UUID, DebPackageStore>();
	
	private final ProjectManager myProjectManager;
	private final DebRepositoryDatabaseXmlPersister myDebRepositoryDatabaseXmlPersister;
	
	public DebRepositoryManagerImpl(ProjectManager projectManager, DebRepositoryDatabaseXmlPersister debRepositoryDatabaseXmlPersister) {
		this.myProjectManager = projectManager;
		this.myDebRepositoryDatabaseXmlPersister = debRepositoryDatabaseXmlPersister;
	}
	
	@Override
	public DebPackageStore getPackageStore(String storeName) throws NonExistantRepositoryException {
		
		if (!repositoryMetaData.containsKey(storeName)) {
			throw new NonExistantRepositoryException();
		}
		return repositories.get(repositoryMetaData.get(storeName).uuid);
	}
	

	@Override
	public boolean persist(UUID uuid) {
		try {
			return this.myDebRepositoryDatabaseXmlPersister.persistDatabaseToXml(repositories.get(uuid));
		} catch (IOException e) {
			Loggers.SERVER.warn("DebRepositoryManagerImpl :: Failed to persist store to disk. UUID: " + uuid.toString());
			if (Loggers.SERVER.isDebugEnabled()) { Loggers.SERVER.debug(e); }
			return false;
		}
	}
	
	
	@Override
	public DebPackageStore initialisePackageStore(String projectId, String storename) {
		DebRepoMeta newData = new DebRepoMeta(UUID.randomUUID(), projectId, storename, new TreeSet<String>());
		DebPackageStore newStore = new DebPackageStore();
		newStore.setUuid(newData.uuid);
		repositoryMetaData.put(storename, newData);
		repositories.put(newData.uuid, newStore);
		Loggers.SERVER.info("DebRepositoryManagerImpl :: Initialised debrepo named '" + storename + "' for project '" + projectId + "'");
		return repositories.get(repositoryMetaData.get(storename).uuid);
	}
	
	@Override
	public DebPackageStore getPackageStoreForBuildType(String buildTypeid) throws NonExistantRepositoryException {
		SBuildType sBuildType = myProjectManager.findBuildTypeById(buildTypeid);
		List<SProject> projectPathList = this.myProjectManager.findProjectById(sBuildType.getProjectId()).getProjectPath();
		Collections.reverse(projectPathList);
		for (SProject sProject : projectPathList) {
			for (DebRepoMeta meta : repositoryMetaData.values()){
				if (meta.projectId.equals(sProject.getProjectId()) && meta.buildTypeIds.contains(buildTypeid)){
					if (repositories.containsKey(meta.uuid)){
						return repositories.get(meta.uuid);
					} else {
						throw new NonExistantRepositoryException();
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean registerBuildWithPackageStore(String storeName, String sBuildTypeId) throws NonExistantRepositoryException {
		if (!repositoryMetaData.containsKey(storeName)){
			throw new NonExistantRepositoryException();
		}
		boolean success = repositoryMetaData.get(storeName).getBuildTypeIds().add(sBuildTypeId);
		if (success){
			Loggers.SERVER.info("DebRepositoryManagerImpl :: Registered buildType '" + sBuildTypeId + "' with store '" + storeName + "'");
		} else {
			Loggers.SERVER.info("DebRepositoryManagerImpl :: buildType '" + sBuildTypeId + "' was already registered with store '" + storeName + "'");
		}
		return success;
	}
	
	@Override
	public boolean registerBuildWithProjectPackageStore(String projectId, String sBuildTypeId) throws NonExistantRepositoryException {
		for (Map.Entry<String,DebRepoMeta> entry : repositoryMetaData.entrySet()){
			if (entry.getValue().projectId.equals(projectId)){
				return registerBuildWithPackageStore(entry.getKey(), sBuildTypeId);
			}
		}
		return false;
	}

	@Override
	public DebPackageStore getPackageStoreForProject(String projectId) throws NonExistantRepositoryException {
		for (DebRepoMeta meta : repositoryMetaData.values()){
			if (meta.projectId.equals(projectId)){
				if (repositories.containsKey(meta.uuid)){
					return repositories.get(meta.uuid);
				} else {
					throw new NonExistantRepositoryException();
				}
			}			
		}
		return null;
	}
	
	@AllArgsConstructor
	public static class DebRepoMeta {
		
		@Getter
		UUID uuid;
		
		@Getter
		String projectId;
		
		@Getter
		String repoName;
		
		@Getter @Setter
		Set<String> buildTypeIds;
	}

}
