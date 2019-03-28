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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;

public class DebRepositoryManagerImplTest extends DebRepositoryBaseTest {
	
	@Before
	public void setup() throws NonExistantRepositoryException, IOException {
		super.setup();
	}
 
	@Test
	public void testGetPackageStore() throws NonExistantRepositoryException {
		List<DebPackageStore> store = debRepositoryManager.getPackageStoresForBuildType(BUILD_TYPE_ID_BT01);
		assertEquals(1, store.size());
		assertEquals(0, store.get(0).size());
		
	}
	
	@Test
	public void testGetParentPackageStore() throws NonExistantRepositoryException {
		
		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		DebRepositoryConfigurationJaxImpl config1 = new DebRepositoryConfigurationJaxImpl("project01", "MyStoreName");
		config1.addBuildType(new DebRepositoryBuildTypeConfig(bt01.getBuildTypeId())
										.af(new Filter(".*\\.deb", "wheezy", "main")));
		DebRepositoryConfigurationJaxImpl config2 = new DebRepositoryConfigurationJaxImpl("project02", "MyStoreName2");
		config2.addBuildType(new DebRepositoryBuildTypeConfig(bt02.getBuildTypeId())
										.af(new Filter(".*\\.deb", "wheezy", "main")));
		manager.initialisePackageStore(config1);
		manager.initialisePackageStore(config2);
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
		
		DebRepositoryConfigurationJaxImpl config1 = new DebRepositoryConfigurationJaxImpl("project01", "MyStoreName");
		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		manager.initialisePackageStore(config1);
		manager.getPackageStore("MystoreMame2");
		
	}
	
	@Test
	public void testGetNullPackageStoreForBuild() throws NonExistantRepositoryException {
		
		DebRepositoryManager manager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		DebRepositoryConfigurationJaxImpl config1 = new DebRepositoryConfigurationJaxImpl("project01", "MyStoreName");
		DebRepositoryConfigurationJaxImpl config2 = new DebRepositoryConfigurationJaxImpl("project02", "MyStoreName2");
		manager.initialisePackageStore(config1);
		manager.initialisePackageStore(config2);
		assertEquals(0, manager.getPackageStoresForBuildType("bt02").size());
		
	}

	@Test
	public void testGetAddPackageToPackageStore() throws NonExistantRepositoryException {
		
		debRepositoryManager = new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		debRepositoryManager.initialisePackageStore(getDebRepoConfig1());
		debRepositoryManager.initialisePackageStore(getDebRepoConfig2());
		List<DebPackageStore> store = debRepositoryManager.getPackageStoresForBuildType("bt01");
		
		DebPackageEntity e = new DebPackageEntity();
		e.setPackageName("testpackage");
		e.setVersion("1.2.3.4");
		e.setArch("i386");
		e.setDist("wheezy");
		e.setComponent("main");
		e.setFilename("package-123.deb");
		e.setUri("ProjectName/BuildName/" + e.getBuildId() + "/" + e.getFilename());
		store.get(0).put(e.buildKey(), e);
		assertEquals(1, store.get(0).size());
		
	}

	@Override
	public DebRepositoryManager getDebRepositoryManager() {
		return new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
	}

}
