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

import debrepo.teamcity.DebPackage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;

public interface DebRepositoryDatabase {
	
	public boolean addPackage(DebPackage entity);
	public boolean removePackage(DebPackage entity);
	public List<DebPackage> findPackageByName(String repositoryName, String name);
	public List<DebPackage> findPackageByNameAndVersion(String repositoryName, String name, String version);
	public List<DebPackage> findPackageByNameAndAchitecture(String repositoryName, String name, String arch);
	public List<DebPackage> findPackageByNameVersionAndArchitecture(String repositoryName, String name, String version, String arch);
	public List<DebPackage> findAllByBuild(SBuild sBuild);
	public List<DebPackage> findAllByBuildType(SBuildType sBuildtype);

}
