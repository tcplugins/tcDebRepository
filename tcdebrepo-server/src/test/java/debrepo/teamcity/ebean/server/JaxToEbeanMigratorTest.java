package debrepo.teamcity.ebean.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.ebean.server.EbeanServerProvider;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebPackageStoreEntity;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.helper.DebRepositoryDatabaseJaxHelperImpl;
import debrepo.teamcity.entity.helper.DebRepositoryDatabaseXmlPersisterImpl;
import debrepo.teamcity.entity.helper.JaxHelper;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.entity.helper.XmlPersister;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationFactoryImpl;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.ServerPaths;

public class JaxToEbeanMigratorTest {
	
	
	@Mock ServerPaths jaxServerPaths, ebeanServerPaths;
	PluginDataResolver jaxPluginDataResolver, ebeanPluginDataResolver;
	@Mock protected ProjectManager projectManager;
	
	JaxHelper<DebPackageStoreEntity> jaxHelper = new DebRepositoryDatabaseJaxHelperImpl();
	XmlPersister<DebPackageStore, DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister;
	@Mock protected DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister;
	
	EbeanServerProvider ebeanServerProvider;
	DebRepositoryManager jaxDebRepositoryManager, ebeanDebRepositoryManager;
	
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
		
	}

	@Test
	public void testMigrate() throws NonExistantRepositoryException {
		DebRepositoryConfigurationJaxImpl c = new DebRepositoryConfigurationJaxImpl("project01", "MyStoreName");
		c.setUuid(UUID.fromString("eafee234-c753-4a7b-9221-6b208eac4ab6"));
		debRepositoryConfigManager.addDebRepository(c);
		jaxDebRepositoryManager.initialisePackageStore(c);
		JaxToEbeanMigrator migrator = new JaxToEbeanMigrator(jaxDebRepositoryManager, ebeanDebRepositoryManager);
		migrator.migrate(c);
	}

}
