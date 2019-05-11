/*******************************************************************************
 * Copyright 2017 Net Wolf UK
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
package debrepo.teamcity.ebean.server;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.mockito.Mock;

import debrepo.teamcity.entity.helper.DebRepositoryToReleaseDescriptionBuilder;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.DebRepositoryManagerTest;
import io.ebean.EbeanServer;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebRepositoryManagerEbeanTests extends DebRepositoryManagerTest {

	@Mock ServerPaths serverPaths;
	@Mock SProject project01, project02;
	@Mock EbeanServerProvider ebeanServerProvider;
	PluginDataResolver pluginDataResolver;
	
	DebRepositoryManager debRepositoryManager;
	DebRepositoryManagerImpl debRepositoryManagerImpl;
	
	public void setupLocal() throws JAXBException, IOException {
		System.out.println("Runing setupLocal()");
	}
	
	private void initialiseDebRepositoryManager() {
		
		projectManager = mock(ProjectManager.class);
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		when(projectManager.findProjectById("project01")).thenReturn(project01);
		when(projectManager.findProjectById("project02")).thenReturn(project02);
		when(project01.getExternalId()).thenReturn("My_Project_Name");
		when(project01.getDescription()).thenReturn("My Project Name - Long description");
		
		when(project02.getExternalId()).thenReturn("My_Project_Name_2");
		when(project02.getDescription()).thenReturn("My Project Name 2 - Long description");
		
		releaseDescriptionBuilder = new DebRepositoryToReleaseDescriptionBuilder(projectManager);
		
		pluginDataResolver = new PluginDataResolverImpl(serverPaths);
		
		EbeanServer ebeanServer = EbeanServerProviderImpl.createEbeanServerInstance(pluginDataResolver);
		when(ebeanServerProvider.getEbeanServer()).thenReturn(ebeanServer);
		
		debRepositoryManagerImpl = new DebRepositoryManagerImpl(
				ebeanServerProvider,
				debRepositoryConfigurationFactory, 
				debRepositoryConfigurationChangePersister,
				releaseDescriptionBuilder);
		
	}

	@Override
	public DebRepositoryManager getDebRepositoryManager() {
		if (debRepositoryManagerImpl == null) {
			initialiseDebRepositoryManager();
		}
		return debRepositoryManagerImpl; 
	}
	
	@Override
	public DebRepositoryConfigurationManager getDebRepositoryConfigurationManager() {
		if (debRepositoryManagerImpl == null) {
			initialiseDebRepositoryManager();
		}
		return debRepositoryManagerImpl; 
	}

	@Override @Test
	public void testPersist() throws IOException {}

}
