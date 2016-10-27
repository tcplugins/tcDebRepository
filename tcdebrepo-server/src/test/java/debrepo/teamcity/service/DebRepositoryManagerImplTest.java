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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageStore;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

public class DebRepositoryManagerImplTest {
	
	@Mock ProjectManager projectManager;
	@Mock SProject project01;
	@Mock SProject project02;
	@Mock SProject root;
	@Mock SBuildType bt01;
	@Mock SBuildType bt02;
	
	List<SProject> projectPath = new ArrayList<>();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		projectPath.add(project01);
		projectPath.add(project02);
		projectPath.add(root);
		when(projectManager.findBuildTypeById("bt01")).thenReturn(bt01);
		when(projectManager.findBuildTypeById("bt02")).thenReturn(bt02);
		when(projectManager.findProjectById("project01")).thenReturn(project01);
		when(bt01.getProjectId()).thenReturn("project01");
		when(bt02.getProjectId()).thenReturn("project01");
		when(project01.getProjectPath()).thenReturn(projectPath);
		when(project01.getProjectId()).thenReturn("project01");
		when(project02.getProjectId()).thenReturn("project02");
		when(root.getProjectId()).thenReturn("_Root");
		
		
	}
	
	@Test
	public void testGetPackageStore() throws NonExistantRepositoryException {
		
		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager);
		manager.initialisePackageStore("project01", "MyStoreName");
		manager.initialisePackageStore("project02", "MyStoreName2");
		manager.registerBuildWithPackageStore("MyStoreName", "bt01");
		DebPackageStore store = manager.getPackageStoreForBuildType("bt01");
		assertEquals(0, store.size());
		
	}
	
	@Test
	public void testGetParentPackageStore() throws NonExistantRepositoryException {
		
		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager);
		manager.initialisePackageStore("project01", "MyStoreName");
		manager.initialisePackageStore("project02", "MyStoreName2");
		manager.registerBuildWithPackageStore("MyStoreName", "bt01");
		manager.registerBuildWithPackageStore("MyStoreName2", "bt02");
		DebPackageStore store = manager.getPackageStoreForBuildType("bt01");
		DebPackageStore store2 = manager.getPackageStoreForBuildType("bt02");
		assertEquals(0, store.size());
		assertEquals(0, store2.size());
		assertNotSame(store, store2);
		
	}
	
	@Test(expected=NonExistantRepositoryException.class)
	public void testGetNonExistantPackageStore() throws NonExistantRepositoryException {
		
		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager);
		manager.initialisePackageStore("project01", "MyStoreName");
		manager.getPackageStore("MystoreMame2");
		
	}
	
	@Test
	public void testGetNullPackageStoreForBuild() throws NonExistantRepositoryException {
		
		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager);
		manager.initialisePackageStore("project01", "MyStoreName");
		manager.initialisePackageStore("project02", "MyStoreName2");
		manager.registerBuildWithPackageStore("MyStoreName", "bt01");
		assertNull(manager.getPackageStoreForBuildType("bt02"));
		
	}

	@Test
	public void testGetAddPackageToPackageStore() throws NonExistantRepositoryException {
		
		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager);
		manager.initialisePackageStore("project01", "MyStoreName");
		manager.initialisePackageStore("project02", "MyStoreName2");
		manager.registerBuildWithPackageStore("MyStoreName", "bt01");
		DebPackageStore store = manager.getPackageStoreForBuildType("bt01");
		
		DebPackageEntity e = new DebPackageEntity();
		e.setPackageName("testpackage");
		e.setVersion("1.2.3.4");
		e.setArch("i386");
		
		store.put(e.buildKey(), e);
		assertEquals(1, store.size());
		
	}

}
