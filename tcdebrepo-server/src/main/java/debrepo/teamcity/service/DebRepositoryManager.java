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

import java.util.UUID;

import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryConfiguration;

public interface DebRepositoryManager {

	public DebPackageStore getPackageStore(String storeName) throws NonExistantRepositoryException;
	public DebPackageStore initialisePackageStore(DebRepositoryConfiguration conf);
	public DebPackageStore getPackageStoreForBuildType(String buildTypeid) throws NonExistantRepositoryException;
	public DebPackageStore getPackageStoreForProject(String projectId) throws NonExistantRepositoryException;
	public boolean registerBuildWithPackageStore(String storeName, String sBuildTypeId) throws NonExistantRepositoryException;
	public boolean registerBuildWithProjectPackageStore(String projectId, String sBuildTypeId) throws NonExistantRepositoryException;
	public boolean persist(UUID uuid);

}