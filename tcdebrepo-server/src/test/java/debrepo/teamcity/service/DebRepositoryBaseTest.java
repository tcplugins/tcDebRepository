/*******************************************************************************
 * Copyright 2016, 2017 Net Wolf UK
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.ebean.server.EbeanServerProvider;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.DebRepositoryConfigurations;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.entity.helper.ReleaseDescriptionBuilder;
import debrepo.teamcity.entity.helper.XmlPersister;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.ServerPaths;

public abstract class DebRepositoryBaseTest {

	final protected String BUILD_TYPE_ID_BT01 = "bt01";
	final protected String BUILD_TYPE_ID_BT02 = "bt02";
	final protected String BUILD_TYPE_ID_BT03 = "bt03";
	protected DebRepositoryManager debRepositoryManager;
	protected DebRepositoryConfigurationManager debRepositoryConfigManager;
	@Mock protected DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister;
	@Mock protected ReleaseDescriptionBuilder releaseDescriptionBuilder;
	@Mock protected ProjectManager projectManager;
	@Mock protected XmlPersister<DebPackageStore, DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister;
	protected DebRepositoryConfigurationFactory debRepositoryConfigurationFactory = new DebRepositoryConfigurationFactoryImpl();
	@Mock protected SProject project01;
	@Mock protected SProject project02;
	@Mock protected SProject root;
	@Mock protected SBuildType bt01;
	@Mock protected SBuildType bt02;
	@Mock protected SBuildType bt03;
	@Mock protected SBuild build01;
	@Mock protected SBuild build02;
	@Mock protected SBuild build03;
	
//	@Mock protected ServerPaths serverPaths;
//	protected PluginDataResolver pluginDataResolver;
//	
//	protected EbeanServerProvider ebeanServerProvider;
	
	protected List<SProject> projectPath = new ArrayList<>();
	protected List<SProject> projectPath2 = new ArrayList<>();
	
	protected DebPackageEntity entity, entity2, entity3, entity4;
	protected DebRepositoryDatabase engine;
	
	public abstract DebRepositoryManager getDebRepositoryManager() throws NonExistantRepositoryException, IOException;
	
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
		
		when(debRepositoryDatabaseXmlPersister.loadfromXml(any(DebRepositoryConfigurationJaxImpl.class))).thenThrow(new IOException());
		when(debRepositoryDatabaseXmlPersister.persistToXml(any(DebPackageStore.class))).thenReturn(true);
		
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
		
		entity2 = new DebPackageEntity();
		entity2.setPackageName("testpackage");
		entity2.setVersion("1.2.3.5");
		entity2.setArch("i386");
		entity2.setDist("wheezy");
		entity2.setComponent("main");		
		entity2.setFilename("testpackage-i386-1.2.3.5.deb");
		entity2.setBuildTypeId(BUILD_TYPE_ID_BT02);
		entity2.setBuildId(build02.getBuildId());
		entity2.setUri("ProjectName/BuildName/" + entity2.getBuildId() + "/" + entity2.getFilename());
		
		entity3 = new DebPackageEntity();
		entity3.setPackageName("testpackage");
		entity3.setVersion("1.2.3.5");
		entity3.setArch("amd64");
		entity3.setDist("wheezy");
		entity3.setComponent("main");		
		entity3.setFilename("testpackage-amd64-1.2.3.5.deb");
		entity3.setBuildTypeId(BUILD_TYPE_ID_BT02);
		entity3.setBuildId(build02.getBuildId());
		entity3.setUri("ProjectName/BuildName/" + entity3.getBuildId() + "/" + entity3.getFilename());
		
		entity4 = new DebPackageEntity();
		entity4.setPackageName("anotherpackage");
		entity4.setVersion("1.5");
		entity4.setArch("amd64");
		entity4.setDist("wheezy");
		entity4.setComponent("main");
		entity4.setFilename("testpackage-amd64-1.5.deb");
		entity4.setBuildTypeId(BUILD_TYPE_ID_BT03);
		entity4.setBuildId(build03.getBuildId());
		entity4.setUri("ProjectName/BuildName/" + entity4.getBuildId() + "/" + entity4.getFilename());
		
//		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));
//		
//		pluginDataResolver = new PluginDataResolverImpl(serverPaths);
//		ebeanServerProvider = new EbeanServerProvider(pluginDataResolver);

		
		debRepositoryManager = getDebRepositoryManager();
		debRepositoryConfigManager = (DebRepositoryConfigurationManager) debRepositoryManager;
		DebRepositoryConfigurationJaxImpl config1 = getDebRepoConfig1();
		DebRepositoryConfigurations configs = new DebRepositoryConfigurations();
		DebRepositoryConfigurationJaxImpl config2 = getDebRepoConfig2();
		configs.add(config1);
		configs.add(config2);
		debRepositoryConfigManager.updateRepositoryConfigurations(configs);
		//debRepositoryManager.initialisePackageStore(config1);
		//debRepositoryManager.initialisePackageStore(config2);
		//debRepositoryManager.registerBuildWithPackageStore("MyStoreName", BUILD_TYPE_ID_BT01);
		//debRepositoryManager.registerBuildWithPackageStore("MyStoreName", BUILD_TYPE_ID_BT02);
		System.out.println("@Before has run");
		
	}

	public DebRepositoryConfigurationJaxImpl getDebRepoConfig1() {
		DebRepositoryConfigurationJaxImpl config1 = new DebRepositoryConfigurationJaxImpl("project01", "MyStoreName");
		config1.addBuildType(new DebRepositoryBuildTypeConfig(bt01.getBuildTypeId())
									.af(new DebRepositoryBuildTypeConfig.Filter(".+\\.deb", "wheezy", "main")));
		config1.addBuildType(new DebRepositoryBuildTypeConfig(bt02.getBuildTypeId())
									.af(new DebRepositoryBuildTypeConfig.Filter(".+\\.deb", "wheezy", "main")));
		return config1;
	}
	
	public DebRepositoryConfigurationJaxImpl getDebRepoConfig2() {
		DebRepositoryConfigurationJaxImpl config2 = new DebRepositoryConfigurationJaxImpl("project02", "MyStoreName2");
		config2.addBuildType(new DebRepositoryBuildTypeConfig(bt03.getBuildTypeId())
									.af(new DebRepositoryBuildTypeConfig.Filter(".+\\.deb", "squeeze", "main")));
		return config2;
	}

	public XmlPersister<DebPackageStore, DebRepositoryConfiguration> getDebRepositoryXmlPersister() throws IOException, NonExistantRepositoryException {
		return debRepositoryDatabaseXmlPersister;
	}
	

}
