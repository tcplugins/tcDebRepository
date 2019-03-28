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
package debrepo.teamcity.entity.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebPackageStoreEntity;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryBaseTest;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.DebRepositoryManagerImpl;
import debrepo.teamcity.service.MapBackedDebRepositoryDatabase;
import debrepo.teamcity.service.NonExistantRepositoryException;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebRepositoryDatabaseXmlPersisterImplTest extends DebRepositoryBaseTest {

	XmlPersister<DebPackageStore,DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister;

	@Before
	public void setuplocal() throws IOException, NonExistantRepositoryException {
		ServerPaths serverPaths = mock(ServerPaths.class);
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));

		PluginDataResolver pluginDataDirectoryResolver = new PluginDataResolverImpl(serverPaths);
		JaxHelper<DebPackageStoreEntity> debRepositoryDatabaseJaxHelper = new DebRepositoryDatabaseJaxHelperImpl();
		debRepositoryDatabaseXmlPersister = new DebRepositoryDatabaseXmlPersisterImpl(
																	pluginDataDirectoryResolver, 
																	debRepositoryDatabaseJaxHelper);
		super.setup();
	}
	
	@Override
	public XmlPersister<DebPackageStore, DebRepositoryConfiguration> getDebRepositoryXmlPersister() throws IOException, NonExistantRepositoryException {
		return debRepositoryDatabaseXmlPersister;
	}
	
	@Test
	public void testPersistDatabaseToXml() throws NonExistantRepositoryException {
		
		List<DebPackageStore> store = debRepositoryManager.getPackageStoresForBuildType("bt01");
		List<DebPackageStore> store2 = debRepositoryManager.getPackageStoresForBuildType("bt03");
		assertEquals(1, store.size());
		assertEquals(0, store.get(0).size());
		assertEquals(1, store2.size());
		assertEquals(0, store2.get(0).size());
		assertNotSame(store, store2);
		
		engine = new MapBackedDebRepositoryDatabase(debRepositoryManager, debRepositoryConfigManager, projectManager);
		engine.addPackage(entity);
		
	}

	@Override
	public DebRepositoryManager getDebRepositoryManager() throws IOException, NonExistantRepositoryException {
		return new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
	}

}
