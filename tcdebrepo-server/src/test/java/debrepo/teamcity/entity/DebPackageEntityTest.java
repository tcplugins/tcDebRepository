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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import debrepo.teamcity.DebPackage;

public class DebPackageEntityTest {
	
	DebPackageEntity e1;

	@Test
	public void testClone() {
		//params.add(new PackageParameter(name, value))
		e1 = new DebPackageEntity();
		e1.setArch("i386");
		e1.setComponent("main");
		e1.setDist("wheezy");
		e1.setFilename("test/package.deb");
		e1.setPackageName("somePackage");
		//e1.populateMetadata(e1.getParameters());
		//e1.setParameters(parameters);
		e1.setBuildId(12345678L);
		e1.setBuildTypeId("bt01");
		e1.setVersion("1.0");
		e1.buildUri();
		
		DebPackage e2 = e1.clone();
		assertEquals(e1, e2);
		assertNotSame(e1, e2);
		System.out.println(e1.hashCode());
		System.out.println(e2.hashCode());
		e2.setComponent("something");
		System.out.println(e2.hashCode());
		assertFalse(e1.equals(e2));
		System.out.println(e1);
		System.out.println(e2);
	}

}
