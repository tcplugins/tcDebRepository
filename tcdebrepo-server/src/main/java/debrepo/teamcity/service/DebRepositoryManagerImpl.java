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

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.stereotype.Service;

import debrepo.teamcity.entity.DebPackageStore;
import lombok.AllArgsConstructor;

@Service
public class DebRepositoryManagerImpl implements DebRepositoryManager  {
	
	Map<String, DebRepoMeta> repositoryMetaData = new TreeMap<String, DebRepositoryManagerImpl.DebRepoMeta>();
	Map<UUID, DebPackageStore> repositories = new TreeMap<UUID, DebPackageStore>();
	
	@Override
	public DebPackageStore getPackageStore(String storeName) throws NonExistantRepositoryException {
		
		if (!repositoryMetaData.containsKey(storeName)) {
			throw new NonExistantRepositoryException();
		}
		return repositories.get(storeName);
	}
	
	@Override
	public DebPackageStore initialisePackageStore(String projectId, String storename) {
		DebRepoMeta newData = new DebRepoMeta(UUID.randomUUID(), projectId, storename);
		DebPackageStore newStore = new DebPackageStore();
		repositoryMetaData.put(storename, newData);
		repositories.put(newData.uuid, newStore);
		return repositories.get(repositoryMetaData.get(storename).uuid);
	}
	
	@AllArgsConstructor
	public static class DebRepoMeta {
		UUID uuid;
		String projectId;
		String repoName;
	}

}
