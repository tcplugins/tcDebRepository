/*******************************************************************************
 * Copyright 2016 Net Wolf UK
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
package debrepo.teamcity.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.helper.XmlPersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

public class DebRepositoryManagerImplTest extends DebRepositoryBaseTest {
	
//	@Mock ProjectManager projectManager;
//	@Mock SProject project01;
//	@Mock SProject project02;
//	@Mock SProject root;
//	@Mock SBuildType bt01;
//	@Mock SBuildType bt02;
//	@Mock XmlPersister<DebPackageStore> debRepositoryDatabaseXmlPersister;
	
//	List<SProject> projectPath = new ArrayList<>();

	//@Before
	//public void setup() throws IOException, NonExistantRepositoryException {
//		MockitoAnnotations.initMocks(this);
		
//		super.setup();
//		projectPath.add(project01);
//		projectPath.add(project02);
//		projectPath.add(root);
//		when(projectManager.findBuildTypeById("bt01")).thenReturn(bt01);
//		when(projectManager.findBuildTypeById("bt02")).thenReturn(bt02);
//		when(projectManager.findProjectById("project01")).thenReturn(project01);
//		when(bt01.getProjectId()).thenReturn("project01");
//		when(bt01.getBuildTypeId()).thenReturn("project01");
//		when(bt02.getProjectId()).thenReturn("project01");
//		when(project01.getProjectPath()).thenReturn(projectPath);
//		when(project01.getProjectId()).thenReturn("project01");
//		when(project02.getProjectId()).thenReturn("project02");
//		when(root.getProjectId()).thenReturn("_Root");
		
//		when(debRepositoryDatabaseXmlPersister.persistToXml(any(DebPackageStore.class))).thenReturn(true);
	//}
	
	@Test
	public void testGetPackageStore() throws NonExistantRepositoryException {
//		DebRepositoryConfiguration config1 = new DebRepositoryConfiguration("project01", "MyStoreName");
//		DebRepositoryConfiguration config2 = new DebRepositoryConfiguration("project02", "MyStoreName2");
//		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister);
//		manager.initialisePackageStore(config1);
//		manager.initialisePackageStore(config2);
//		manager.registerBuildWithPackageStore("MyStoreName", "bt01");
		List<DebPackageStore> store = debRepositoryManager.getPackageStoresForBuildType(BUILD_TYPE_ID_BT01);
		assertEquals(1, store.size());
		assertEquals(0, store.get(0).size());
		
	}
	
	@Test
	public void testGetParentPackageStore() throws NonExistantRepositoryException {
		
		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister);
		DebRepositoryConfiguration config1 = new DebRepositoryConfiguration("project01", "MyStoreName");
		config1.addBuildType(new DebRepositoryBuildTypeConfig(bt01.getBuildTypeId())
										.af(new Filter(".*\\.deb", "wheezy", "main")));
		DebRepositoryConfiguration config2 = new DebRepositoryConfiguration("project02", "MyStoreName2");
		config2.addBuildType(new DebRepositoryBuildTypeConfig(bt02.getBuildTypeId())
										.af(new Filter(".*\\.deb", "wheezy", "main")));
		manager.initialisePackageStore(config1);
		manager.initialisePackageStore(config2);
		//manager.registerBuildWithPackageStore("MyStoreName", "bt01");
		//manager.registerBuildWithPackageStore("MyStoreName2", "bt02");
		List<DebPackageStore> store = manager.getPackageStoresForBuildType("bt01");
		List<DebPackageStore> store2 = manager.getPackageStoresForBuildType("bt02");
		assertEquals(1, store.size());
		assertEquals(0, store.get(0).size());
		assertEquals(1, store2.size());
		assertEquals(0, store2.get(0).size());
		assertNotSame(store, store2);
		
	}
	
	@Test(expected=NonExistantRepositoryException.class)
	public void testGetNonExistantPackageStore() throws NonExistantRepositoryException {
		
		DebRepositoryConfiguration config1 = new DebRepositoryConfiguration("project01", "MyStoreName");
		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister);
		manager.initialisePackageStore(config1);
		manager.getPackageStore("MystoreMame2");
		
	}
	
	@Test
	public void testGetNullPackageStoreForBuild() throws NonExistantRepositoryException {
		
		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister);
		DebRepositoryConfiguration config1 = new DebRepositoryConfiguration("project01", "MyStoreName");
		DebRepositoryConfiguration config2 = new DebRepositoryConfiguration("project02", "MyStoreName2");
		manager.initialisePackageStore(config1);
		manager.initialisePackageStore(config2);
		//manager.registerBuildWithPackageStore("MyStoreName", "bt01");
		assertEquals(0, manager.getPackageStoresForBuildType("bt02").size());
		
	}

	@Test
	public void testGetAddPackageToPackageStore() throws NonExistantRepositoryException {
		
		debRepositoryManager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister);
//		DebRepositoryConfiguration config1 = new DebRepositoryConfiguration("project01", "MyStoreName");
//		DebRepositoryConfiguration config2 = new DebRepositoryConfiguration("project02", "MyStoreName2");
		debRepositoryManager.initialisePackageStore(getDebRepoConfig1());
		debRepositoryManager.initialisePackageStore(getDebRepoConfig2());
//		debRepositoryManager.registerBuildWithPackageStore("MyStoreName", "bt01");
		List<DebPackageStore> store = debRepositoryManager.getPackageStoresForBuildType("bt01");
		
		DebPackageEntity e = new DebPackageEntity();
		e.setPackageName("testpackage");
		e.setVersion("1.2.3.4");
		e.setArch("i386");
		e.setFilename("package-123.deb");
		e.setUri("ProjectName/BuildName/" + e.getSBuildId() + "/" + e.getFilename());
		store.get(0).put(e.buildKey(), e);
		assertEquals(1, store.get(0).size());
		
	}

}
