/*******************************************************************************
 * Copyright 2016, 2017 Net Wolf UK
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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.entity.DebPackageNotFoundInStoreException;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryStatistics;
import lombok.Value;

public interface DebRepositoryManager {

	DebPackageStore getPackageStore(String storeName) throws NonExistantRepositoryException;
	public DebPackageStore initialisePackageStore(DebRepositoryConfiguration conf);
	public List<DebPackageStore> getPackageStoresForBuildType(String buildTypeid) throws NonExistantRepositoryException;
	public boolean persist(UUID uuid);
	public DebRepositoryStatistics getRepositoryStatistics(String uuid, String repoUrl);
	public DebRepositoryStatistics getRepositoryStatistics(DebRepositoryConfiguration projectConfig, String repoUrl);
	public void addBuildPackage(DebRepositoryConfiguration config, DebPackage newEntity);
	
	/**
	 * Returns all the architectures found in the repository for a dist/component combination.
	 * Note: If a package in the repo has an arch of "all", then all archs represented by all will be returned.  
	 * See {@link DebRepositoryConfiguration} for where that list of configured
	 * 
	 * @param repoName The name of the repository to search in.
	 * @param distName The dist to filter on.
	 * @param component The component to filter on.
	 * @return A Set<String> of unique architecture values found in the repository when searching for a specific dist and component.
	 *         If a package of arch "all" exists in the repository and matches the filters, then a list of all the architectures
	 *         represented by "all" will be returned.
	 * @throws NonExistantRepositoryException if the repoName searched for does not exist.
	 */
	public Set<String> findUniqueArchByDistAndComponent(String repoName, String distName, String component) throws NonExistantRepositoryException;
	
	/**
	 * Returns all the components found in the repository for the specified dist.
	 * @param repoName The name of the repository to search in.
	 * @param distName The dist to filter on.
	 * @return A Set<String> of unique component values found in the repository when searching for the specific dist.
	 * @throws NonExistantRepositoryException if the repoName searched for does not exist.
	 */
	public Set<String> findUniqueComponentByDist(String repoName, String distName) throws NonExistantRepositoryException;
	public Set<String> findUniqueDist(String repoName) throws NonExistantRepositoryException;
	public Set<String> findUniqueComponent(String repoName) throws NonExistantRepositoryException;
	public Set<String> findUniquePackageNameByComponent(String repoName, String component) throws NonExistantRepositoryException;
	public Set<String> findUniqueFilenames(String repoName) throws NonExistantRepositoryException;
	public List<? extends DebPackage> findAllByDistComponentArch(String repoName, String distName, String component, String archName) throws NonExistantRepositoryException;
	public List<? extends DebPackage> findAllByDistComponentArchIncludingAll(String repoName, String distName, String component, String archName) throws NonExistantRepositoryException;
	
	/**
	 * Returns a unique list of the DebPackage items (files) in the repository for the specified component and package name. The filename is used as the unique key.
	 * @param repoName The name of the repository to search in.
	 * @param component The component to filter on.
	 * @param packageName The package name to filter on.
	 * @return A List<? extends DebPackage> of unique packages found in the repository when searching for the specific component and package name.
	 * @throws NonExistantRepositoryException
	 */
	public List<? extends DebPackage> getUniquePackagesByComponentAndPackageName(String repoName, String component, String packageName) throws NonExistantRepositoryException;
	public DebPackage findByUri(String repoName, String uri) throws NonExistantRepositoryException, DebPackageNotFoundInStoreException;
	public boolean isExistingRepository(String repoName);
	public boolean isExistingRepository(UUID uuid);
	public void removeRepository(UUID uuid);
	public boolean isRestrictedRepository(String repoName) throws NonExistantRepositoryException;
	
	void addBuildPackages(DebRepositoryConfiguration debRepositoryConfiguration, List<DebPackage> newPackages) throws NonExistantRepositoryException;
	public void removeBuildPackages(DebPackageRemovalBean packageRemovalBean);
	
	@Value
	public static class DebPackageRemovalBean {
		private DebRepositoryConfiguration debRepositoryConfiguration;
		private String buildTypeId;
		private Long buildId;
		private List<DebPackage> packagesToKeep;
	}

	public List<? extends DebPackage>  findAllByFilenames(String repoName, Collection<String> filenames) throws NonExistantRepositoryException;



}