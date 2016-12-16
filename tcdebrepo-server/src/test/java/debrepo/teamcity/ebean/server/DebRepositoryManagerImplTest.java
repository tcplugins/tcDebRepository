package debrepo.teamcity.ebean.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.service.DebRepositoryBaseTest;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebRepositoryManagerImplTest extends DebRepositoryBaseTest {
	
	@Mock ServerPaths serverPaths;
	PluginDataResolver pluginDataResolver;
	
	EbeanServerProvider ebeanServerProvider;
	DebRepositoryManager debRepositoryManager;
	
	@Before
	public void setuplocal() {
		MockitoAnnotations.initMocks(this);
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		
		pluginDataResolver = new PluginDataResolverImpl(serverPaths);
		ebeanServerProvider = new EbeanServerProvider(pluginDataResolver);
		debRepositoryManager = new DebRepositoryManagerImpl(ebeanServerProvider.getEbeanServer(), debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		debRepositoryConfigManager = (DebRepositoryConfigurationManager) debRepositoryManager;
		
		DebRepositoryConfiguration c = getDebRepoConfig1();
		c.setRepoName("blahBlah01");
		debRepositoryConfigManager.addDebRepository(c);
		
		debRepositoryManager.addBuildPackage(c, entity);
		debRepositoryManager.addBuildPackage(c, entity2);
		debRepositoryManager.addBuildPackage(c, entity3);
		debRepositoryManager.addBuildPackage(c, entity4);
		debRepositoryManager.addBuildPackage(c, entity);
		debRepositoryManager.addBuildPackage(c, entity2);
		debRepositoryManager.addBuildPackage(c, entity3);
		debRepositoryManager.addBuildPackage(c, entity4);
		debRepositoryManager.addBuildPackage(c, entity);
		debRepositoryManager.addBuildPackage(c, entity2);
		debRepositoryManager.addBuildPackage(c, entity3);
		debRepositoryManager.addBuildPackage(c, entity4);
		debRepositoryManager.addBuildPackage(c, entity);
		debRepositoryManager.addBuildPackage(c, entity2);
		debRepositoryManager.addBuildPackage(c, entity3);
		debRepositoryManager.addBuildPackage(c, entity4);
		debRepositoryManager.addBuildPackage(c, entity);
		debRepositoryManager.addBuildPackage(c, entity2);
		debRepositoryManager.addBuildPackage(c, entity3);
		debRepositoryManager.addBuildPackage(c, entity4);
		debRepositoryManager.addBuildPackage(c, entity);
		debRepositoryManager.addBuildPackage(c, entity2);
		debRepositoryManager.addBuildPackage(c, entity3);
		debRepositoryManager.addBuildPackage(c, entity4);
		
		System.out.println(debRepositoryManager.getRepositoryStatistics(c, "myUrl").getTotalPackageCount());
		assertTrue(debRepositoryManager.getRepositoryStatistics(c, "myUrl").getTotalPackageCount() == 24);
	}

	@Test @Ignore
	public void testGetRepositoryStatisticsStringString() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetRepositoryStatisticsDebRepositoryConfigurationString() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testAddBuildPackage() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testFindUniqueArchByDistAndComponent() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testFindUniqueComponentByDist() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testFindUniqueDist() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testFindUniqueComponent() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testFindUniquePackageNameByComponent() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUniquePackagesByComponentAndPackageName() throws NonExistantRepositoryException {

		List<? extends DebPackage> packages = debRepositoryManager.getUniquePackagesByComponentAndPackageName("blahBlah01", "main", "testpackage");
		for (DebPackage d : packages) {
			System.out.println(d.getFilename() + " :: " + d.getUri());
		}
		assertEquals(3, packages.size());
		
	}

	@Test @Ignore
	public void testFindAllByDistComponentArch() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testFindByUri() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testIsExistingRepository() {
		fail("Not yet implemented");
	}

}
