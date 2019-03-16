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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.digest.DigestUtils;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.SqlUpdate;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.GenericRepositoryFile;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.ebean.DebDistCompArchModel;
import debrepo.teamcity.ebean.DebFileModel;
import debrepo.teamcity.ebean.DebPackageModel;
import debrepo.teamcity.ebean.DebPackagesFileModel;
import debrepo.teamcity.ebean.DebReleaseFileModel;
import debrepo.teamcity.ebean.DebReleaseFileSimpleModel;
import debrepo.teamcity.ebean.DebRepositoryModel;
import debrepo.teamcity.entity.DebPackageNotFoundInStoreException;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryStatistics;
import debrepo.teamcity.entity.helper.DebPackageToPackageDescriptionBuilder;
import debrepo.teamcity.entity.helper.ReleaseDescriptionBuilder;
import debrepo.teamcity.service.DebReleaseFileGenerator;
import debrepo.teamcity.service.DebReleaseFileLocator;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryConfigurationManagerImpl;
import debrepo.teamcity.service.DebRepositoryItemNotFoundException;
import debrepo.teamcity.service.DebRepositoryMaintenanceManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.DebRepositoryPersistanceException;
import debrepo.teamcity.service.NonExistantRepositoryException;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import lombok.AllArgsConstructor;
import lombok.Value;

public class DebRepositoryManagerImpl extends DebRepositoryConfigurationManagerImpl 
								   implements DebRepositoryManager, DebRepositoryConfigurationManager, 
											  DebRepositoryMaintenanceManager, DebReleaseFileGenerator,
											  DebReleaseFileLocator {

	private EbeanServer myEbeanServer;
	private ReleaseDescriptionBuilder myReleaseDescriptionBuilder;

	public DebRepositoryManagerImpl(
			EbeanServer ebeanServer,
			DebRepositoryConfigurationFactory debRepositoryConfigurationFactory,
			DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister,
			ReleaseDescriptionBuilder debRepositoryToReleaseDescriptionBuilder) {
		super(debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		this.myEbeanServer = ebeanServer;
		this.myReleaseDescriptionBuilder = debRepositoryToReleaseDescriptionBuilder;
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
		DebRepositoryModel repo = DebRepositoryModel.find.query().where().eq("uuid", conf.getUuid().toString()).findOne();
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
		int count = DebPackageModel.find.query().where().eq("repository.uuid", uuid).findCount();
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
		DebPackageModel m = DebPackageModel.copy(newEntity);
		DebRepositoryModel repo = DebRepositoryModel.find.query().where().eq("uuid", config.getUuid().toString()).findOne();
		m.setRepository(repo);
		m.save();
	}
	
	public void addBuildPackage(DebRepositoryModel repo, DebPackage newEntity) {
		DebPackageModel m = DebPackageModel.copy(newEntity);
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
		List<String> archs = DebFileModel.find.query().select("arch")
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
		List<String> components = DebPackageModel.find.query().select("component")
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
		List<String> dists = DebPackageModel.find.query().select("dist")
				  .setDistinct(false)
				  .where().eq("repository.name", repoName)
				  .findSingleAttributeList();
		return new TreeSet<String>(dists);
	}
	
	@Override
	public Set<String> findUniqueFilenames(String repoName) throws NonExistantRepositoryException {
		List<String> files = DebPackageModel.find.query().select("filename")
				  .setDistinct(false)
				  .where().eq("repository.name", repoName)
				  .findSingleAttributeList();
		return new TreeSet<String>(files);
	}
	
	@Override
	public List<? extends DebPackage>  findAllByFilenames(String repoName, Collection<String> filenames) {
		return DebPackageModel.find.query().where().in("filename", filenames).findList();
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
		List<String> components = DebPackageModel.find.query().select("component")
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
		
		List<String> packages = DebFileModel.find.query().select("packageName")
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

		return DebPackageModel.find.query()
				  			  .setRawSql(rawSql)
				  			  .setParameter("repoName", repoName)
				  			  .setParameter("component", component)
				  			  .setParameter("packageName", packageName)
				  			  .findList();
		  
	}

	@Override
	public List<? extends DebPackage> findAllByDistComponentArch(String repoName, String distName, String component, String archName) throws NonExistantRepositoryException {
		return DebPackageModel.find.query().where().eq("repository.name", repoName).eq("dist", distName).eq("component", component).eq("debFile.arch", archName).findList(); 
	}
	

	@Override
	public List<? extends DebPackage> findAllByDistComponentArchIncludingAll(String repoName, String distName,
			String component, String archName) throws NonExistantRepositoryException {
		return DebPackageModel.find.query().where().or()
											   .and().eq("repository.name", repoName).eq("dist", distName).eq("component", component).eq("debFile.arch", archName).endAnd()
											   .and().eq("repository.name", repoName).eq("dist", distName).eq("component", component).ieq("debFile.arch", "all").endAnd()
											   .findList();
	}

	@Override
	public DebPackage findByUri(String repoName, String uri)
			throws NonExistantRepositoryException, DebPackageNotFoundInStoreException {
		try {
			return DebPackageModel.find.query().where().eq("repository.name", repoName).eq("uri", uri).findList().get(0);
		} catch (IndexOutOfBoundsException e) {
			throw new DebPackageNotFoundInStoreException(uri);
		}
	}

	@Override
	public boolean isExistingRepository(String repoName) {
		return DebRepositoryModel.find.query().where().eq("name",repoName).findCount()  > 0;
	}

	@Override
	public boolean isExistingRepository(UUID uuid) {
		//return new QDebRepositoryModel().findCount() > 0;
		//return new QDebRepositoryModel().uuid.eq(uuid.toString()).findCount() > 0;
		return DebRepositoryModel.find.query().where().eq("uuid", uuid.toString()).findCount() > 0;
	}

	public DebRepositoryModel findRepository(UUID uuid) throws NonExistantRepositoryException {
		DebRepositoryModel repo = DebRepositoryModel.find.query().where().eq("uuid", uuid.toString()).findOne();
		if (repo == null) {
			throw new NonExistantRepositoryException();
		}
		return repo;
	}
	
	@Override
	public void removeRepository(UUID uuid) {
		
		//DebPackageModel.db().createUpdate(DebPackageModel.class, "delete from orderDetail").execute();
		
		//DebPackageModel.find.where().eq("repository.uuid", uuid.toString()).delete();
		DebRepositoryModel repo = DebRepositoryModel.find.query().where().eq("uuid", uuid.toString()).findOne();
		repo.delete();
	}

	@Override
	public void addBuildPackages(DebRepositoryConfiguration debRepositoryConfiguration, List<DebPackage> newPackages)
			throws NonExistantRepositoryException {
		DebRepositoryModel repo = findRepository(debRepositoryConfiguration.getUuid());
		Set<DistComponentArchitecture> packageFileToRegenerate = new HashSet<>();
		
		//DebPackageModel.db().beginTransaction();
		for (DebPackage debPackage : newPackages) {
			addBuildPackage(repo, debPackage);
			packageFileToRegenerate.add(new DistComponentArchImpl(debPackage.getDist(), debPackage.getComponent(), debPackage.getArch()));
		}
		updateReleaseFiles(debRepositoryConfiguration, repo, packageFileToRegenerate);
		//DebPackageModel.db().endTransaction();
	}
	
	public void rebuildSimpleReleaseFiles(DebRepositoryConfiguration debRepositoryConfiguration, 
			DebRepositoryModel repositoryModel, 
			Set<? extends DistComponentArchitecture> dcasToUpdate) throws NonExistantRepositoryException {

		for (DistComponentArchitecture dca : dcasToUpdate) {
			updateSimpleRelaseFile(debRepositoryConfiguration, repositoryModel, dca);
		}
	}
	
	@Override
	public void bulkAddBuildPackages(DebRepositoryConfiguration debRepositoryConfiguration, List<DebPackage> newPackages)
			throws NonExistantRepositoryException {
		for (DebPackage debPackage : newPackages) {
			addBuildPackage(debRepositoryConfiguration, debPackage);
		}
	}
	
	private Set<? extends DistComponentArchitecture> getDistinctDistComponentArch(DebRepositoryConfiguration config) throws NonExistantRepositoryException {
		
		if (! isExistingRepository(config.getRepoName())){
			throw new NonExistantRepositoryException();
		}
				
		String sql = " select distinct DIST, COMPONENT, ARCH "
		 		+ " FROM O_DEBPACKAGE "
		 		+ " JOIN O_DEBFILE"
		 		+ " JOIN O_REPOSITORY "
		 		+ "   on O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID "
		 		+ "   AND O_DEBPACKAGE.REPOSITORY_ID = O_REPOSITORY.ID "
		 		+ "where O_REPOSITORY.NAME = :repoName ";

		RawSql rawSql = RawSqlBuilder.parse(sql)
				  					 .columnMapping("DIST", "dist")
				  					 .columnMapping("COMPONENT", "component")
				  					 .columnMapping("ARCH", "arch")				
				  					 .create();

		Query<DebDistCompArchModel> query = Ebean.find(DebDistCompArchModel.class);
		
		return query.setRawSql(rawSql)
				    .setParameter("repoName", config.getRepoName())
				  			  .findSet();
		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void updateAllReleaseFiles(DebRepositoryConfiguration config) throws NonExistantRepositoryException {
		Set<? extends DistComponentArchitecture> dcas = getDistinctDistComponentArch(config);
		DebRepositoryModel repo = findRepository(config.getUuid());
		rebuildSimpleReleaseFiles(config, repo, dcas);
		updateReleaseFiles(config, repo, dcas);
	}

	@Override
	public void removeBuildPackages(DebPackageRemovalBean packageRemovalBean) {
		
		List<DebPackageModel> packagesForDeletion = DebPackageModel.find.query().where()
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
			if (packagesForDeletion.size() > 0) {
				Set<DistComponentArchitecture> distComps = new HashSet<>();
				for (DebPackageModel m : packagesForDeletion) {
					distComps.add(new DistComponentArchImpl(m.getDist(), m.getComponent(), m.getArch()));
				}
				
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
		return DebPackageModel.find.query().findCount();
	}

	@Override
	public int getTotalFileCount() {
		return DebFileModel.find.query().findCount();
	}

	@Override
	public int getTotalRepositoryCount() {
		return DebRepositoryModel.find.query().findCount();
	}

	@Override
	public int getDanglingFileCount() {
		
		 String sql = " SELECT O_DEBFILE.id FROM O_DEBFILE "
				    + " left join O_DEBPACKAGE"
				    + " on O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID"
				    + " where O_DEBPACKAGE.DEB_FILE_ID IS NULL";

		  RawSql rawSql = RawSqlBuilder.parse(sql).create();

		  return DebFileModel.find.query().setRawSql(rawSql).findCount();
	}

	@Override
	public List<DebFileModel> getDanglingFiles() {
		 String sql = " SELECT O_DEBFILE.* FROM O_DEBFILE "
				    + " left join O_DEBPACKAGE"
				    + " on O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID"
				    + " where O_DEBPACKAGE.DEB_FILE_ID IS NULL";

		  RawSql rawSql = RawSqlBuilder.parse(sql).create();

		  return DebFileModel.find.query().setRawSql(rawSql).findList();
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

		  return DebFileModel.find.query().setRawSql(rawSql).findCount();
	}
	
	@Override
	public void updateReleaseFiles(DebRepositoryConfiguration config, Set<? extends DistComponentArchitecture> dcas) throws NonExistantRepositoryException {
		DebRepositoryModel repositoryModel = findRepository(config.getUuid());
		updateReleaseFiles(config, repositoryModel, dcas);
	}	

	private void updateReleaseFiles(DebRepositoryConfiguration config, DebRepositoryModel repositoryModel, Set<? extends DistComponentArchitecture> dcas) throws NonExistantRepositoryException {
		Set<String> distsToUpdate = new HashSet<>();
		
		for (DistComponentArchitecture dca : dcas) {
			updatePackagesFiles(config, dca);
			distsToUpdate.add(dca.getDist());
		}
		
		for (String dist : distsToUpdate) {
			updateReleaseFile(config, repositoryModel, dist);
		}
	}

	private void updateSimpleRelaseFile(DebRepositoryConfiguration config, DebRepositoryModel repositoryModel,
			DistComponentArchitecture distCompArch) throws NonExistantRepositoryException {
		
		String releaseFileContents = myReleaseDescriptionBuilder.buildSimpleReleaseFile(config, distCompArch.getDist(), distCompArch.getComponent(), distCompArch.getArch());
		
		DebReleaseFileSimpleModel releaseFileSimple = new DebReleaseFileSimpleModel();
		releaseFileSimple.setRepository(findRepository(config.getUuid()));
		releaseFileSimple.setDist(distCompArch.getDist());
		releaseFileSimple.setComponent(distCompArch.getComponent());
		releaseFileSimple.setArch(distCompArch.getArch());
		releaseFileSimple.setPath(distCompArch.getDist() 
								+ "/" + distCompArch.getComponent() 
								+ "/binary-" + distCompArch.getArch() 
								+ "/Release");
		
		releaseFileSimple.setReleaseFile(releaseFileContents);
		releaseFileSimple.setMd5(DigestUtils.md5Hex(releaseFileContents));
		releaseFileSimple.setSha1(DigestUtils.sha1Hex(releaseFileContents));
		releaseFileSimple.setSha256(DigestUtils.sha256Hex(releaseFileContents));
		
		releaseFileSimple.save();
		
	}

	private void updateReleaseFile(DebRepositoryConfiguration config, DebRepositoryModel repositoryModel, String dist)
			throws NonExistantRepositoryException {
		String packagesSql = " SELECT id, PACKAGES_FILE from O_DEB_PACKAGES_FILE where ID IN "
		 		    + " ( SELECT ID FROM O_DEB_PACKAGES_FILE T1"
		 		    + "   WHERE MODIFIED_TIME = "
		 		    + "   ( SELECT max(MODIFIED_TIME) "
		 		    + "     FROM O_DEB_PACKAGES_FILE "
		 		    + "		WHERE O_DEB_PACKAGES_FILE.REPOSITORY_ID = :repoId"
		 		    + "      AND O_DEB_PACKAGES_FILE.DIST = :dist"
		 		    + "		 AND T1.PATH = O_DEB_PACKAGES_FILE.PATH"
		 		    + "	  ) "
		 		    + " ) ORDER BY PATH";

		RawSql packagesRawSql = RawSqlBuilder.parse(packagesSql).create();
		
		String releasesSql = " SELECT id, RELEASE_FILE from O_DEB_RELEASE_FILE_SIMPLE where ID IN "
				+ " ( SELECT ID FROM O_DEB_RELEASE_FILE_SIMPLE T1"
				+ "   WHERE MODIFIED_TIME = "
				+ "   ( SELECT max(MODIFIED_TIME) "
				+ "     FROM O_DEB_RELEASE_FILE_SIMPLE "
				+ "		WHERE O_DEB_RELEASE_FILE_SIMPLE.REPOSITORY_ID = :repoId"
				+ "      AND O_DEB_RELEASE_FILE_SIMPLE.DIST = :dist"
				+ "		 AND T1.PATH = O_DEB_RELEASE_FILE_SIMPLE.PATH"
				+ "	  ) "
				+ " ) ORDER BY PATH";
		
		RawSql releasesRawSql = RawSqlBuilder.parse(releasesSql).create();

		List<DebPackagesFileModel> packagesFiles = DebPackagesFileModel.find.query()
					 .setRawSql(packagesRawSql)
					 .setParameter("repoId", repositoryModel.getId())
					 .setParameter("dist", dist)
					 .findList();
		
		List<DebReleaseFileSimpleModel> releaseFilesSimple = DebReleaseFileSimpleModel.find.query()
					 .setRawSql(releasesRawSql)
					 .setParameter("repoId", repositoryModel.getId())
					 .setParameter("dist", dist)
					 .findList();
		
		List<GenericRepositoryFile> repoFiles = new ArrayList<>();
		repoFiles.addAll(releaseFilesSimple);
		repoFiles.addAll(packagesFiles);
		
		Date modifiedTime = new Date();
		String releaseFileContent = myReleaseDescriptionBuilder.buildPackageDescriptionList(config, repoFiles, dist, modifiedTime);
		
		DebReleaseFileModel releaseFile = DebReleaseFileModel.find.query()
				.where()
				.eq("repository.uuid", config.getUuid().toString())
				.eq("dist", dist)
				.findOne();

		if (releaseFile == null) {
			releaseFile = new DebReleaseFileModel();
			releaseFile.setRepository(findRepository(config.getUuid()));
			releaseFile.setDist(dist);
		}
		releaseFile.setReleaseFile(releaseFileContent);
		releaseFile.setModifiedTime(modifiedTime);
		releaseFile.save();
	}
	
	@Override
	public void updatePackagesFiles(DebRepositoryConfiguration config, DistComponentArchitecture distCompArch) throws NonExistantRepositoryException {
		List<? extends DebPackage> packages = this.findAllByDistComponentArchIncludingAll(config.getRepoName(), distCompArch.getDist(), distCompArch.getComponent(), distCompArch.getArch());
		String packageFileContents = DebPackageToPackageDescriptionBuilder.buildPackageDescriptionList(packages);
		
		//DebPackagesFileModel.db().beginTransaction();
		
		
		updatePackagesFile(config, distCompArch, 
				packageFileContents.getBytes(StandardCharsets.UTF_8),
				"Packages");
		

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream(packageFileContents.length());
			GZIPOutputStream gzip = new GZIPOutputStream(out);
			gzip.write(packageFileContents.getBytes());
			gzip.close();
	
			updatePackagesFile(config, distCompArch, 
					out.toByteArray(),
					"Packages.gz");
		} catch (IOException e) {
			Loggers.SERVER.warn("DebRepositoryManagerImpl::updatePackagesFiles - Could not create GZ packages file");
		}
		//DebPackagesFileModel.db().commitTransaction();
	}

	private void updatePackagesFile(DebRepositoryConfiguration config, DistComponentArchitecture distCompArch,
			byte[] packageFileContents, String filename) throws NonExistantRepositoryException {
		
		/*
		DebPackagesFileModel packagesFile = DebPackagesFileModel.find
															.fetch("debPackagesHashes")
															.where()
															.eq("repository.uuid", config.getUuid().toString())
															.eq("dist", distCompArch.getDist())
															.eq("component", distCompArch.getComponent())
															.eq("arch", distCompArch.getArch())
															.eq("packagesFileName", filename)
															.findUnique();
		*/													
		DebPackagesFileModel packagesFile = new DebPackagesFileModel();
		packagesFile.setRepository(findRepository(config.getUuid()));
		packagesFile.setDist(distCompArch.getDist());
		packagesFile.setComponent(distCompArch.getComponent());
		packagesFile.setArch(distCompArch.getArch());
		packagesFile.setPackagesFileName(filename);
		packagesFile.setPath(distCompArch.getDist() 
								+ "/" + distCompArch.getComponent() 
								+ "/binary-" + distCompArch.getArch() 
								+ "/" + filename);
		
		packagesFile.setPackagesFile(packageFileContents);
		packagesFile.setMd5(DigestUtils.md5Hex(packageFileContents));
		packagesFile.setSha1(DigestUtils.sha1Hex(packageFileContents));
		packagesFile.setSha256(DigestUtils.sha256Hex(packageFileContents));
		
		packagesFile.save();
	}
	
	
	
	@AllArgsConstructor @Value
	public static class DistComponentArchImpl implements DistComponentArchitecture {
		String dist;
		String component;
		String arch;
	}

	@Override
	public String findReleaseFile(String reponame, String dist, ReleaseFileType releaseFileType)
			throws NonExistantRepositoryException, DebRepositoryItemNotFoundException {
		if (!isExistingRepository(reponame)) {
			throw new NonExistantRepositoryException();
		}
		
		DebReleaseFileModel releaseFile = DebReleaseFileModel.find.query().where()
																  .eq("repository.name", reponame)
																  .eq("dist", dist)
																  .findOne();
		if (releaseFile == null) {
			throw new DebRepositoryItemNotFoundException("Unable to find Release file " + releaseFileType.toString() + " for Repo " + reponame);
		}
		
		switch (releaseFileType) {
		case ReleaseGpg:
			return releaseFile.getReleaseFileGpg();
		case InRelease:
			return releaseFile.getInReleaseFile();
		case Release:
		default: 
			return releaseFile.getReleaseFile();
		}
	}

	@Override
	public String findReleaseFile(String reponame, String dist, String component, String architecture, ReleaseFileType releaseFileType)
			throws NonExistantRepositoryException, DebRepositoryItemNotFoundException {
		if (!isExistingRepository(reponame)) {
			throw new NonExistantRepositoryException();
		}
		
		DebReleaseFileSimpleModel releaseFile = DebReleaseFileSimpleModel.find.query().where()
																  .eq("repository.name", reponame)
																  .eq("dist", dist)
																  .eq("component", component)
																  .eq("arch", architecture)
																  .order("modifiedTime desc")
																  .setMaxRows(1)
																  .findOne();
		if (releaseFile == null) {
			throw new DebRepositoryItemNotFoundException("Unable to find Release file " + releaseFileType.toString() + " for Repo " + reponame);
		}
		
		return releaseFile.getReleaseFile();
	}

	@Override
	public byte[] findPackagesFile(String reponame, PackagesFileType packagesFileType, String dist,
			String component, String architecture) throws NonExistantRepositoryException, DebRepositoryItemNotFoundException {
		
		if (!isExistingRepository(reponame)) {
			throw new NonExistantRepositoryException();
		}

		DebPackagesFileModel packagesFile = DebPackagesFileModel.find.query().where()
																	 .eq("repository.name", reponame)
																	 .eq("dist", dist)
																	 .eq("component", component)
																	 .eq("arch", architecture)
																	 .eq("packagesFileName", packagesFileType.getFilename())
																	 .order("modifiedTime desc")
																	 .setMaxRows(1)
																	 .findOne();
		if (packagesFile == null) {
			throw new DebRepositoryItemNotFoundException("Unable to find packagesFile file " + packagesFileType.toString() + " for Repo " + reponame);
		}
		
		return packagesFile.getPackagesFile();
	}

	@Override
	public void updateRepositoryMetaData(DebRepositoryConfiguration config)
			throws NonExistantRepositoryException, DebRepositoryPersistanceException {
		final DebRepositoryModel repositoryModel = findRepository(config.getUuid());
		rebuildSimpleReleaseFiles(config, repositoryModel, getDistinctDistComponentArch(config));
		
	}
	
}
