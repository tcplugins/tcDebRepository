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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

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
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.helper.XmlPersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

public class DebRepositoryBaseTest {

	final protected String BUILD_TYPE_ID_BT01 = "bt01";
	final protected String BUILD_TYPE_ID_BT02 = "bt02";
	final protected String BUILD_TYPE_ID_BT03 = "bt03";
	protected DebRepositoryManager debRepositoryManager;
	@Mock protected ProjectManager projectManager;
	@Mock protected XmlPersister<DebPackageStore, DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister;
	@Mock protected SProject project01;
	@Mock protected SProject project02;
	@Mock protected SProject root;
	@Mock protected SBuildType bt01;
	@Mock protected SBuildType bt02;
	@Mock protected SBuildType bt03;
	@Mock protected SBuild build01;
	@Mock protected SBuild build02;
	@Mock protected SBuild build03;
	
	protected List<SProject> projectPath = new ArrayList<>();
	protected List<SProject> projectPath2 = new ArrayList<>();
	
	protected DebPackageEntity entity, entity2, entity3, entity4;
	protected DebRepositoryDatabase engine;
	
	@Before
	public void setup() throws NonExistantRepositoryException, IOException {
		
		MockitoAnnotations.initMocks(this);
		
		projectPath.add(project01);
		projectPath.add(project02);
		projectPath.add(root);
		
		projectPath2.add(project02);
		projectPath2.add(root);
		
		when(projectManager.findBuildTypeById(BUILD_TYPE_ID_BT01)).thenReturn(bt01);
		when(projectManager.findBuildTypeById(BUILD_TYPE_ID_BT02)).thenReturn(bt02);
		when(projectManager.findBuildTypeById(BUILD_TYPE_ID_BT03)).thenReturn(bt03);
		when(projectManager.findProjectById("project01")).thenReturn(project01);
		when(bt01.getProjectId()).thenReturn("project01");
		when(bt01.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT01);
		
		when(bt02.getProjectId()).thenReturn("project01");
		when(bt02.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT02);
		
		when(projectManager.findProjectById("project02")).thenReturn(project02);
		when(bt03.getProjectId()).thenReturn("project02");
		when(bt03.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT03);
		
		when(build01.getBuildType()).thenReturn(bt01);
		when(build01.getBuildId()).thenReturn(12345L);
		when(build01.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT01);
		
		when(build02.getBuildType()).thenReturn(bt02);
		when(build02.getBuildId()).thenReturn(12346L);
		when(build02.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT02);
		
		when(build03.getBuildType()).thenReturn(bt01);
		when(build03.getBuildId()).thenReturn(12347L);
		when(build03.getBuildTypeId()).thenReturn(BUILD_TYPE_ID_BT01);
		
		when(project01.getProjectId()).thenReturn("project01");
		when(project01.getProjectPath()).thenReturn(projectPath);
		
		when(project02.getProjectId()).thenReturn("project02");
		when(project02.getProjectPath()).thenReturn(projectPath2);
		when(root.getProjectId()).thenReturn("_Root");
		
		when(debRepositoryDatabaseXmlPersister.loadfromXml(any(DebRepositoryConfiguration.class))).thenThrow(new IOException());
		when(debRepositoryDatabaseXmlPersister.persistToXml(any(DebPackageStore.class))).thenReturn(true);
		
		entity = new DebPackageEntity();
		entity.setPackageName("testpackage");
		entity.setVersion("1.2.3.4");
		entity.setArch("i386");
		entity.setFilename("testpackage-i386-1.2.3.4.deb");
		entity.setSBuildTypeId(BUILD_TYPE_ID_BT01);
		entity.setSBuildId(build01.getBuildId());
		entity.setUri("ProjectName/BuildName/" + entity.getSBuildId() + "/" + entity.getFilename());
		
		entity2 = new DebPackageEntity();
		entity2.setPackageName("testpackage");
		entity2.setVersion("1.2.3.5");
		entity2.setArch("i386");
		entity2.setFilename("testpackage-i386-1.2.3.5.deb");
		entity2.setSBuildTypeId(BUILD_TYPE_ID_BT02);
		entity2.setSBuildId(build02.getBuildId());
		entity2.setUri("ProjectName/BuildName/" + entity2.getSBuildId() + "/" + entity2.getFilename());
		
		entity3 = new DebPackageEntity();
		entity3.setPackageName("testpackage");
		entity3.setVersion("1.2.3.5");
		entity3.setArch("amd64");
		entity3.setFilename("testpackage-amd64-1.2.3.5.deb");
		entity3.setSBuildTypeId(BUILD_TYPE_ID_BT02);
		entity3.setSBuildId(build02.getBuildId());
		entity3.setUri("ProjectName/BuildName/" + entity3.getSBuildId() + "/" + entity3.getFilename());
		
		entity4 = new DebPackageEntity();
		entity4.setPackageName("anotherpackage");
		entity4.setVersion("1.5");
		entity4.setArch("amd64");
		entity4.setFilename("testpackage-amd64-1.5.deb");
		entity4.setSBuildTypeId(BUILD_TYPE_ID_BT03);
		entity4.setSBuildId(build03.getBuildId());
		entity4.setUri("ProjectName/BuildName/" + entity4.getSBuildId() + "/" + entity4.getFilename());
		
		debRepositoryManager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister);
		DebRepositoryConfiguration config1 = getDebRepoConfig1();
		DebRepositoryConfiguration config2 = getDebRepoConfig2();
		debRepositoryManager.initialisePackageStore(config1);
		debRepositoryManager.initialisePackageStore(config2);
		//debRepositoryManager.registerBuildWithPackageStore("MyStoreName", BUILD_TYPE_ID_BT01);
		//debRepositoryManager.registerBuildWithPackageStore("MyStoreName", BUILD_TYPE_ID_BT02);
		
		System.out.println("@Before has run");
		
	}

	public DebRepositoryConfiguration getDebRepoConfig1() {
		DebRepositoryConfiguration config1 = new DebRepositoryConfiguration("project01", "MyStoreName");
		config1.addBuildType(new DebRepositoryBuildTypeConfig(bt01.getBuildTypeId())
									.af(new DebRepositoryBuildTypeConfig.Filter(".+\\.deb", "wheezy", "main")));
		config1.addBuildType(new DebRepositoryBuildTypeConfig(bt02.getBuildTypeId())
									.af(new DebRepositoryBuildTypeConfig.Filter(".+\\.deb", "wheezy", "main")));
		return config1;
	}
	
	public DebRepositoryConfiguration getDebRepoConfig2() {
		DebRepositoryConfiguration config2 = new DebRepositoryConfiguration("project02", "MyStoreName2");
		config2.addBuildType(new DebRepositoryBuildTypeConfig(bt03.getBuildTypeId())
									.af(new DebRepositoryBuildTypeConfig.Filter(".+\\.deb", "squeeze", "main")));
		return config2;
	}
	

}
