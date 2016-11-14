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

import java.util.List;
import java.util.UUID;

import debrepo.teamcity.archive.DebFileReader;
import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryStatistics;

public interface DebRepositoryManager {

	public DebPackageStore getPackageStore(String storeName) throws NonExistantRepositoryException;
	public DebPackageStore initialisePackageStore(DebRepositoryConfiguration conf);
	public List<DebPackageStore> getPackageStoresForBuildType(String buildTypeid) throws NonExistantRepositoryException;
	public List<DebPackageStore> getPackageStoresForDebPackage(DebPackageEntity entity) throws NonExistantRepositoryException;
	public DebPackageStore getPackageStoreForProject(String projectId) throws NonExistantRepositoryException;
	public boolean persist(UUID uuid);
	public DebRepositoryStatistics getRepositoryStatistics(String uuid, String repoUrl);
	public DebRepositoryStatistics getRepositoryStatistics(DebRepositoryConfiguration projectConfig, String repoUrl);
	public void addBuildPackages(String buildTypeId, List<DebPackageEntity> debPackageEntities, DebFileReader debFileReader);
	public void addBuildPackage(DebRepositoryConfiguration config, DebPackageEntity newEntity);

}