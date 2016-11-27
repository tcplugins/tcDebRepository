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
package debrepo.teamcity.entity.helper;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.DebRepositoryConfigurations;

import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import jetbrains.buildServer.serverSide.ServerPaths;

public class DebRepositoryConfigurationJaxHelperImplTest {
	
	@Mock ServerPaths serverPaths;
	PluginDataResolver pathResolver;
	JaxHelper<DebRepositoryConfigurations> configJaxHelper= new DebRepositoryConfigurationJaxHelperImpl();
	DebRepositoryConfigurations repositoryConfigurations = new DebRepositoryConfigurations();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		pathResolver = new PluginDataResolverImpl(serverPaths);
		when(serverPaths.getConfigDir()).thenReturn("target");
		
	}
	
	@Test
	public void testReadString() throws JAXBException, IOException {
		DebRepositoryConfigurationJaxImpl config01 = new DebRepositoryConfigurationJaxImpl("project01", "TestRepoName01");
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt01")
									.af(new DebRepositoryBuildTypeConfig.Filter(".+\\.deb", "wheezy", "main"))
									.af(new DebRepositoryBuildTypeConfig.Filter("/prod/somthing.*\\.deb", "wheezy", "main")));
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt02", "wheezy", "main", ".*\\.deb"));
		repositoryConfigurations.getDebRepositoryConfigurations().add(config01);
		configJaxHelper.write(repositoryConfigurations, pathResolver.getPluginConfigurationFile());	
		
		DebRepositoryConfigurations readConfig = configJaxHelper.read(pathResolver.getPluginConfigurationFile());
		assertEquals(config01, readConfig.getDebRepositoryConfigurations().get(0));
	}

	@Test
	public void testWrite() throws JAXBException, IOException {
		DebRepositoryConfigurationJaxImpl config01 = new DebRepositoryConfigurationJaxImpl("project01", "TestRepoName01");
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt01")
									.af(new DebRepositoryBuildTypeConfig.Filter(".+\\.deb", "wheezy", "main"))
									.af(new DebRepositoryBuildTypeConfig.Filter("/prod/somthing.*\\.deb", "wheezy", "main")));
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt02", "wheezy", "main", ".+\\.deb"));
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt03", "wheezy", "main", ".+\\.deb"));
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt04", "wheezy", "main", ".+\\.deb"));
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt05", "wheezy", "main", ".+\\.deb"));
		repositoryConfigurations.getDebRepositoryConfigurations().add(config01);
		configJaxHelper.write(repositoryConfigurations, pathResolver.getPluginConfigurationFile());
	}

}
