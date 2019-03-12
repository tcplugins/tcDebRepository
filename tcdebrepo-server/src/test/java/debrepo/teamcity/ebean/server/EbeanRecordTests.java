/*******************************************************************************
 * Copyright 2017 Net Wolf UK
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
package debrepo.teamcity.ebean.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.archive.DebFileReader;
import debrepo.teamcity.archive.DebFileReaderFactory;
import debrepo.teamcity.ebean.DebFileModel;
import debrepo.teamcity.ebean.DebPackageModel;
import debrepo.teamcity.ebean.DebPackageParameterModel;
import debrepo.teamcity.ebean.DebRepositoryModel;
import debrepo.teamcity.ebean.server.EbeanServerProvider;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebPackageStoreEntity;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.helper.DebRepositoryDatabaseJaxHelperImpl;
import debrepo.teamcity.entity.helper.DebRepositoryDatabaseXmlPersisterImpl;
import debrepo.teamcity.entity.helper.DebRepositoryToReleaseDescriptionBuilder;
import debrepo.teamcity.entity.helper.JaxDbFileRenamer;
import debrepo.teamcity.entity.helper.JaxHelper;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.entity.helper.ReleaseDescriptionBuilder;
import debrepo.teamcity.entity.helper.XmlPersister;
import debrepo.teamcity.service.DebFileBuildArtifactsProcessorFactory;
import debrepo.teamcity.service.DebPackageFactory;
import debrepo.teamcity.service.DebReleaseFileGenerator;
import debrepo.teamcity.service.DebRepositoryBuildArtifactsCleaner;
import debrepo.teamcity.service.DebRepositoryBuildArtifactsCleanerImpl;
import debrepo.teamcity.service.DebRepositoryBuildArtifactsPublisher;
import debrepo.teamcity.service.DebRepositoryBuildArtifactsPublisherImpl;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationFactoryImpl;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryConfigurationManager.DebRepositoryActionResult;
import debrepo.teamcity.service.DebRepositoryMaintenanceManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.DebRepositoryManager.DebPackageRemovalBean;
import debrepo.teamcity.service.DebRepositoryPersistanceException;
import debrepo.teamcity.service.NonExistantRepositoryException;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts.BuildArtifactsProcessor;

public class EbeanRecordTests {
	
	private static final Logger logger = LogManager.getLogger(EbeanRecordTests.class);
	@Mock ServerPaths jaxServerPaths, ebeanServerPaths;
	PluginDataResolver jaxPluginDataResolver, ebeanPluginDataResolver;
	@Mock protected ProjectManager projectManager;
	@Mock protected SProject project01, project02;
	
	JaxHelper<DebPackageStoreEntity> jaxHelper = new DebRepositoryDatabaseJaxHelperImpl();
	XmlPersister<DebPackageStore, DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister;
	@Mock protected DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister;
	@Mock JaxDbFileRenamer jaxDbFileRenamer;
	
	EbeanServerProvider ebeanServerProvider;
	DebRepositoryManager jaxDebRepositoryManager, ebeanDebRepositoryManager;
	DebRepositoryConfigurationJaxImpl config;
	
	@Mock SBuild build;
	@Mock SBuildType sBuildType;
	@Mock BuildArtifact buildArtifact;
	@Mock BuildArtifacts buildArtifacts;
	ReleaseDescriptionBuilder releaseDescriptionBuilder;
	
	
	protected DebRepositoryMaintenanceManager debRepositoryMaintenanceManager;
	protected DebRepositoryConfigurationManager debRepositoryConfigManager;
	protected DebReleaseFileGenerator debReleaseFileGenerator;
	protected DebRepositoryConfigurationFactory debRepositoryConfigurationFactory = new DebRepositoryConfigurationFactoryImpl();
	
	@Before
	public void setuplocal() throws NonExistantRepositoryException, IOException {
		MockitoAnnotations.initMocks(this);
		when(jaxServerPaths.getPluginDataDirectory()).thenReturn(new File("src/test/resources/testplugindata"));
		when(ebeanServerPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		when(projectManager.findProjectById("project01")).thenReturn(project01);
		when(projectManager.findProjectById("project02")).thenReturn(project02);
		when(project01.getExternalId()).thenReturn("My_Project_Name");
		when(project01.getDescription()).thenReturn("My Project Name - Long description");
		
		when(project02.getExternalId()).thenReturn("My_Project_Name_2");
		when(project02.getDescription()).thenReturn("My Project Name 2 - Long description");
		
		releaseDescriptionBuilder = new DebRepositoryToReleaseDescriptionBuilder(projectManager);
		
		jaxPluginDataResolver = new PluginDataResolverImpl(jaxServerPaths);
		ebeanPluginDataResolver = new PluginDataResolverImpl(ebeanServerPaths);
		ebeanServerProvider = new EbeanServerProvider(ebeanPluginDataResolver);
		
		debRepositoryDatabaseXmlPersister = new DebRepositoryDatabaseXmlPersisterImpl(jaxPluginDataResolver, jaxHelper);
		jaxDebRepositoryManager = new debrepo.teamcity.service.DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		ebeanDebRepositoryManager = new debrepo.teamcity.ebean.server.DebRepositoryManagerImpl(ebeanServerProvider.getEbeanServer(), debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister, releaseDescriptionBuilder);
		debRepositoryConfigManager = (DebRepositoryConfigurationManager) ebeanDebRepositoryManager;
		debRepositoryMaintenanceManager = (DebRepositoryMaintenanceManager) ebeanDebRepositoryManager;
		debReleaseFileGenerator = (DebReleaseFileGenerator) ebeanDebRepositoryManager;
		
		config = new DebRepositoryConfigurationJaxImpl("project01", "MyStoreName");
		config.setUuid(UUID.fromString("a187bd92-b22d-43ea-98ce-55ec2cedb942"));
		debRepositoryConfigManager.addDebRepository(config);
		jaxDebRepositoryManager.initialisePackageStore(config);
		JaxToEbeanMigrator migrator = new JaxToEbeanMigrator(jaxDebRepositoryManager, ebeanDebRepositoryManager, debReleaseFileGenerator, jaxDbFileRenamer);
		migrator.migrate(config);
	}

	@Test
	public void testFindByDistComponentArch() throws NonExistantRepositoryException {
		
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "amd64").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "all").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "wily", "main", "i386").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "wily", "main", "amd64").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "potato", "stable", "amd64").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "potato", "stable", "i386").size());
		
	}
	
	@Test
	public void testFindByDistComponentArchIncludingAll() throws NonExistantRepositoryException {
		
		assertEquals(2, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "potato", "main", "i386").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "potato", "stable", "i386").size());
		
	}
	
	@Test
	public void testCleaner() throws NonExistantRepositoryException {
		assertEquals(0, debRepositoryMaintenanceManager.getDanglingFileCount());
		
		DebRepositoryBuildArtifactsCleaner cleaner = new DebRepositoryBuildArtifactsCleanerImpl((DebRepositoryMaintenanceManager) ebeanDebRepositoryManager);
		cleaner.removeDetachedDebFilesFromRepositories();
		
		assertEquals(2, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "potato", "main", "i386").size());
		
		ebeanDebRepositoryManager.removeBuildPackages(new DebPackageRemovalBean(config, "bt25", 3221L, new ArrayList<DebPackage>()));
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(2, debRepositoryMaintenanceManager.getDanglingFileCount());

		cleaner.removeDetachedDebFilesFromRepositories();
		assertEquals(0, debRepositoryMaintenanceManager.getDanglingFileCount());
	}
	
	@Test
	public void testRemove() throws NonExistantRepositoryException {
		
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "amd64").size());
		ebeanDebRepositoryManager.removeBuildPackages(new DebPackageRemovalBean(config, "bt25", 3221L, new ArrayList<DebPackage>()));
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "amd64").size());
	}
	
	@Test
	public void testDeleteRepository() throws NonExistantRepositoryException {
		
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "amd64").size());
		DebRepositoryActionResult result = debRepositoryConfigManager.removeDebRespository(config);
		assertFalse(result.isError());
	}
	
	@Test
	public void testRemoveFromLargeDB() throws NonExistantRepositoryException {
		DebRepositoryConfigurationJaxImpl config2 = new DebRepositoryConfigurationJaxImpl("project01", "MyStoreName2");
		config2.setUuid(UUID.fromString("eafee234-c753-4a7b-9221-6b208eac4ab6"));
		debRepositoryConfigManager.addDebRepository(config2);
		jaxDebRepositoryManager.initialisePackageStore(config2);
		JaxToEbeanMigrator migrator = new JaxToEbeanMigrator(jaxDebRepositoryManager, ebeanDebRepositoryManager, debReleaseFileGenerator, jaxDbFileRenamer);
		migrator.migrate(config2);
		
		assertEquals(1217, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName2", "jessie", "main", "i386").size());
		assertEquals(1217, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName2", "jessie", "main", "amd64").size());
		ebeanDebRepositoryManager.removeBuildPackages(new DebPackageRemovalBean(config2, "bt25", 3221L, new ArrayList<DebPackage>()));
		assertEquals(1216, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName2", "jessie", "main", "i386").size());
		assertEquals(1216, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName2", "jessie", "main", "amd64").size());
		ebeanDebRepositoryManager.removeBuildPackages(new DebPackageRemovalBean(config2, "bt25", 3183L, new ArrayList<DebPackage>()));
		assertEquals(1215, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName2", "jessie", "main", "i386").size());
		assertEquals(1215, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName2", "jessie", "main", "amd64").size());
		ebeanDebRepositoryManager.removeBuildPackages(new DebPackageRemovalBean(config2, "bt25", 3180L, new ArrayList<DebPackage>()));
		assertEquals(1214, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName2", "jessie", "main", "i386").size());
		assertEquals(1214, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName2", "jessie", "main", "amd64").size());
		
		assertFalse(debRepositoryConfigManager.removeDebRespository(config2).isError());
		
	}
	
	@Test
	public void testMultipleRepositoriesAndThenRemoveOneRepository() throws NonExistantRepositoryException {
		logger.info("DebRepositoryModel count:" + DebRepositoryModel.getFind().findCount());
		logger.info("DebPackageModel count:" + DebPackageModel.getFind().findCount());
		logger.info("DebFileModel count:" + DebFileModel.getFind().findCount());
		logger.info("DebPackageParameterModel count:" + DebPackageParameterModel.getFind().findCount());
		
		DebRepositoryConfigurationJaxImpl config3 = new DebRepositoryConfigurationJaxImpl("project02", "MyStoreForDeletion");
		config3.setUuid(UUID.fromString("dd7824da-c0c2-4895-b0e3-b8af7a05dafe"));
		debRepositoryConfigManager.addDebRepository(config3);
		jaxDebRepositoryManager.initialisePackageStore(config3);
		JaxToEbeanMigrator migrator = new JaxToEbeanMigrator(jaxDebRepositoryManager, ebeanDebRepositoryManager, debReleaseFileGenerator, jaxDbFileRenamer);
		migrator.migrate(config3);

		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "amd64").size());
		assertEquals(2, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreForDeletion", "jessie", "main", "i386").size());
		assertEquals(2, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreForDeletion", "jessie", "main", "amd64").size());
		
		logger.info("DebRepositoryModel count:" + DebRepositoryModel.getFind().findCount());
		logger.info("DebPackageModel count:" + DebPackageModel.getFind().findCount());
		logger.info("DebFileModel count:" + DebFileModel.getFind().findCount());
		logger.info("DebPackageParameterModel count:" + DebPackageParameterModel.getFind().findCount());
		
		DebRepositoryActionResult result = debRepositoryConfigManager.removeDebRespository(config3);
		assertFalse(result.isError());
		
		logger.info("DebRepositoryModel count:" + DebRepositoryModel.getFind().findCount());
		logger.info("DebPackageModel count:" + DebPackageModel.getFind().findCount());
		logger.info("DebFileModel count:" + DebFileModel.getFind().findCount());
		logger.info("DebPackageParameterModel count:" + DebPackageParameterModel.getFind().findCount());
		
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "amd64").size());

	}
	
	@Test
	public void testRemoveDanglingDebFileModelRows() throws NonExistantRepositoryException, DebRepositoryPersistanceException {
		logger.info("DebRepositoryModel count:" + DebRepositoryModel.getFind().findCount());
		logger.info("DebPackageModel count:" + DebPackageModel.getFind().findCount());
		logger.info("DebFileModel count:" + DebFileModel.getFind().findCount());
		logger.info("DebPackageParameterModel count:" + DebPackageParameterModel.getFind().findCount());
		
		DebRepositoryConfigurationJaxImpl config3 = new DebRepositoryConfigurationJaxImpl("project02", "MyStoreForDeletion");
		config3.setUuid(UUID.fromString("dd7824da-c0c2-4895-b0e3-b8af7a05dafe"));
		debRepositoryConfigManager.addDebRepository(config3);
		jaxDebRepositoryManager.initialisePackageStore(config3);
		JaxToEbeanMigrator migrator = new JaxToEbeanMigrator(jaxDebRepositoryManager, ebeanDebRepositoryManager, debReleaseFileGenerator, jaxDbFileRenamer);
		migrator.migrate(config3);

		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "amd64").size());
		assertEquals(2, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreForDeletion", "jessie", "main", "i386").size());
		assertEquals(2, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreForDeletion", "jessie", "main", "amd64").size());
		
		logger.info("DebRepositoryModel count:" + DebRepositoryModel.getFind().findCount());
		logger.info("DebPackageModel count:" + DebPackageModel.getFind().findCount());
		logger.info("DebFileModel count:" + DebFileModel.getFind().findCount());
		logger.info("DebPackageParameterModel count:" + DebPackageParameterModel.getFind().findCount());
		
		DebRepositoryActionResult result = debRepositoryConfigManager.removeDebRespository(config);
		assertFalse(result.isError());
		result = debRepositoryConfigManager.removeDebRespository(config3);
		assertFalse(result.isError());
		
		logger.info("DebRepositoryModel count:" + DebRepositoryModel.getFind().findCount());
		logger.info("DebPackageModel count:" + DebPackageModel.getFind().findCount());
		logger.info("DebFileModel count:" + DebFileModel.getFind().findCount());
		logger.info("DebPackageParameterModel count:" + DebPackageParameterModel.getFind().findCount());
		
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "amd64").size());
	
		assertEquals(5, debRepositoryMaintenanceManager.getDanglingFileCount());
		debRepositoryMaintenanceManager.removeDanglingFiles();
		assertEquals(0, debRepositoryMaintenanceManager.getDanglingFileCount());
		
		logger.info("DebRepositoryModel count:" + DebRepositoryModel.getFind().findCount());
		logger.info("DebPackageModel count:" + DebPackageModel.getFind().findCount());
		logger.info("DebFileModel count:" + DebFileModel.getFind().findCount());
		logger.info("DebPackageParameterModel count:" + DebPackageParameterModel.getFind().findCount());
		
	}
	
	@Test
	public void testAddingPackageToDbWithIdenticalRulesAndReturningSinglePackage() throws NonExistantRepositoryException {
		when(build.isArtifactsExists()).thenReturn(true);
		when(build.getBuildTypeId()).thenReturn("bt1");
		when(build.getBuildType()).thenReturn(sBuildType);
		when(sBuildType.getBuildTypeId()).thenReturn("bt1");
		
		DebRepositoryConfigurationJaxImpl config3 = new DebRepositoryConfigurationJaxImpl("project02", "MyStore03");
		config3.addBuildType(new DebRepositoryBuildTypeConfig("bt1", "jessie", "main", ".+\\.deb$"));
		config3.addBuildType(new DebRepositoryBuildTypeConfig("bt1", "jessie", "main", ".+\\.deb$"));
		debRepositoryConfigManager.addDebRepository(config3);
		DebRepositoryBuildArtifactsPublisher publisher =  new DebRepositoryBuildArtifactsPublisherImpl(ebeanDebRepositoryManager, debRepositoryConfigManager, new DebFileReaderFactory() {
			
			@Override
			public DebFileReader createFileReader(SBuild build) {
				return new DebFileReaderMock();
			}
		}, new DebFileBuildArtifactsProcessorFactory() {
			
			@Override
			public BuildArtifactsProcessor getBuildArtifactsProcessor(SBuild build, List<DebPackage> entities) {
				return new BuildArtifactsProcessorMock(entities, build, "dist/e3_2.71_amd64.deb");
			}
		});
		publisher.addArtifactsToRepositories(build, buildArtifacts);
		assertEquals(1, ebeanDebRepositoryManager.getUniquePackagesByComponentAndPackageName("MyStore03", "main", "e3").size());
	}
	
	public static class DebFileReaderMock implements DebFileReader {
		
		@Override
		public Map<String, String> getMetaDataFromPackage(String filename) throws IOException {
			Map<String,String> params = new TreeMap<>();
			
			params.put("Package","e3");
			params.put("Version","2.71-1");
			params.put("Architecture", "amd64");
			params.put("Maintainer","Paweł Więcek <coven@debian.org>");
			params.put("Installed-Size","120");
			params.put("Section","editors");
			params.put("Priority","optional");
			params.put("Homepage","http://mitglied.lycos.de/albkleine/");
			params.put("Description","A very small editor");
			
			return params;
		}
		
		@Override
		public boolean fileExists(String filename) {
			return true;
		}
	}
	
	public static class BuildArtifactsProcessorMock implements BuildArtifactsProcessor {
		
		public BuildArtifactsProcessorMock(List<DebPackage> entities, SBuild myBuild, String filename) {
			entities.add(DebPackageFactory.buildFromArtifact(myBuild, filename));
		}

		@Override
		public Continuation processBuildArtifact(BuildArtifact artifact) {
			return Continuation.BREAK;
		}
		
	}
	
}
