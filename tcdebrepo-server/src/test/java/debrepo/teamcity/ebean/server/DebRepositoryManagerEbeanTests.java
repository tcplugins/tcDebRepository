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


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.entity.helper.XmlPersister;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.DebRepositoryManagerTest;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebRepositoryManagerEbeanTests extends DebRepositoryManagerTest {

	@Mock ServerPaths serverPaths;
	PluginDataResolver pluginDataResolver;
	
	EbeanServerProvider ebeanServerProvider;
	DebRepositoryManager debRepositoryManager;
	DebRepositoryManagerImpl debRepositoryManagerImpl;
	
	public void setupLocal() throws JAXBException, IOException {
		System.out.println("Runing setupLocal()");
	}
	
	private void initialiseDebRepositoryManager() {
		
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		
		pluginDataResolver = new PluginDataResolverImpl(serverPaths);
		ebeanServerProvider = new EbeanServerProvider(pluginDataResolver);
		
		debRepositoryManagerImpl = new DebRepositoryManagerImpl(
				ebeanServerProvider.getEbeanServer(),
				debRepositoryConfigurationFactory, 
				debRepositoryConfigurationChangePersister);
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
