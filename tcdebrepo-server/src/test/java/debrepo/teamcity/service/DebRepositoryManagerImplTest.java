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

import org.junit.Test;

import debrepo.teamcity.entity.DebPackageStore;

public class DebRepositoryManagerImplTest {

	@Test
	public void testGetPackageStore() {
		DebRepositoryManager manager = new DebRepositoryManagerImpl();
		DebPackageStore store = manager.initialisePackageStore("project01", "MyStoreName");
		fail("Not yet implemented");
	}

	@Test
	public void testInitialisePackageStore() {
		fail("Not yet implemented");
	}

}
