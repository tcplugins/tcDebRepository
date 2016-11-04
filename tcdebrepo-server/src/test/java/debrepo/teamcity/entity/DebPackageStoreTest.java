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
package debrepo.teamcity.entity;

import static org.junit.Assert.*;

import org.junit.Test;

public class DebPackageStoreTest {

	@Test
	public void addItemToStoreTest() {
		
		DebPackageStore store = new DebPackageStore();
		
		DebPackageEntityKey key = new DebPackageEntityKey("abc-package", "1.1-ubuntu-6", "amd64");
		DebPackageEntity entity = new DebPackageEntity();
		entity.setPackageName("abc-package");
		entity.setVersion("1.1-ubuntu-6");
		entity.setArch("amd64");
		entity.setFilename("package-123.deb");
		store.put(key, entity);
		assertTrue(store.size() == 1);
	}
	
	@Test
	public void addItemToStoreAndRetreiveWithSameKeyTest() {
		
		DebPackageStore store = new DebPackageStore();
		
		DebPackageEntityKey key = new DebPackageEntityKey("abc-package", "1.1-ubuntu-6", "amd64");
		DebPackageEntity entity = new DebPackageEntity();
		entity.setPackageName("abc-package");
		entity.setVersion("1.1-ubuntu-6");
		entity.setArch("amd64");
		entity.setFilename("package-123.deb");
		store.put(key, entity);
		assertTrue(store.size() == 1);
		
		DebPackageEntity newEntity = store.find(key);
		assertEquals(key.getPackageName(), newEntity.getPackageName());
	}
	
	@Test
	public void addItemToStoreAndRetreiveWithNewKeyTest() {
		
		DebPackageStore store = new DebPackageStore();
		
		DebPackageEntityKey key = new DebPackageEntityKey("abc-package", "1.1-ubuntu-6", "amd64");
		DebPackageEntity entity = new DebPackageEntity();
		entity.setPackageName("abc-package");
		entity.setVersion("1.1-ubuntu-6");
		entity.setArch("amd64");
		entity.setFilename("package-123.deb");
		store.put(key, entity);
		assertTrue(store.size() == 1);
		
		DebPackageEntity newEntity = store.find(new DebPackageEntityKey("abc-package", "1.1-ubuntu-6", "amd64"));
		assertEquals(key.getPackageName(), newEntity.getPackageName());
	}

	@Test
	public void addItemToStoreAndRetreiveWithKeyConstructorTest() {
		
		DebPackageStore store = new DebPackageStore();
		
		DebPackageEntityKey key = new DebPackageEntityKey("abc-package", "1.1-ubuntu-6", "amd64");
		DebPackageEntity entity = new DebPackageEntity();
		entity.setPackageName("abc-package");
		entity.setVersion("1.1-ubuntu-6");
		entity.setArch("amd64");
		entity.setFilename("package-123.deb");
		store.put(key, entity);
		assertTrue(store.size() == 1);
		
		DebPackageEntity newEntity = store.find("abc-package", "1.1-ubuntu-6", "amd64");
		assertEquals(key.getPackageName(), newEntity.getPackageName());
	}

}
