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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.entity.DebPackageEntity;

public class MapBackedDebRepositoryPersistanceEngineTest extends DebRepositoryBaseTest {

	
	@Before
	public void setupLocal() throws NonExistantRepositoryException, IOException {
		super.setup();
		engine = new MapBackedDebRepositoryDatabase(debRepositoryManager, debRepositoryConfigManager, projectManager);
	}
	
	@Test
	public void testMapBackedDebRepositoryPersistanceEngine() {
		assertEquals(0, engine.findAllByBuildType(bt01).size());
	}

	@Test
	public void testAddPackage() {
		engine.addPackage(entity);
		assertEquals(1, engine.findAllByBuildType(bt01).size());
		
		engine.addPackage(entity2); // entity2 belongs to 
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
		
		DebPackage e = engine.findPackageByName("MyStoreName", entity2.getPackageName()).get(0);
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
		assertEquals(1, engine.findAllByBuild(build03).size());
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

	@Override
	public DebRepositoryManager getDebRepositoryManager() {
		return new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
	}

}
