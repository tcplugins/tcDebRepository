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

import debrepo.teamcity.entity.DebPackageEntity;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

public interface DebRepositoryDatabase {
	
	public boolean addPackage(DebPackageEntity entity);
	public boolean removePackage(DebPackageEntity entity);
	public List<DebPackageEntity> findPackageByName(String repositoryName, String name);
	public List<DebPackageEntity> findPackageByNameAndVersion(String repositoryName, String name, String version);
	public List<DebPackageEntity> findPackageByNameAndAchitecture(String repositoryName, String name, String arch);
	public List<DebPackageEntity> findPackageByNameVersionAndArchitecture(String repositoryName, String name, String version, String arch);
	public List<DebPackageEntity> findAllByBuild(SBuild sBuild);
	public List<DebPackageEntity> findAllByBuildType(SBuildType sBuildtype);
	public List<DebPackageEntity> findAllByProject(SProject sProject);
	public List<DebPackageEntity> findAllByProjectId(String projectId);

}
