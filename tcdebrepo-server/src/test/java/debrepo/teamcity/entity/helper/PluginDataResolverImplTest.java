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
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.serverSide.ServerPaths;

public class PluginDataResolverImplTest {
	
	private static final String BUILDSERVER_PATH = ".BuildServer" + File.separator;
	@Mock ServerPaths serverPaths;
	File f;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGetPluginDatabaseDirectory() throws IOException {
		f = new File(BUILDSERVER_PATH + "pluginData");
		when(serverPaths.getPluginDataDirectory()).thenReturn(f);
		when(serverPaths.getConfigDir()).thenReturn(f.getAbsolutePath());
		PluginDataResolverImpl resolver = new PluginDataResolverImpl(serverPaths);
		System.out.println(resolver.getPluginDatabaseDirectory());
		assertEquals(f.getAbsolutePath() + File.separator + "tcDebRepository" + File.separator + "database", resolver.getPluginDatabaseDirectory());
	}

	@Test
	public void testGetPluginConfigurationFile() throws IOException {
		f = new File(BUILDSERVER_PATH +  "config");
		when(serverPaths.getPluginDataDirectory()).thenReturn(f);
		when(serverPaths.getConfigDir()).thenReturn(f.getAbsolutePath());
		PluginDataResolverImpl resolver = new PluginDataResolverImpl(serverPaths);
		System.out.println(resolver.getPluginConfigurationFile());
		assertEquals(f.getAbsolutePath() + File.separator + "deb-repositories.xml", resolver.getPluginConfigurationFile());
	}

}
