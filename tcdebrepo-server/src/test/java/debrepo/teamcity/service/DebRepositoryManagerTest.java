package debrepo.teamcity.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.*;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
//import debrepo.teamcity.ebean.DebPackageModel;
//import debrepo.teamcity.ebean.DebRepositoryModel;
import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageNotFoundInStoreException;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurations;
import debrepo.teamcity.entity.DebRepositoryStatistics;
import debrepo.teamcity.entity.helper.DebRepositoryConfigurationJaxHelperImpl;
import debrepo.teamcity.entity.helper.JaxHelper;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.service.DebRepositoryManager.DebPackageRemovalBean;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.ServerPaths;

public abstract class DebRepositoryManagerTest {
	
	final protected String BUILD_TYPE_ID_BT01 = "bt01";
	final protected String BUILD_TYPE_ID_BT02 = "bt02";
	final protected String BUILD_TYPE_ID_BT03 = "bt03";
	
	public abstract DebRepositoryManager getDebRepositoryManager();
	public abstract DebRepositoryConfigurationManager getDebRepositoryConfigurationManager();
	public abstract void setupLocal() throws Exception;
	public abstract void testPersist() throws IOException;
	
	DebRepositoryManager debRepositoryManager;
	protected ProjectManager projectManager;
	protected DebRepositoryConfigurationFactory debRepositoryConfigurationFactory = new DebRepositoryConfigurationFactoryImpl();
	DebRepositoryConfigurationManager debRepositoryConfigManager;

	@Mock protected SBuildType bt01;
	@Mock protected SBuild build01;
	@Mock protected SBuildType bt02;
	@Mock protected SBuild build02;
	@Mock protected SBuildType bt03;
	@Mock protected SBuild build03;
	@Mock protected DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister;
	@Mock ServerPaths serverPaths;
	
	PluginDataResolver pathResolver;

	JaxHelper<DebRepositoryConfigurations> configJaxHelper= new DebRepositoryConfigurationJaxHelperImpl();
	DebRepositoryConfigurations readConfig;
	DebPackageEntity entity, entity02, entity03, entity04;

	
	@Before
	public void setup() throws Exception {
		System.out.println("Runing setup()");
		MockitoAnnotations.initMocks(this);
		
		pathResolver = new PluginDataResolverImpl(serverPaths);
		when(serverPaths.getConfigDir()).thenReturn("src/test/resources/repo-configs");

		when(bt01.getProjectId()).thenReturn("project01");
		when(bt01.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT01);
		when(build01.getBuildType()).thenReturn(bt01);
		when(build01.getBuildId()).thenReturn(12345L);
		
		when(bt02.getProjectId()).thenReturn("project01");
		when(bt02.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT02);
		when(build02.getBuildType()).thenReturn(bt02);
		when(build02.getBuildId()).thenReturn(12346L);
		
		readConfig = configJaxHelper.read(pathResolver.getPluginConfigurationFile());
		getDebRepositoryManager();
		setupLocal();
		getDebRepositoryConfigurationManager().updateRepositoryConfigurations(readConfig);
		
		entity = new DebPackageEntity();
		entity.setPackageName("testpackage");
		entity.setVersion("1.2.3.4");
		entity.setArch("i386");
		entity.setDist("wheezy");
		entity.setComponent("main");
		entity.setFilename("testpackage-i386-1.2.3.4.deb");
		entity.setBuildTypeId(BUILD_TYPE_ID_BT01);
		entity.setBuildId(build01.getBuildId());
		entity.setUri("ProjectName/BuildName/" + entity.getBuildId() + "/" + entity.getFilename());
		
		entity02 = new DebPackageEntity();
		entity02.setPackageName("testpackage");
		entity02.setVersion("1.2.3.5");
		entity02.setArch("i386");
		entity02.setDist("wheezy");
		entity02.setComponent("main");		
		entity02.setFilename("testpackage-i386-1.2.3.5.deb");
		entity02.setBuildTypeId(BUILD_TYPE_ID_BT02);
		entity02.setBuildId(build02.getBuildId());
		entity02.setUri("ProjectName/BuildName/" + entity02.getBuildId() + "/" + entity02.getFilename());
		
		entity03 = new DebPackageEntity();
		entity03.setPackageName("testpackage");
		entity03.setVersion("1.2.3.5");
		entity03.setArch("amd64");
		entity03.setDist("wheezy");
		entity03.setComponent("main");		
		entity03.setFilename("testpackage-amd64-1.2.3.5.deb");
		entity03.setBuildTypeId(BUILD_TYPE_ID_BT02);
		entity03.setBuildId(build02.getBuildId());
		entity03.setUri("ProjectName/BuildName/" + entity03.getBuildId() + "/" + entity03.getFilename());
		
		entity04 = new DebPackageEntity();
		entity04.setPackageName("anotherpackage");
		entity04.setVersion("1.5");
		entity04.setArch("amd64");
		entity04.setDist("wheezy");
		entity04.setComponent("main");
		entity04.setFilename("testpackage-amd64-1.5.deb");
		entity04.setBuildTypeId(BUILD_TYPE_ID_BT03);
		entity04.setBuildId(build03.getBuildId());
		entity04.setUri("ProjectName/BuildName/" + entity04.getBuildId() + "/" + entity04.getFilename());
			
		
	}

	/* These three methods are local to the XML implmentation.
	@Test
	public void testGetPackageStore() {
		fail("Not yet implemented");
	}

	@Test
	public void testInitialisePackageStore() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPackageStoresForBuildType() {
		fail("Not yet implemented");
	} */



	@Test
	public void testGetRepositoryStatisticsStringString() {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		getDebRepositoryManager().addBuildPackage(config, entity);
		getDebRepositoryManager().addBuildPackage(config, entity02);
		
		DebRepositoryStatistics stats = getDebRepositoryManager().getRepositoryStatistics(config.getUuid().toString(), "something");
		assertEquals(3, (int)stats.getTotalFilterCount());
		assertEquals(2, (int)stats.getTotalPackageCount());
	}

	@Test
	public void testGetRepositoryStatisticsDebRepositoryConfigurationString() {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		getDebRepositoryManager().addBuildPackage(config, entity);
		getDebRepositoryManager().addBuildPackage(config, entity02);
		
		DebRepositoryStatistics stats = getDebRepositoryManager().getRepositoryStatistics(config, "somethingelse");
		assertEquals(3, (int)stats.getTotalFilterCount());
		assertEquals(2, (int)stats.getTotalPackageCount());
	}

	@Test
	public void testAddBuildPackages() throws NonExistantRepositoryException, DebPackageNotFoundInStoreException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "i386", "jessie", "main", "package_xyz_1.0_i386.deb", BUILD_TYPE_ID_BT01, 12345L));
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		DebPackage result = getDebRepositoryManager().findByUri(config.getRepoName(), packages.get(0).getUri());
		assertEquals("package_xyz_1.0_i386.deb", result.getFilename());
	}

	@Test
	public void testFindUniqueArchByDistAndComponent() throws NonExistantRepositoryException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "potato", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "potato", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "jessie", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "jessie", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "potato", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "potato", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageABC", "1.0", "i386",  "potato", "main", "package_abc_1.0_i386.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageABC", "1.0", "amd64", "potato", "main", "package_abc_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageABC", "1.0", "risc",  "potato", "main", "package_abc_1.0_risc.deb", BUILD_TYPE_ID_BT01, 12346L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		// This should return a Set<String> containing "i386" and "amd64" only
		Set<String> archs = getDebRepositoryManager().findUniqueArchByDistAndComponent(config.getRepoName(), "jessie", "main");
		assertEquals(2, archs.size());
		assertTrue(archs.contains("i386"));
		assertTrue(archs.contains("amd64"));
		assertFalse(archs.contains("powerpc"));
		
		// Should return 0 entries for invalid dist or component
		assertEquals(0, getDebRepositoryManager().findUniqueArchByDistAndComponent(config.getRepoName(), "wheezy", "main").size());
		assertEquals(0, getDebRepositoryManager().findUniqueArchByDistAndComponent(config.getRepoName(), "jessie", "development").size());
	}
	
	@Test
	public void testFindUniqueArchByDistAndComponentShouldReturnLotsOfArchWhenPackageIsAll() throws NonExistantRepositoryException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "all",   "jessie", "main", "package_xyz_1.0_no_arch.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "jessie", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "jessie", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		// This should return a Set<String> containing "i386" and "amd64" only
		Set<String> archs = getDebRepositoryManager().findUniqueArchByDistAndComponent(config.getRepoName(), "jessie", "main");
		assertEquals(11, archs.size());
		assertTrue(archs.contains("i386"));
		assertTrue(archs.contains("amd64"));
		assertTrue(archs.contains("powerpc"));
		assertTrue(archs.contains("ppc64el"));
	}
	
	@Test
	public void testFindUniqueArchByDistAndComponentShouldReturnLotsOfArchWhenOnlyPackageIsAll() throws NonExistantRepositoryException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "all",   "jessie", "main", "package_xyz_1.0_no_arch.deb",  BUILD_TYPE_ID_BT01, 12345L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		// This should return a Set<String> containing "i386" and "amd64" only
		Set<String> archs = getDebRepositoryManager().findUniqueArchByDistAndComponent(config.getRepoName(), "jessie", "main");
		assertEquals(11, archs.size());
		assertTrue(archs.contains("i386"));
		assertTrue(archs.contains("amd64"));
		assertTrue(archs.contains("powerpc"));
		assertTrue(archs.contains("ppc64el"));
	}

	@Test
	public void testFindUniqueComponentByDist() throws NonExistantRepositoryException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "contrib", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "contrib", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "non-free", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "non-free", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "potato", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "potato", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "jessie", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "jessie", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "potato", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "potato", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		Set<String> components = getDebRepositoryManager().findUniqueComponentByDist(config.getRepoName(), "jessie");
		assertEquals(3, components.size());
		assertTrue(components.contains("main"));
		assertTrue(components.contains("contrib"));
		assertTrue(components.contains("non-free"));
		
		assertEquals(0, getDebRepositoryManager().findUniqueComponentByDist(config.getRepoName(), "wheezy").size());
		assertEquals(1, getDebRepositoryManager().findUniqueComponentByDist(config.getRepoName(), "potato").size());
	}

	@Test
	public void testFindUniqueDist() throws NonExistantRepositoryException {
		
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "contrib", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "contrib", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "non-free", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "non-free", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "potato", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "potato", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "jessie", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "jessie", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "potato", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "potato", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.2", "all", "wheezy", "main", "package_xyz_1.2_all.deb", BUILD_TYPE_ID_BT01, 12349L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		Set<String> dists = getDebRepositoryManager().findUniqueDist(config.getRepoName());
		assertEquals(3, dists.size());
		assertTrue(dists.contains("jessie"));
		assertTrue(dists.contains("wheezy"));
		assertTrue(dists.contains("potato"));
		assertFalse(dists.contains("sid"));
	}

	@Test
	public void testFindUniqueComponent() throws NonExistantRepositoryException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "contrib", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "contrib", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "non-free", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "non-free", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "potato", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "potato", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "jessie", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "jessie", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "potato", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "potato", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		Set<String> components = getDebRepositoryManager().findUniqueComponent(config.getRepoName());
		assertEquals(3, components.size());
	}

	@Test
	public void testFindUniquePackageNameByComponent() throws NonExistantRepositoryException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "contrib", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "contrib", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "non-free", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "non-free", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "potato", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "potato", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "jessie", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "jessie", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "potato", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "potato", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("SomePackage", "1.10", "amd64", "potato", "main", "package_somepackage_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("SomePackage2", "1.10", "amd64", "potato", "contrib", "package_somepackage_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		Set<String> packageNames = getDebRepositoryManager().findUniquePackageNameByComponent(config.getRepoName(), "main");
		assertEquals(2, packageNames.size());
		assertTrue(packageNames.contains("PackageXYX"));
		assertTrue(packageNames.contains("SomePackage"));
	}

	@Test
	public void testFindAllByDistComponentArch() throws NonExistantRepositoryException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "contrib", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "contrib", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "non-free", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "non-free", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "potato", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "all", "potato", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "jessie", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "jessie", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "potato", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "potato", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("SomePackage", "1.10", "amd64", "potato", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("SomePackage2", "1.10", "amd64", "potato", "contrib", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		List<? extends DebPackage> foundPackages = getDebRepositoryManager().findAllByDistComponentArch(config.getRepoName(), "jessie", "contrib", "i386");
		assertEquals(1, foundPackages.size());
	}

	@Test
	public void testFindAllByDistComponentArchIncludingAll() throws NonExistantRepositoryException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "contrib", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "contrib", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "non-free", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "non-free", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "potato", "main", "package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "all", "potato", "main", "package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "jessie", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "jessie", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "i386",  "potato", "main", "package_xyz_1.1_i386.deb",  BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("PackageXYX", "1.1", "amd64", "potato", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("SomePackage", "1.10", "amd64", "potato", "main", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		packages.add(buildEntity("SomePackage2", "1.10", "amd64", "potato", "contrib", "package_xyz_1.1_amd64.deb", BUILD_TYPE_ID_BT01, 12346L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		List<? extends DebPackage> foundPackages = getDebRepositoryManager().findAllByDistComponentArchIncludingAll(config.getRepoName(), "jessie", "contrib", "i386");
		assertEquals(1, foundPackages.size());
		
		foundPackages = getDebRepositoryManager().findAllByDistComponentArchIncludingAll(config.getRepoName(), "jessie", "main", "i386");
		assertEquals(2, foundPackages.size());
	}

	@Test
	public void testGetUniquePackagesByComponentAndPackageName() throws NonExistantRepositoryException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "main", "i386/package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "main", "amd64/package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "contrib", "i386/package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "contrib", "amd64/package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "non-free", "i386/package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "non-free", "amd64/package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		List<? extends DebPackage> foundPackages = getDebRepositoryManager().getUniquePackagesByComponentAndPackageName(config.getRepoName(), "contrib", "PackageXYX");
		assertEquals(2, foundPackages.size());
		
	}

	@Test
	public void testFindByUri() throws NonExistantRepositoryException, DebPackageNotFoundInStoreException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "main", "i386/package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "main", "amd64/package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "contrib", "i386/package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "contrib", "amd64/package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "non-free", "i386/package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "non-free", "amd64/package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		DebPackage foundPackage = getDebRepositoryManager().findByUri(config.getRepoName(), packages.get(0).getUri());
		assertEquals(packages.get(0).getFilename(), foundPackage.getFilename());
	}

	@Test
	public void testIsExistingRepositoryString() {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		assertTrue(getDebRepositoryManager().isExistingRepository(config.getRepoName()));
		assertFalse(getDebRepositoryManager().isExistingRepository("ANonExistingName"));
	}

	@Test
	public void testIsExistingRepositoryUUID() {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		assertTrue(getDebRepositoryManager().isExistingRepository(config.getUuid()));
		assertFalse(getDebRepositoryManager().isExistingRepository(UUID.randomUUID()));	}

	@Test
	public void testRemoveRepository() throws NonExistantRepositoryException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		assertTrue(getDebRepositoryManager().isExistingRepository(config.getUuid()));
		getDebRepositoryManager().removeRepository(config.getUuid());
		assertFalse(getDebRepositoryManager().isExistingRepository(config.getUuid()));
	}

	@Test
	public void testRemoveBuildPackages() throws NonExistantRepositoryException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "main", "main/i386/package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "main", "main/amd64/package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "contrib", "contrib/i386/package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "contrib", "contrib/amd64/package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "non-free", "non-free/i386/package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		packages.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "non-free", "non-free/amd64/package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		List<DebPackage> packagesToKeep = new ArrayList<>();
		packagesToKeep.add(buildEntity("PackageXYX", "1.0", "amd64", "jessie", "main", "main/amd64/package_xyz_1.0_amd64.deb", BUILD_TYPE_ID_BT01, 12345L));
		packagesToKeep.add(buildEntity("PackageXYX", "1.0", "i386",  "jessie", "contrib", "contrib/i386/package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L));
		
		DebPackageRemovalBean packageRemovalBean = new DebPackageRemovalBean(config, BUILD_TYPE_ID_BT01, 12345L, packagesToKeep);
		
		assertEquals(6, (int)getDebRepositoryManager().getRepositoryStatistics(config, "").getTotalPackageCount());
		getDebRepositoryManager().removeBuildPackages(packageRemovalBean);
		assertEquals(2, (int)getDebRepositoryManager().getRepositoryStatistics(config, "").getTotalPackageCount());
	}
	
	@Test
	public void testPackageParameters() throws NonExistantRepositoryException, DebPackageNotFoundInStoreException {
		DebRepositoryConfiguration config = readConfig.getDebRepositoryConfigurations().get(0);
		List<DebPackage> packages = new ArrayList<>();
		DebPackage debPackage = buildEntity("PackageXYX", "1.0", "i386",  "jessie", "main", "main/i386/package_xyz_1.0_i386.deb",  BUILD_TYPE_ID_BT01, 12345L);
		
		Map<String,String> params = new TreeMap<>();
		params.put("param01", "value01");
		params.put("param02", "value02");
		params.put("param03", "value03");
		params.put("param04", "value04");
		params.put("param05", "value05");
		
		debPackage.setParameters(params);
		packages.add(debPackage);
		getDebRepositoryManager().addBuildPackages(config, packages);
		
		DebPackage foundPackage = getDebRepositoryManager().findByUri(config.getRepoName(), packages.get(0).getUri());
		assertEquals("value01", foundPackage.getParameters().get("param01"));
	}
	
	private DebPackageEntity buildEntity(String packageName, String version, String arch, String dist, String component, String filename, String buildTypeId, Long buildId) {
		DebPackageEntity entity = new DebPackageEntity();
		entity.setPackageName(packageName);
		entity.setVersion(version);
		entity.setArch(arch);
		entity.setDist(dist);
		entity.setComponent(component);
		entity.setFilename(filename);
		entity.setBuildTypeId(buildTypeId);
		entity.setBuildId(buildId);
		entity.setUri("ProjectName/BuildName/" + entity.getBuildId() + "/" + entity.getFilename());
		return entity;
	}

}
