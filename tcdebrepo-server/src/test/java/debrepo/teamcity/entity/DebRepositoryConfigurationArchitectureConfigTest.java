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
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.entity.helper.DebRepositoryConfigurationJaxHelperImpl;
import debrepo.teamcity.entity.helper.JaxHelper;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationFactoryImpl;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebRepositoryConfigurationArchitectureConfigTest {
	
	@Mock ServerPaths serverPaths;
	PluginDataResolver pathResolver;
	JaxHelper<DebRepositoryConfigurations> configJaxHelper= new DebRepositoryConfigurationJaxHelperImpl();
	DebRepositoryConfigurations repositoryConfigurations = new DebRepositoryConfigurations();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		pathResolver = new PluginDataResolverImpl(serverPaths);
	}

	@Test
	public void testReadConfigWithEmptyConfiguredArchitectures() throws JAXBException, IOException {
		when(serverPaths.getConfigDir()).thenReturn("src/test/resources/configs/with-empty-architectures-list");
		DebRepositoryConfigurations readConfig = configJaxHelper.read(pathResolver.getPluginConfigurationFile());
		DebRepositoryConfigurationJaxImpl config01 = readConfig.debRepositoryConfigurations.get(0);
		
		assertEquals(5, config01.getBuildTypes().size());
		assertEquals(0, config01.getArchitecturesRepresentedByAll().size());
	}
	
	@Test
	public void testReadConfigWithConfiguredArchitectures() throws JAXBException, IOException {
		when(serverPaths.getConfigDir()).thenReturn("src/test/resources/configs/with-architectures");
		DebRepositoryConfigurations readConfig = configJaxHelper.read(pathResolver.getPluginConfigurationFile());
		DebRepositoryConfigurationJaxImpl config01 = readConfig.debRepositoryConfigurations.get(0);
		
		assertEquals(5, config01.getBuildTypes().size());
		assertEquals(10, config01.getArchitecturesRepresentedByAll().size());
	}
	
	@Test
	public void testReadConfigWithPartialConfiguredArchitectures() throws JAXBException, IOException {
		when(serverPaths.getConfigDir()).thenReturn("src/test/resources/configs/with-partial-architectures-list");
		DebRepositoryConfigurations readConfig = configJaxHelper.read(pathResolver.getPluginConfigurationFile());
		DebRepositoryConfigurationJaxImpl config01 = readConfig.debRepositoryConfigurations.get(0);
		
		assertEquals(5, config01.getBuildTypes().size());
		assertEquals(2, config01.getArchitecturesRepresentedByAll().size());
	}

}
