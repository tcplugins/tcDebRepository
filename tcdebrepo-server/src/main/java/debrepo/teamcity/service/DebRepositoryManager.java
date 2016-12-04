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
import java.util.Set;
import java.util.UUID;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.entity.DebPackageNotFoundInStoreException;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryStatistics;

public interface DebRepositoryManager {

	DebPackageStore getPackageStore(String storeName) throws NonExistantRepositoryException;
	public DebPackageStore initialisePackageStore(DebRepositoryConfiguration conf);
	public List<DebPackageStore> getPackageStoresForBuildType(String buildTypeid) throws NonExistantRepositoryException;
	public boolean persist(UUID uuid);
	public DebRepositoryStatistics getRepositoryStatistics(String uuid, String repoUrl);
	public DebRepositoryStatistics getRepositoryStatistics(DebRepositoryConfiguration projectConfig, String repoUrl);
	public void addBuildPackage(DebRepositoryConfiguration config, DebPackage newEntity);
	public Set<String> findUniqueArchByDistAndComponent(String repoName, String distName, String component) throws NonExistantRepositoryException;
	public Set<String> findUniqueComponentByDist(String repoName, String distName) throws NonExistantRepositoryException;
	public Set<String> findUniqueDist(String repoName) throws NonExistantRepositoryException;
	public Set<String> findUniqueComponent(String repoName) throws NonExistantRepositoryException;
	public Set<String> findUniquePackageNameByComponent(String repoName, String component) throws NonExistantRepositoryException;
	public List<? extends DebPackage> findAllByDistComponentArch(String repoName, String distName, String component, String archName) throws NonExistantRepositoryException;
	public List<? extends DebPackage> getUniquePackagesByComponentAndPackageName(String repoName, String component, String packageName) throws NonExistantRepositoryException;
	public DebPackage findByUri(String repoName, String uri) throws NonExistantRepositoryException, DebPackageNotFoundInStoreException;
	public boolean isExistingRepository(String repoName);
	public boolean isExistingRepository(UUID uuid);
	public void removeRepository(UUID uuid);
}