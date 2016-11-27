package debrepo.teamcity.service;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import debrepo.teamcity.entity.DebRepositoryConfiguration;

public class DebRepositoryConfigurationFactoryImplTest extends DebRepositoryBaseTest {
	DebRepositoryConfigurationFactory factory;
	
	@Before
	public void setupLocal() {
		 factory = new DebRepositoryConfigurationFactoryImpl();
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

}
