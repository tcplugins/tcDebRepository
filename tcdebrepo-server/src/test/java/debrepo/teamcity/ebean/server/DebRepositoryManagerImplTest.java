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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.entity.helper.ReleaseDescriptionBuilder;
import debrepo.teamcity.service.DebRepositoryBaseTest;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebRepositoryManagerImplTest extends DebRepositoryBaseTest {
	
	@Mock ServerPaths serverPaths;
	PluginDataResolver pluginDataResolver;
	
	EbeanServerProvider ebeanServerProvider;
	DebRepositoryManager debRepositoryManager;
	
	@Before
	public void setuplocal() throws NonExistantRepositoryException, IOException {
		MockitoAnnotations.initMocks(this);
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		
		pluginDataResolver = new PluginDataResolverImpl(serverPaths);
		ebeanServerProvider = new EbeanServerProvider(pluginDataResolver);
		debRepositoryManager = new DebRepositoryManagerImpl(ebeanServerProvider.getEbeanServer(), debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister, releaseDescriptionBuilder);
		debRepositoryConfigManager = (DebRepositoryConfigurationManager) debRepositoryManager;
		
		DebRepositoryConfiguration c = getDebRepoConfig1();
		c.setRepoName("blahBlah01");
		debRepositoryConfigManager.addDebRepository(c);
		
		debRepositoryManager.addBuildPackage(c, entity);
		debRepositoryManager.addBuildPackage(c, entity2);
		debRepositoryManager.addBuildPackage(c, entity3);
		debRepositoryManager.addBuildPackage(c, entity4);
		
		System.out.println(debRepositoryManager.getRepositoryStatistics(c, "myUrl").getTotalPackageCount());
		assertTrue(debRepositoryManager.getRepositoryStatistics(c, "myUrl").getTotalPackageCount() == 4);
	}

	@Test @Ignore
	public void testGetRepositoryStatisticsStringString() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetRepositoryStatisticsDebRepositoryConfigurationString() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testAddBuildPackage() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testFindUniqueArchByDistAndComponent() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testFindUniqueComponentByDist() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testFindUniqueDist() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testFindUniqueComponent() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testFindUniquePackageNameByComponent() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUniquePackagesByComponentAndPackageName() throws NonExistantRepositoryException {

		List<? extends DebPackage> packages = debRepositoryManager.getUniquePackagesByComponentAndPackageName("blahBlah01", "main", "testpackage");
		for (DebPackage d : packages) {
			Loggers.SERVER.debug(d.toString());
			System.out.println(d.getFilename() + " :: " + d.getUri() + " " + d.getDist() + d.getComponent() + d.getArch());
		}
		assertEquals(3, packages.size());
		
	}

	@Test
	public void testFindAllByDistComponentArch() throws NonExistantRepositoryException {
		List<? extends DebPackage> packages = debRepositoryManager.findAllByDistComponentArch("blahBlah01", "wheezy", "main", "i386");
		for (DebPackage d : packages) {
			System.out.println(d.getFilename() + " :: " + d.getUri() + " " + d.getDist() + d.getComponent() + d.getArch());
		}
		assertEquals(2, packages.size());
	}

	@Test @Ignore
	public void testFindByUri() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testIsExistingRepository() {
		fail("Not yet implemented");
	}

	@Override
	public DebRepositoryManager getDebRepositoryManager() throws NonExistantRepositoryException, IOException {
		setuplocal();
		return new DebRepositoryManagerImpl(ebeanServerProvider.getEbeanServer(), debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister, releaseDescriptionBuilder);
	}

}
