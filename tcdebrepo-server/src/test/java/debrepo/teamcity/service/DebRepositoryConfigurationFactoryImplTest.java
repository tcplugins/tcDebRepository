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
package debrepo.teamcity.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import debrepo.teamcity.entity.DebRepositoryConfiguration;

public class DebRepositoryConfigurationFactoryImplTest extends DebRepositoryBaseTest {
	DebRepositoryConfigurationFactory factory;
	
	@Before
	public void setupLocal() throws NonExistantRepositoryException, IOException {
		 factory = new DebRepositoryConfigurationFactoryImpl();
		 super.setup();
	}

	@Test
	public void testCreateDebRepositoryConfiguration() {
		DebRepositoryConfiguration newConfig = factory.createDebRepositoryConfiguration(project01.getProjectId(), "TestXyz01");
		assertEquals(project01.getProjectId(), newConfig.getProjectId());
		assertEquals("TestXyz01", newConfig.getRepoName());
		assertNotNull(newConfig.getUuid());
		assertEquals(0, newConfig.getBuildTypes().size());
	}

	@Test
	public void testCopyDebRepositoryConfigurationDebRepositoryConfiguration() {
		DebRepositoryConfiguration oldConfig = getDebRepoConfig1();
		DebRepositoryConfiguration newConfig = factory.copyDebRepositoryConfiguration(oldConfig);
		assertEquals(oldConfig.getProjectId(), newConfig.getProjectId());
		assertEquals(oldConfig.getRepoName(), newConfig.getRepoName());
		assertEquals(oldConfig.getBuildTypes().size(), newConfig.getBuildTypes().size());
	}

	@Test
	public void testCopyDebRepositoryConfigurationDebRepositoryConfigurationManagerString() {
		DebRepositoryConfiguration oldConfig = null;
		for (DebRepositoryConfiguration conf : debRepositoryConfigManager.getConfigurationsForProject("project01")) {
			if (conf.getRepoName().equals("MyStoreName")) {
				oldConfig = conf;
			}
		}
		UUID uuid = oldConfig.getUuid(); 
		DebRepositoryConfiguration newConfig = factory.copyDebRepositoryConfiguration(debRepositoryConfigManager, uuid.toString());
		assertEquals(oldConfig.getProjectId(), newConfig.getProjectId());
		assertEquals(oldConfig.getRepoName(), newConfig.getRepoName());
		assertEquals(oldConfig.getBuildTypes().size(), newConfig.getBuildTypes().size());
	}

	@Override
	public DebRepositoryManager getDebRepositoryManager() {
		return new DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
	}

}
