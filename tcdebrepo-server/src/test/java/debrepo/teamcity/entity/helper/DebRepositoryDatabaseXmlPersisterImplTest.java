package debrepo.teamcity.entity.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebPackageStoreEntity;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryBaseTest;
import debrepo.teamcity.service.DebRepositoryDatabase;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.DebRepositoryManagerImpl;
import debrepo.teamcity.service.MapBackedDebRepositoryDatabase;
import debrepo.teamcity.service.NonExistantRepositoryException;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebRepositoryDatabaseXmlPersisterImplTest extends DebRepositoryBaseTest {

	private static final String BUILD_TYPE_ID_BT01 = "bt01";
	private static final String BUILD_TYPE_ID_BT02 = "bt02";
	private static final String BUILD_TYPE_ID_BT03 = "bt03";
	
	//@Mock ProjectManager projectManager;
	XmlPersister<DebPackageStore> debRepositoryDatabaseXmlPersister;
	//DebRepositoryManager debRepositoryManager;
	
//	@Mock SProject project01;
//	@Mock SProject project02;
//	@Mock SProject root;
//	@Mock SBuildType bt01;
//	@Mock SBuildType bt02;
//	@Mock SBuildType bt03;
//	@Mock SBuild build01;
//	@Mock SBuild build02;
//	@Mock SBuild build03;
//	
//	List<SProject> projectPath = new ArrayList<>();
//	List<SProject> projectPath2 = new ArrayList<>();
//	
//	DebPackageEntity entity, entity2, entity3, entity4;
//	DebRepositoryDatabase engine;
//	
	@Mock ServerPaths serverPaths;

	@Before
	public void setuplocal() throws IOException, NonExistantRepositoryException {
//		MockitoAnnotations.initMocks(this);
//		
//		projectPath.add(project01);
//		projectPath.add(project02);
//		projectPath.add(root);
//		
//		projectPath2.add(project02);
//		projectPath2.add(root);
//		
//		when(projectManager.findBuildTypeById(BUILD_TYPE_ID_BT01)).thenReturn(bt01);
//		when(projectManager.findBuildTypeById(BUILD_TYPE_ID_BT02)).thenReturn(bt02);
//		when(projectManager.findProjectById("project01")).thenReturn(project01);
//		when(bt01.getProjectId()).thenReturn("project01");
//		when(bt01.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT01);
//
//		when(bt02.getProjectId()).thenReturn("project01");
//		when(bt02.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT02);
//		
//		when(bt03.getProjectId()).thenReturn("project02");
//		when(bt03.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT03);
//		
//		when(build01.getBuildType()).thenReturn(bt01);
//		when(build01.getBuildId()).thenReturn(12345L);
//		when(build01.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT01);
//		
//		when(build02.getBuildType()).thenReturn(bt02);
//		when(build02.getBuildId()).thenReturn(12346L);
//		when(build02.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT02);
//		
//		when(build03.getBuildType()).thenReturn(bt01);
//		when(build02.getBuildId()).thenReturn(12347L);
//		when(build03.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT01);
//		
//		when(project01.getProjectId()).thenReturn("project01");
//		when(project01.getProjectPath()).thenReturn(projectPath);
//		
//		when(project02.getProjectId()).thenReturn("project02");
//		when(project02.getProjectPath()).thenReturn(projectPath2);
//		when(root.getProjectId()).thenReturn("_Root");
//		
//		entity = new DebPackageEntity();
//		entity.setPackageName("testpackage");
//		entity.setVersion("1.2.3.4");
//		entity.setArch("i386");
//		entity.setSBuildTypeId(BUILD_TYPE_ID_BT01);
//		entity.setSBuildId(build01.getBuildId());
//		
//		entity2 = new DebPackageEntity();
//		entity2.setPackageName("testpackage");
//		entity2.setVersion("1.2.3.5");
//		entity2.setArch("i386");
//		entity2.setSBuildTypeId(BUILD_TYPE_ID_BT02);
//		entity2.setSBuildId(build02.getBuildId());
//		
//		entity3 = new DebPackageEntity();
//		entity3.setPackageName("testpackage");
//		entity3.setVersion("1.2.3.5");
//		entity3.setArch("amd64");
//		entity3.setSBuildTypeId(BUILD_TYPE_ID_BT02);
//		entity3.setSBuildId(build02.getBuildId());
//		
//		entity4 = new DebPackageEntity();
//		entity4.setPackageName("anotherpackage");
//		entity4.setVersion("1.5");
//		entity4.setArch("amd64");
//		entity4.setSBuildTypeId(BUILD_TYPE_ID_BT03);
//		entity4.setSBuildId(build03.getBuildId());
//		
//		debRepositoryManager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister);
//		DebRepositoryConfiguration config1 = new DebRepositoryConfiguration("project01", "MyStoreName");
//		DebRepositoryConfiguration config2 = new DebRepositoryConfiguration("project02", "MyStoreName2");
//		debRepositoryManager.initialisePackageStore(config1);
//		debRepositoryManager.initialisePackageStore(config2);
//		debRepositoryManager.registerBuildWithPackageStore("MyStoreName", BUILD_TYPE_ID_BT01);
//		debRepositoryManager.registerBuildWithPackageStore("MyStoreName", BUILD_TYPE_ID_BT02);
		
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));

		PluginDataResolver pluginDataDirectoryResolver = new PluginDataResolverImpl(serverPaths);
		JaxHelper<DebPackageStoreEntity> debRepositoryDatabaseJaxHelper = new DebRepositoryDatabaseJaxHelperImpl();
		debRepositoryDatabaseXmlPersister = new DebRepositoryDatabaseXmlPersisterImpl(
																	pluginDataDirectoryResolver, 
																	debRepositoryDatabaseJaxHelper);
	}
	
	@Test
	public void testPersistDatabaseToXml() throws NonExistantRepositoryException {
		
		debRepositoryManager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister);
//		DebRepositoryConfiguration config1 = new DebRepositoryConfiguration("project01", "MyStoreName");
//		
//		DebRepositoryConfiguration config2 = new DebRepositoryConfiguration("project02", "MyStoreName2");
//		debRepositoryManager.initialisePackageStore(config1);
//		debRepositoryManager.initialisePackageStore(config2);
//		manager.registerBuildWithPackageStore("MyStoreName", "bt01");
//		manager.registerBuildWithPackageStore("MyStoreName2", "bt02");
		debRepositoryManager.initialisePackageStore(getDebRepoConfig1());
		debRepositoryManager.initialisePackageStore(getDebRepoConfig2());
		List<DebPackageStore> store = debRepositoryManager.getPackageStoresForBuildType("bt01");
		List<DebPackageStore> store2 = debRepositoryManager.getPackageStoresForBuildType("bt03");
		assertEquals(1, store.size());
		assertEquals(0, store.get(0).size());
		assertEquals(1, store2.size());
		assertEquals(0, store2.get(0).size());
		assertNotSame(store, store2);
		
		engine = new MapBackedDebRepositoryDatabase(debRepositoryManager, projectManager);
		
//		DebPackageEntity e = new DebPackageEntity();
//		e.setPackageName("testpackage");
//		e.setVersion("1.2.3.4");
//		e.setArch("i386");
		
		engine.addPackage(entity);
		
	}

}
