/*******************************************************************************
 *
 *  Copyright 2016, 2017 Net Wolf UK
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
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.SqlUpdate;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.ebean.DebFileModel;
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
import debrepo.teamcity.service.DebRepositoryMaintenanceManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.DebRepositoryPersistanceException;
import debrepo.teamcity.service.NonExistantRepositoryException;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;

public class DebRepositoryManagerImpl extends DebRepositoryConfigurationManagerImpl implements DebRepositoryManager, DebRepositoryConfigurationManager, DebRepositoryMaintenanceManager {

	private EbeanServer myEbeanServer;

	public DebRepositoryManagerImpl(
			EbeanServer ebeanServer,
			DebRepositoryConfigurationFactory debRepositoryConfigurationFactory,
			DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister) {
		super(debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		this.myEbeanServer = ebeanServer;
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
			Loggers.SERVER.info("DebRepositoryManagerImpl:initialisePackageStore :: Repository '" + conf.getRepoName() + "' not found in DB. Initialising...");
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
		List<String> archs = DebFileModel.find.select("arch")
				  .setDistinct(true)
				  .where()
				  .eq("debpackages.repository.name", repoName)
				  .eq("debpackages.dist", distName)
				  .eq("debpackages.component", component)
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
		
		List<String> packages = DebFileModel.find.select("packageName")
				.setDistinct(true)
				.where().eq("debpackages.repository.name", repoName)
				.and().eq("debpackages.component", component)
				.findSingleAttributeList();
		
		return new TreeSet<String>(packages);

	}

	@Override
	public List<? extends DebPackage> getUniquePackagesByComponentAndPackageName(String repoName, String component,
			String packageName) throws NonExistantRepositoryException {
		if (! isExistingRepository(repoName)){
			throw new NonExistantRepositoryException();
		}
				
		String sql = " SELECT distinct O_DEBFILE.FILENAME, O_DEBPACKAGE.URI, O_DEBFILE.ID "
		 		+ " FROM O_DEBFILE "
		 		+ " JOIN O_DEBPACKAGE"
		 		+ " JOIN O_REPOSITORY "
		 		+ "   on O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID "
		 		+ "   AND O_DEBPACKAGE.REPOSITORY_ID = O_REPOSITORY.ID "
		 		+ "where O_REPOSITORY.NAME = :repoName "
		 		+ "   AND O_DEBPACKAGE.COMPONENT = :component "
		 		+ "   AND O_DEBFILE.PACKAGE_NAME = :packageName";

		RawSql rawSql = RawSqlBuilder.parse(sql)
				  					 .columnMapping("O_DEBFILE.ID", "debFile.id")
				  					 .columnMapping("O_DEBFILE.FILENAME", "debFile.filename")
				  					 .columnMapping("O_DEBPACKAGE.URI", "uri")				
				  					 .create();

		return DebPackageModel.getFind()
				  			  .setRawSql(rawSql)
				  			  .setParameter("repoName", repoName)
				  			  .setParameter("component", component)
				  			  .setParameter("packageName", packageName)
				  			  .findList();
		  
	}

	@Override
	public List<? extends DebPackage> findAllByDistComponentArch(String repoName, String distName, String component, String archName) throws NonExistantRepositoryException {
		return DebPackageModel.find.where().eq("repository.name", repoName).eq("dist", distName).eq("component", component).eq("debFile.arch", archName).findList(); 
	}
	

	@Override
	public List<? extends DebPackage> findAllByDistComponentArchIncludingAll(String repoName, String distName,
			String component, String archName) throws NonExistantRepositoryException {
		return DebPackageModel.find.where().or()
											   .and().eq("repository.name", repoName).eq("dist", distName).eq("component", component).eq("debFile.arch", archName).endAnd()
											   .and().eq("repository.name", repoName).eq("dist", distName).eq("component", component).ieq("debFile.arch", "all").endAnd()
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
									.eq("debFile.buildTypeId", packageRemovalBean.getBuildTypeId())
									.eq("debFile.buildId", packageRemovalBean.getBuildId())
								    .notIn("debFile.filename", extractFileNames(packageRemovalBean.getPackagesToKeep()))
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

	@Override
	public int getTotalPackageCount() {
		return DebPackageModel.find.findCount();
	}

	@Override
	public int getTotalFileCount() {
		return DebFileModel.find.findCount();
	}

	@Override
	public int getTotalRepositoryCount() {
		return DebRepositoryModel.find.findCount();
	}

	@Override
	public int getDanglingFileCount() {
		
		 String sql = " SELECT O_DEBFILE.id FROM O_DEBFILE "
				    + " left join O_DEBPACKAGE"
				    + " on O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID"
				    + " where O_DEBPACKAGE.DEB_FILE_ID IS NULL";

		  RawSql rawSql = RawSqlBuilder.parse(sql).create();

		  return DebFileModel.getFind().setRawSql(rawSql).findCount();
	}

	@Override
	public List<DebFileModel> getDanglingFiles() {
		 String sql = " SELECT O_DEBFILE.* FROM O_DEBFILE "
				    + " left join O_DEBPACKAGE"
				    + " on O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID"
				    + " where O_DEBPACKAGE.DEB_FILE_ID IS NULL";

		  RawSql rawSql = RawSqlBuilder.parse(sql).create();

		  return DebFileModel.getFind().setRawSql(rawSql).findList();
	}

	@Override
	public void removeDanglingFiles() throws DebRepositoryPersistanceException {
		int removedParams = 0;
		int removedFiles = 0;
		try {
			myEbeanServer.beginTransaction();
			removedParams = executeRemoveDanglingPackageParameters();
			removedFiles = executeRemoveDanglingFiles();
			myEbeanServer.commitTransaction();
		} catch (Exception e) {
			myEbeanServer.rollbackTransaction();
			Loggers.SERVER.debug(e);
			throw new DebRepositoryPersistanceException("Unable to remove dangling DebFileModel rows.");
		} finally {
			Loggers.SERVER.info("DebRepositoryManagerImpl:removeDanglingFiles:: Removed " + removedFiles + " Deb Files and " + removedParams + " file parameters from Database.");
		}
	}
	
	private int executeRemoveDanglingFiles() throws Exception {
		/* Delete the DebFile rows that don't
		 * have a corresponding DebPackage row.
		 */
 		
		String dml = "delete from O_DEBFILE where ID in "
				+ "(" 
				+ " SELECT O_DEBFILE.id FROM O_DEBFILE  "
				+ "		left join O_DEBPACKAGE  "
				+ "		on O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID "
				+ "		where O_DEBPACKAGE.DEB_FILE_ID IS NULL"
				+ ")";
		SqlUpdate update = myEbeanServer.createSqlUpdate(dml);
		return update.execute();
		
	}
	
	private int executeRemoveDanglingPackageParameters() throws Exception {
		
		/* Delete the package parameters for DebFile rows that don't
		 * have a corresponding DebPackage row.
		 * Do this first before deleting the DebFile row due to FK constraints.
		 */
		String dml = "delete from o_debfile_parameter where deb_file_id in"
				+ "("
				+ "	SELECT O_DEBFILE.id FROM O_DEBFILE  "
				+ "			left join O_DEBPACKAGE  "
				+ "			on O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID "
				+ "			where O_DEBPACKAGE.DEB_FILE_ID IS NULL "
				+ ")";
		SqlUpdate update = myEbeanServer.createSqlUpdate(dml);
		return update.execute();			
	}

	@Override
	public int getAssociatedFileCount() {
		 String sql = "	SELECT O_DEBFILE.id FROM O_DEBFILE  "
					+ "			left join O_DEBPACKAGE  "
					+ "			on O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID "
					+ "			where O_DEBPACKAGE.DEB_FILE_ID IS NOT NULL ";

		  RawSql rawSql = RawSqlBuilder.parse(sql).create();

		  return DebFileModel.getFind().setRawSql(rawSql).findCount();
	}
	
}
