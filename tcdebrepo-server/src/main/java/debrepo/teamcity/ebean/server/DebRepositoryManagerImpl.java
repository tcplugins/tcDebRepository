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
package debrepo.teamcity.ebean.server;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.avaje.ebean.EbeanServer;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.ebean.DebPackageModel;
import debrepo.teamcity.ebean.DebRepositoryModel;
import debrepo.teamcity.entity.DebPackageNotFoundInStoreException;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryStatistics;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryConfigurationManagerImpl;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;

public class DebRepositoryManagerImpl extends DebRepositoryConfigurationManagerImpl implements DebRepositoryManager, DebRepositoryConfigurationManager {

	public DebRepositoryManagerImpl(
			EbeanServer ebeanServer,
			DebRepositoryConfigurationFactory debRepositoryConfigurationFactory,
			DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister) {
		super(debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
	}

	/**
	 * Shouldn't be used anyway.
	 */
	@Override
	public DebPackageStore getPackageStore(String storeName) throws NonExistantRepositoryException {
		return null;
	}

	
	@Override
	public DebPackageStore initialisePackageStore(DebRepositoryConfiguration conf) {
		DebRepositoryModel repo = DebRepositoryModel.find.where().eq("uuid", conf.getUuid().toString()).findUnique();
		if (repo == null) {
			repo = new DebRepositoryModel();
			Loggers.SERVER.info("DebRepositoryManagerImpl:initialisePackageStore :: Repository '" + conf.getRepoName() + " not found in DB. Initliasing...");
		}
		repo.setName(conf.getRepoName());
		repo.setUuid(conf.getUuid().toString());
		repo.setProjectId(conf.getProjectId());
		repo.save();
		return null;
	}

	/**
	 * Shouldn't be required for DB.
	 */
	@Override
	public List<DebPackageStore> getPackageStoresForBuildType(String buildTypeid) throws NonExistantRepositoryException {
		// Shouldn't be required for DB.
		return null;
	}

	/** 
	 * This is a noop on a DB.
	 */
	@Override
	public boolean persist(UUID uuid) {
		// This is a noop on a DB.
		return true;
	}

	@Override
	public DebRepositoryStatistics getRepositoryStatistics(String uuid, String repoUrl) {
		int count = DebPackageModel.find.where().eq("repository.uuid", uuid).findCount();
		DebRepositoryConfiguration config = getDebRepositoryConfiguration(uuid);
		int filterCount = 0;
		for (DebRepositoryBuildTypeConfig btConfig : config.getBuildTypes()) {
			filterCount = filterCount + btConfig.getDebFilters().size();
		}
		return new DebRepositoryStatistics(count, repoUrl, filterCount, config.isRestricted());
	}

	@Override
	public DebRepositoryStatistics getRepositoryStatistics(DebRepositoryConfiguration projectConfig, String repoUrl) {
		return getRepositoryStatistics(projectConfig.getUuid().toString(), repoUrl);
	}

	@Override
	public void addBuildPackage(DebRepositoryConfiguration config, DebPackage newEntity) {
		//initialisePackageStore(config);
		DebPackageModel m = DebPackageModel.copy(newEntity);
		DebRepositoryModel repo = DebRepositoryModel.find.where().eq("uuid", config.getUuid().toString()).findUnique();
		
		m.setRepository(repo);
		m.save();
	}
	
	@Override
	public Set<String> findUniqueArchByDistAndComponent(String repoName, String distName, String component) throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}
//		List<String> archs = new QDebPackageModel().select("arch")
//				.setDistinct(false)
//				.repository.name.eq(repoName)
//				.dist.eq(distName)
//				.component.eq(component)
//				.findSingleAttributeList();
		List<String> archs = DebPackageModel.find.select("arch")
				  .setDistinct(false)
				  .where()
				  .eq("repository.name", repoName)
				  .eq("dist", distName)
				  .eq("component", component)
				  .findSingleAttributeList();
		if (archs.contains("all")) {
			archs.addAll(getDebRepositoryConfigurationByName(repoName).getArchitecturesRepresentedByAll());
		}
		return new TreeSet<String>(archs);
	}

	@Override
	public Set<String> findUniqueComponentByDist(String repoName, String distName) throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}
//		List<String> components = new QDebPackageModel().select("component")
//				.setDistinct(false)
//				.repository.name.eq(repoName)
//				.dist.eq(distName)
//				.findSingleAttributeList();
		List<String> components = DebPackageModel.find.select("component")
				  .setDistinct(false)
				  .where().eq("repository.name", repoName)
				  .and().eq("dist", distName)
				  .findSingleAttributeList();
		return new TreeSet<String>(components);
	}

	@Override
	public Set<String> findUniqueDist(String repoName) throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}
//		List<String> dists = new QDebPackageModel().select("dist")
//				.setDistinct(false)
//				.repository.name.eq(repoName)
//				.findSingleAttributeList();
		List<String> dists = DebPackageModel.find.select("dist")
				  .setDistinct(false)
				  .where().eq("repository.name", repoName)
				  .findSingleAttributeList();
		return new TreeSet<String>(dists);
	}
	
	@Override
	public Set<String> findUniqueFilenames(String repoName) throws NonExistantRepositoryException {
		List<String> files = DebPackageModel.find.select("filename")
				  .setDistinct(false)
				  .where().eq("repository.name", repoName)
				  .findSingleAttributeList();
		return new TreeSet<String>(files);
	}
	
	@Override
	public List<? extends DebPackage>  findAllByFilenames(String repoName, Collection<String> filenames) {
		return DebPackageModel.find.where().in("filename", filenames).findList();
	}


	@Override
	public Set<String> findUniqueComponent(String repoName) throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}
//		List<String> components = new QDebPackageModel().select("component")
//				  .setDistinct(false)
//				  .repository.name.eq(repoName)
//				  .findSingleAttributeList();
		List<String> components = DebPackageModel.find.select("component")
				.setDistinct(false)
				.where().eq("repository.name", repoName)
				.findSingleAttributeList();
		return new TreeSet<String>(components);
	}

	@Override
	public Set<String> findUniquePackageNameByComponent(String repoName, String component)
			throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}		

//		List<String> packages = new QDebPackageModel().select("packageName")
//				 					  .setDistinct(false)
//				 					  .repository.name.eq(repoName)
//				 					  .component.eq(component)
//				 					  .findSingleAttributeList();
		
		List<String> packages = DebPackageModel.find.select("packageName")
				.setDistinct(false)
				.where().eq("repository.name", repoName)
				.and().eq("component", component)
				.findSingleAttributeList();
		
		return new TreeSet<String>(packages);

	}

	@Override
	public List<? extends DebPackage> getUniquePackagesByComponentAndPackageName(String repoName, String component,
			String packageName) throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}
//		return new QDebPackageModel().select("filename, uri")
//									 .setDistinct(true)
//									 .repository.name.eq(repoName)
//									 .component.eq(component)
//									 .packageName.eq(packageName)
//									 .findList(); 
		return DebPackageModel.find.select("filename, uri, packageName, version, arch")
				.setDistinct(true)
				.where().eq("repository.name",repoName)
				.eq("component", component)
				.eq("packageName", packageName)
				.findList(); 
	}

	@Override
	public List<? extends DebPackage> findAllByDistComponentArch(String repoName, String distName, String component, String archName) throws NonExistantRepositoryException {
		return DebPackageModel.find.where().eq("repository.name", repoName).eq("dist", distName).eq("component", component).eq("arch", archName).findList(); 
	}
	

	@Override
	public List<? extends DebPackage> findAllByDistComponentArchIncludingAll(String repoName, String distName,
			String component, String archName) throws NonExistantRepositoryException {
		return DebPackageModel.find.where().or()
											   .and().eq("repository.name", repoName).eq("dist", distName).eq("component", component).eq("arch", archName).endAnd()
											   .and().eq("repository.name", repoName).eq("dist", distName).eq("component", component).ieq("arch", "all").endAnd()
											   .findList();
	}

	@Override
	public DebPackage findByUri(String repoName, String uri)
			throws NonExistantRepositoryException, DebPackageNotFoundInStoreException {
		try {
			return DebPackageModel.find.where().eq("repository.name", repoName).eq("uri", uri).findList().get(0);
		} catch (IndexOutOfBoundsException e) {
			throw new DebPackageNotFoundInStoreException(uri);
		}
	}

	@Override
	public boolean isExistingRepository(String repoName) {
		return DebRepositoryModel.find.where().eq("name",repoName).findCount()  > 0;
	}

	@Override
	public boolean isExistingRepository(UUID uuid) {
		//return new QDebRepositoryModel().findCount() > 0;
		//return new QDebRepositoryModel().uuid.eq(uuid.toString()).findCount() > 0;
		return DebRepositoryModel.find.where().eq("uuid", uuid.toString()).findCount() > 0;
	}


	@Override
	public void removeRepository(UUID uuid) {
		
		//DebPackageModel.db().createUpdate(DebPackageModel.class, "delete from orderDetail").execute();
		
		//DebPackageModel.find.where().eq("repository.uuid", uuid.toString()).delete();
		DebRepositoryModel repo = DebRepositoryModel.find.where().eq("uuid", uuid.toString()).findUnique();
		repo.delete();
	}

	@Override
	public void addBuildPackages(DebRepositoryConfiguration debRepositoryConfiguration, List<DebPackage> newPackages)
			throws NonExistantRepositoryException {
		for (DebPackage debPackage : newPackages) {
			addBuildPackage(debRepositoryConfiguration, debPackage);
		}
		
	}

	@Override
	public void removeBuildPackages(DebPackageRemovalBean packageRemovalBean) {
		
		List<DebPackageModel> packagesForDeletion = DebPackageModel.find.where()
									.eq("repository.uuid", packageRemovalBean.getDebRepositoryConfiguration().getUuid().toString())
									.eq("buildTypeId", packageRemovalBean.getBuildTypeId())
									.eq("buildId", packageRemovalBean.getBuildId())
								    .notIn("filename", extractFileNames(packageRemovalBean.getPackagesToKeep()))
								    .findList();
		DebPackageModel.db().beginTransaction();
		try {
			for (DebPackageModel m : packagesForDeletion) {
				m.delete();
			}
			DebPackageModel.db().commitTransaction();
		} finally {
			DebPackageModel.db().endTransaction();
		}
	}
	
	private Set<String> extractFileNames(List<DebPackage> packages) {
		final Set<String> filenames = new TreeSet<>(); 
		for (DebPackage debPackage : packages) {
			filenames.add(debPackage.getFilename());
		}
		return filenames;
	}
	
}
