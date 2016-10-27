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
package debrepo.teamcity.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public class DebPackageStore extends TreeMap<DebPackageEntityKey, DebPackageEntity> {

	private static final long serialVersionUID = 5836877424915088844L;
	
	@Getter @Setter
	private UUID uuid;

	public DebPackageEntity find(DebPackageEntityKey key) {
		return this.get(key);
	}
	
	public DebPackageEntity find(String packageName, String version, String arch){
		return this.get(new DebPackageEntityKey(packageName, version, arch));
	}
	
	public List<DebPackageEntity> findAllForBuildType(String buildTypeId) {
		List<DebPackageEntity> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getSBuildTypeId().equalsIgnoreCase(buildTypeId)){
				debs.add(deb);
			}
		}
		return debs;
	}
	

	public List<DebPackageEntity> findAllForBuild(long buildId) {
		List<DebPackageEntity> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getSBuildId()== buildId){
				debs.add(deb);
			}
		}
		return debs;
	}	
	
	public List<DebPackageEntity> findAllForPackageName(String packageName) {
		List<DebPackageEntity> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getPackageName().equalsIgnoreCase(packageName)){
				debs.add(deb);
			}
		}
		return debs;
	}

	public List<DebPackageEntity> findAllForPackageNameAndVersion(String packageName, String version) {
		List<DebPackageEntity> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getPackageName().equalsIgnoreCase(packageName)
				&& deb.getVersion().equalsIgnoreCase(version)){
				debs.add(deb);
			}
		}
		return debs;
	}
	
	public List<DebPackageEntity> findAllForPackageNameVersionAndArch(String packageName, String version, String arch) {
		List<DebPackageEntity> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getPackageName().equalsIgnoreCase(packageName)
					&& deb.getVersion().equalsIgnoreCase(version)
					&& deb.getArch().equalsIgnoreCase(arch)){
				debs.add(deb);
			}
		}
		return debs;
	}

	public List<DebPackageEntity> findAllForPackageNameAndArch(String packageName, String arch) {
		List<DebPackageEntity> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getPackageName().equalsIgnoreCase(packageName)
					&& deb.getArch().equalsIgnoreCase(arch)){
				debs.add(deb);
			}
		}
		return debs;
	}

	public List<DebPackageEntity> findAll() {
		List<DebPackageEntity> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			debs.add(deb);
		}
		return debs;
	}

}
