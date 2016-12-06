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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class DebPackageStore extends TreeMap<DebPackageEntityKey, DebPackageEntity> {

	private static final long serialVersionUID = 5836877424915088844L;
	
	@Getter @Setter
	private UUID uuid;
	
	private Map<String, DebPackageEntityKey> uriMap = new TreeMap<>();
	
	public DebPackageEntity findByUri(String uri) throws DebPackageNotFoundInStoreException {
		if (uriMap.containsKey(uri)){
			return get(uriMap.get(uri));
		}
		throw new DebPackageNotFoundInStoreException("File Not Found:: Uri:" + uri);
	}
	
	@Override
	public DebPackageEntity put(DebPackageEntityKey key, DebPackageEntity value) {
		super.put(key, value);
		uriMap.put(value.getUri(), key);
		return value;
	}

	public DebPackageEntity find(DebPackageEntityKey key) {
		return this.get(key);
	}
	
	public DebPackageEntity find(String packageName, String version, String arch, String component, String dist){
		return this.get(new DebPackageEntityKey(packageName, version, arch, component, dist));
	}
	
	public List<DebPackageEntity> findAllForBuildType(String buildTypeId) {
		List<DebPackageEntity> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getBuildTypeId().equalsIgnoreCase(buildTypeId)){
				debs.add(deb);
			}
		}
		return debs;
	}
	

	public List<DebPackageEntity> findAllForBuild(long buildId) {
		List<DebPackageEntity> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getBuildId()== buildId){
				debs.add(deb);
			}
		}
		return debs;
	}	
	
	public List<DebPackage> findAllForPackageName(String packageName) {
		List<DebPackage> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getPackageName().equalsIgnoreCase(packageName)){
				debs.add(deb);
			}
		}
		return debs;
	}
	
	public List<DebPackageEntity> findAllForPackageNameAndComponent(String packageName, String component) {
		List<DebPackageEntity> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getPackageName().equalsIgnoreCase(packageName) && deb.getComponent().equalsIgnoreCase(component)){
				debs.add(deb);
			}
		}
		return debs;
	}

	public List<DebPackage> findAllForPackageNameAndVersion(String packageName, String version) {
		List<DebPackage> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getPackageName().equalsIgnoreCase(packageName)
				&& deb.getVersion().equalsIgnoreCase(version)){
				debs.add(deb);
			}
		}
		return debs;
	}
	
	public List<DebPackage> findAllForPackageNameVersionAndArch(String packageName, String version, String arch) {
		List<DebPackage> debs = new ArrayList<>();
		for (DebPackageEntity deb: this.values()){
			if (deb.getPackageName().equalsIgnoreCase(packageName)
					&& deb.getVersion().equalsIgnoreCase(version)
					&& deb.getArch().equalsIgnoreCase(arch)){
				debs.add(deb);
			}
		}
		return debs;
	}

	public List<DebPackage> findAllForPackageNameAndArch(String packageName, String arch) {
		List<DebPackage> debs = new ArrayList<>();
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

	public List<DebPackage> findAllByDistComponentArch(String distName, String component, String archName) {
		List<DebPackage> debs = new ArrayList<>();
		for (DebPackage deb: this.values()){
			if (deb.getDist().equalsIgnoreCase(distName)
					&& deb.getComponent().equalsIgnoreCase(component)
					&& deb.getArch().equalsIgnoreCase(archName)){
				debs.add(deb);
			}
		}
		return debs;
	}
	
	public boolean removePackagesForBuild(Long buildId, List<DebPackage> packgesToKeep) {
		final Set<String> filenames = extractFileNames(packgesToKeep);
		boolean storeUpdated = false;
		List<DebPackageEntity> packagesForBuild = findAllForBuild(buildId);
		if (packagesForBuild.isEmpty()) {
			if (Loggers.SERVER.isDebugEnabled()) {
				Loggers.SERVER.debug("DebPackageStore#removePackagesForBuild :: No packages found in store for build " + buildId.toString() + ". No packages will be removed from store " + this.uuid.toString());
			}
			return storeUpdated;
		}
		for (DebPackage debPackage : findAllForBuild(buildId)) {
			if (filenames.contains(debPackage.getFilename())) {
				if (Loggers.SERVER.isDebugEnabled()) {
					Loggers.SERVER.debug("DebPackageStore#removePackagesForBuild :: Not removing " + debPackage.getFilename() + " because it's in the keep list." + debPackage.toString());
				}
				continue;
			}
			if (Loggers.SERVER.isDebugEnabled()) {
				Loggers.SERVER.debug("DebPackageStore#removePackagesForBuild :: Removing " + debPackage.getFilename() + " because it's NOT in the keep list." + debPackage.toString());
			}
			DebPackageEntity e = DebPackageEntity.copy(debPackage);
			remove(e.buildKey());
			uriMap.remove(e.getUri());
			storeUpdated = true;
		}
		return storeUpdated;		
	}
	
	private Set<String> extractFileNames(List<DebPackage> packages) {
		final Set<String> filenames = new TreeSet<>(); 
		for (DebPackage debPackage : packages) {
			filenames.add(debPackage.getFilename());
		}
		return filenames;
	}
	
	@AllArgsConstructor @Data
	private static class DebPackageEntityArchUriKey implements Comparable<DebPackageEntityArchUriKey>{
		String arch;
		String uri;
		
		@Override
		public int compareTo(DebPackageEntityArchUriKey o) {
			if (this.getArch().equalsIgnoreCase(o.getArch())){
				return this.getUri().compareTo(o.getUri());
			} else {
				return this.getArch().compareToIgnoreCase(o.getArch());
			}
		}
	}
}
