package debrepo.teamcity.ebean.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.ebean.server.EbeanServerProvider;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebPackageStoreEntity;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.helper.DebRepositoryDatabaseJaxHelperImpl;
import debrepo.teamcity.entity.helper.DebRepositoryDatabaseXmlPersisterImpl;
import debrepo.teamcity.entity.helper.JaxDbFileRenamer;
import debrepo.teamcity.entity.helper.JaxHelper;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.entity.helper.XmlPersister;
import debrepo.teamcity.service.DebRepositoryBuildArtifactsCleaner;
import debrepo.teamcity.service.DebRepositoryBuildArtifactsCleanerImpl;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationFactoryImpl;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryConfigurationManager.DebRepositoryActionResult;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.DebRepositoryManager.DebPackageRemovalBean;
import debrepo.teamcity.service.NonExistantRepositoryException;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.ServerPaths;

public class EbeanRecordTests {
	
	
	@Mock ServerPaths jaxServerPaths, ebeanServerPaths;
	PluginDataResolver jaxPluginDataResolver, ebeanPluginDataResolver;
	@Mock protected ProjectManager projectManager;
	
	JaxHelper<DebPackageStoreEntity> jaxHelper = new DebRepositoryDatabaseJaxHelperImpl();
	XmlPersister<DebPackageStore, DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister;
	@Mock protected DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister;
	@Mock JaxDbFileRenamer jaxDbFileRenamer;
	
	EbeanServerProvider ebeanServerProvider;
	DebRepositoryManager jaxDebRepositoryManager, ebeanDebRepositoryManager;
	DebRepositoryConfigurationJaxImpl config;
	
	protected DebRepositoryConfigurationManager debRepositoryConfigManager;
	protected DebRepositoryConfigurationFactory debRepositoryConfigurationFactory = new DebRepositoryConfigurationFactoryImpl();
	
	@Before
	public void setuplocal() throws NonExistantRepositoryException, IOException {
		MockitoAnnotations.initMocks(this);
		when(jaxServerPaths.getPluginDataDirectory()).thenReturn(new File("src/test/resources/testplugindata"));
		when(ebeanServerPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		
		jaxPluginDataResolver = new PluginDataResolverImpl(jaxServerPaths);
		ebeanPluginDataResolver = new PluginDataResolverImpl(ebeanServerPaths);
		ebeanServerProvider = new EbeanServerProvider(ebeanPluginDataResolver);
		
		debRepositoryDatabaseXmlPersister = new DebRepositoryDatabaseXmlPersisterImpl(jaxPluginDataResolver, jaxHelper);
		jaxDebRepositoryManager = new debrepo.teamcity.service.DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		ebeanDebRepositoryManager = new debrepo.teamcity.ebean.server.DebRepositoryManagerImpl(ebeanServerProvider.getEbeanServer(), debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		debRepositoryConfigManager = (DebRepositoryConfigurationManager) ebeanDebRepositoryManager;
		
		config = new DebRepositoryConfigurationJaxImpl("project01", "MyStoreName");
		config.setUuid(UUID.fromString("a187bd92-b22d-43ea-98ce-55ec2cedb942"));
		debRepositoryConfigManager.addDebRepository(config);
		jaxDebRepositoryManager.initialisePackageStore(config);
		JaxToEbeanMigrator migrator = new JaxToEbeanMigrator(jaxDebRepositoryManager, ebeanDebRepositoryManager, jaxDbFileRenamer);
		migrator.migrate(config);
	}

	@Test
	public void testFindByDistComponentArch() throws NonExistantRepositoryException {
		
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "amd64").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "jessie", "main", "all").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "wily", "main", "i386").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "wily", "main", "amd64").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "potato", "stable", "amd64").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArch("MyStoreName", "potato", "stable", "i386").size());
		
	}
	
	@Test
	public void testFindByDistComponentArchIncludingAll() throws NonExistantRepositoryException {
		
		assertEquals(2, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "potato", "main", "i386").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "potato", "stable", "i386").size());
		
	}
	
	@Test @Ignore
	public void testCleaner() throws NonExistantRepositoryException {
		
		assertEquals(2, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "jessie", "main", "i386").size());
		assertEquals(0, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "potato", "main", "i386").size());
		assertEquals(1, ebeanDebRepositoryManager.findAllByDistComponentArchIncludingAll("MyStoreName", "potato", "stable", "i386").size());
		
		DebRepositoryBuildArtifactsCleaner cleaner = new DebRepositoryBuildArtifactsCleanerImpl(projectManager, debRepositoryConfigManager, ebeanDebRepositoryManager);
		cleaner.removeDetachedArtifactsFromRepositories();
		fail("Not implemented yet");
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
		JaxToEbeanMigrator migrator = new JaxToEbeanMigrator(jaxDebRepositoryManager, ebeanDebRepositoryManager, jaxDbFileRenamer);
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
	
	

}
