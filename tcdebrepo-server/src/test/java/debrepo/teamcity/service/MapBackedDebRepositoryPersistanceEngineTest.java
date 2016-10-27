package debrepo.teamcity.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.entity.DebPackageEntity;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

public class MapBackedDebRepositoryPersistanceEngineTest {

	private static final String BUILD_TYPE_ID_BT01 = "bt01";
	private static final String BUILD_TYPE_ID_BT02 = "bt02";
	private static final String BUILD_TYPE_ID_BT03 = "bt03";
	DebRepositoryManager debRepositoryManager;
	@Mock ProjectManager projectManager;
	@Mock SProject project01;
	@Mock SProject project02;
	@Mock SProject root;
	@Mock SBuildType bt01;
	@Mock SBuildType bt02;
	@Mock SBuildType bt03;
	@Mock SBuild build01;
	@Mock SBuild build02;
	@Mock SBuild build03;
	
	List<SProject> projectPath = new ArrayList<>();
	List<SProject> projectPath2 = new ArrayList<>();
	
	DebPackageEntity entity, entity2, entity3, entity4;
	DebRepositoryPersistanceEngine engine;
	
	@Before
	public void setup() throws NonExistantRepositoryException {
		
		MockitoAnnotations.initMocks(this);
		
		projectPath.add(project01);
		projectPath.add(project02);
		projectPath.add(root);
		
		projectPath2.add(project02);
		projectPath2.add(root);
		
		when(projectManager.findBuildTypeById(BUILD_TYPE_ID_BT01)).thenReturn(bt01);
		when(projectManager.findBuildTypeById(BUILD_TYPE_ID_BT02)).thenReturn(bt02);
		when(projectManager.findProjectById("project01")).thenReturn(project01);
		when(bt01.getProjectId()).thenReturn("project01");
		when(bt01.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT01);

		when(bt02.getProjectId()).thenReturn("project01");
		when(bt02.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT02);
		
		when(bt03.getProjectId()).thenReturn("project02");
		when(bt03.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT03);
		
		when(build01.getBuildType()).thenReturn(bt01);
		when(build01.getBuildId()).thenReturn(12345L);
		when(build01.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT01);
		
		when(build02.getBuildType()).thenReturn(bt02);
		when(build02.getBuildId()).thenReturn(12346L);
		when(build02.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT02);
		
		when(build03.getBuildType()).thenReturn(bt01);
		when(build02.getBuildId()).thenReturn(12347L);
		when(build03.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT01);
		
		when(project01.getProjectId()).thenReturn("project01");
		when(project01.getProjectPath()).thenReturn(projectPath);
		
		when(project02.getProjectId()).thenReturn("project02");
		when(project02.getProjectPath()).thenReturn(projectPath2);
		when(root.getProjectId()).thenReturn("_Root");
		
		entity = new DebPackageEntity();
		entity.setPackageName("testpackage");
		entity.setVersion("1.2.3.4");
		entity.setArch("i386");
		entity.setSBuildTypeId(BUILD_TYPE_ID_BT01);
		entity.setSBuildId(build01.getBuildId());
		
		entity2 = new DebPackageEntity();
		entity2.setPackageName("testpackage");
		entity2.setVersion("1.2.3.5");
		entity2.setArch("i386");
		entity2.setSBuildTypeId(BUILD_TYPE_ID_BT02);
		entity2.setSBuildId(build02.getBuildId());
		
		entity3 = new DebPackageEntity();
		entity3.setPackageName("testpackage");
		entity3.setVersion("1.2.3.5");
		entity3.setArch("amd64");
		entity3.setSBuildTypeId(BUILD_TYPE_ID_BT02);
		entity3.setSBuildId(build02.getBuildId());
		
		entity4 = new DebPackageEntity();
		entity4.setPackageName("anotherpackage");
		entity4.setVersion("1.5");
		entity4.setArch("amd64");
		entity4.setSBuildTypeId(BUILD_TYPE_ID_BT03);
		entity4.setSBuildId(build03.getBuildId());
		
		debRepositoryManager = new DebRepositoryManagerImpl(projectManager);
		debRepositoryManager.initialisePackageStore("project01", "MyStoreName");
		debRepositoryManager.initialisePackageStore("project02", "MyStoreName2");
		debRepositoryManager.registerBuildWithPackageStore("MyStoreName", BUILD_TYPE_ID_BT01);
		debRepositoryManager.registerBuildWithPackageStore("MyStoreName", BUILD_TYPE_ID_BT02);
		
		engine = new MapBackedDebRepositoryPersistanceEngine(debRepositoryManager, projectManager);
	}
	
	@Test
	public void testMapBackedDebRepositoryPersistanceEngine() {
		assertEquals(0, engine.findAllByBuildType(bt01).size());
	}

	@Test
	public void testAddPackage() {
		engine.addPackage(entity);
		assertEquals(1, engine.findAllByBuildType(bt01).size());
		
		engine.addPackage(entity2);
		assertEquals(1, engine.findAllByBuildType(bt02).size());
		
		engine.addPackage(entity3);
		assertEquals(2, engine.findAllByBuildType(bt02).size());
		
		assertEquals(0, engine.findAllByBuildType(bt03).size());
	}

	@Test
	public void testRemovePackage() {
		engine.addPackage(entity);
		engine.addPackage(entity2);
		
		assertEquals(2, engine.findPackageByName("MyStoreName", entity2.getPackageName()).size());
		
		engine.removePackage(entity);
		assertEquals(1, engine.findPackageByName("MyStoreName", entity2.getPackageName()).size());
		
		DebPackageEntity e = engine.findPackageByName("MyStoreName", entity2.getPackageName()).get(0);
		assertEquals(entity2.getPackageName(), e.getPackageName());
		assertEquals(entity2.getVersion(), e.getVersion());
		assertEquals(entity2.getArch(), e.getArch());
		
	}

	@Test
	public void testFindPackageByName() {
		engine.addPackage(entity);
		assertEquals(1, engine.findPackageByName("MyStoreName", entity.getPackageName()).size());
		
		engine.addPackage(entity2);
		assertEquals(2, engine.findPackageByName("MyStoreName", entity2.getPackageName()).size());
		
		// Adding a package with the same name, version and arch should not increase the package count
		engine.addPackage(entity);
		assertEquals(2, engine.findPackageByName("MyStoreName", entity.getPackageName()).size());
		
		assertEquals(0, engine.findPackageByName("MyStoreName", entity4.getPackageName()).size());
	}

	@Test
	public void testFindPackageByNameAndVersion() {
		engine.addPackage(entity);
		assertEquals(1, engine.findPackageByNameAndVersion("MyStoreName", entity.getPackageName(), entity.getVersion()).size());
		
		engine.addPackage(entity2);
		assertEquals(1, engine.findPackageByNameAndVersion("MyStoreName", entity2.getPackageName(), entity2.getVersion()).size());
		
		// Adding a package with the same name, version and arch should not increase the package count
		engine.addPackage(entity);
		assertEquals(1, engine.findPackageByNameAndVersion("MyStoreName", entity.getPackageName(), entity.getVersion()).size());
	
		assertEquals(0, engine.findPackageByNameAndVersion("MyStoreName", entity4.getPackageName(), entity4.getVersion()).size());
	}

	@Test
	public void testFindPackageByNameAndAchitecture() {
		engine.addPackage(entity);
		assertEquals(1, engine.findPackageByNameAndAchitecture("MyStoreName", entity.getPackageName(), entity.getArch()).size());
		
		engine.addPackage(entity2);
		assertEquals(2, engine.findPackageByNameAndAchitecture("MyStoreName", entity2.getPackageName(), entity2.getArch()).size());
		
		// Adding a package with the same name, version and arch should not increase the package count
		engine.addPackage(entity);
		assertEquals(2, engine.findPackageByNameAndAchitecture("MyStoreName", entity.getPackageName(), entity.getArch()).size());
		
		assertEquals(0, engine.findPackageByNameAndAchitecture("MyStoreName", entity4.getPackageName(), entity4.getArch()).size());
	}

	@Test
	public void testFindPackageByNameVersionAndArchitecture() {
		engine.addPackage(entity);
		assertEquals(1, engine.findPackageByNameVersionAndArchitecture("MyStoreName", entity.getPackageName(), entity.getVersion(), entity.getArch()).size());
		
		engine.addPackage(entity2);
		assertEquals(1, engine.findPackageByNameVersionAndArchitecture("MyStoreName", entity2.getPackageName(), entity2.getVersion(), entity2.getArch()).size());
		
		// Adding a package with the same name, version and arch should not increase the package count
		engine.addPackage(entity);
		assertEquals(1, engine.findPackageByNameVersionAndArchitecture("MyStoreName", entity.getPackageName(), entity.getVersion(), entity.getArch()).size());
		
		// Adding a package with the same name and version but different arch should not increase the package count when searching for original arch
		engine.addPackage(entity3);
		assertEquals(1, engine.findPackageByNameVersionAndArchitecture("MyStoreName", entity.getPackageName(), entity.getVersion(), entity.getArch()).size());
		
		assertEquals(1, engine.findPackageByNameVersionAndArchitecture("MyStoreName", entity3.getPackageName(), entity3.getVersion(), entity3.getArch()).size());
		
		assertEquals(0, engine.findPackageByNameVersionAndArchitecture("MyStoreName", entity4.getPackageName(), entity4.getVersion(), entity4.getArch()).size());
	}

	@Test
	public void testFindAllByBuild() {
		engine.addPackage(entity);
		assertEquals(1, engine.findAllByBuild(build01).size());
		
		engine.addPackage(entity2);
		assertEquals(1, engine.findAllByBuild(build01).size());
		assertEquals(1, engine.findAllByBuild(build02).size());
		
		engine.addPackage(entity3);
		assertEquals(1, engine.findAllByBuild(build01).size());
		assertEquals(2, engine.findAllByBuild(build02).size());
		
		assertEquals(0, engine.findAllByBuild(build03).size());		
	}

	@Test
	public void testFindAllByBuildType() {
		engine.addPackage(entity);
		assertEquals(1, engine.findAllByBuildType(bt01).size());
		
		engine.addPackage(entity2);
		assertEquals(1, engine.findAllByBuildType(bt01).size());
		assertEquals(1, engine.findAllByBuildType(bt02).size());
		
		engine.addPackage(entity3);
		assertEquals(1, engine.findAllByBuildType(bt01).size());
		assertEquals(2, engine.findAllByBuildType(bt02).size());
		
		assertEquals(0, engine.findAllByBuildType(bt03).size());		
	}

	@Test
	public void testFindAllByProject() {
		engine.addPackage(entity);
		assertEquals(1, engine.findAllByProject(project01).size());
		
		engine.addPackage(entity2);
		assertEquals(2, engine.findAllByProject(project01).size());
		
		assertEquals(0, engine.findAllByProject(project02).size());
	}

	@Test
	public void testFindAllByProjectId() {
		engine.addPackage(entity);
		assertEquals(1, engine.findAllByProjectId(project01.getProjectId()).size());
		
		engine.addPackage(entity2);
		assertEquals(2, engine.findAllByProjectId(project01.getProjectId()).size());
		
		assertEquals(0, engine.findAllByProjectId(project02.getProjectId()).size());
	}

}
