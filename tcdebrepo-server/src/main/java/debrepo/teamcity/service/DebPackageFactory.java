/*******************************************************************************
 *
 *  Copyright 2016 Net Wolf UK
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  
 *******************************************************************************/
package debrepo.teamcity.service;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.entity.DebPackageEntity;
import jetbrains.buildServer.serverSide.SBuild;

public class DebPackageFactory {

	public static DebPackage buildFromArtifact(SBuild build, String filename) {
		DebPackage e = new DebPackageEntity();
		e.setBuildId(build.getBuildId());
		e.setBuildTypeId(build.getBuildTypeId());
		e.setFilename(filename);
		return e;
	}
	
	public static DebPackage copy(DebPackage debPackage) {
		return DebPackageEntity.copy(debPackage);
	}
	

}
