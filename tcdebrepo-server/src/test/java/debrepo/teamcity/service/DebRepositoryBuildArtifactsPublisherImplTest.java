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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.archive.DebFileReaderFactoryImpl;
import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactHolder;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.util.PluralUtil;

public class DebRepositoryBuildArtifactsPublisherImplTest {
	@Mock
	DebRepositoryManager debRepositoryManager;
	
	@Mock
	DebRepositoryConfigurationManager debRepositoryConfigManager;
	
	@Mock
	PluginDataResolver pluginDataResolver;
	
	@Mock
	ServerPaths serverPaths;
	
	@Mock
	SBuild build;
	
	@Mock
	SBuildType buildType;
	
	BuildArtifacts buildArtifacts;
	
	DebFileReaderFactoryImpl debFileReaderFactory;
	DebRepositoryConfiguration config;
	
	Set<DebRepositoryConfiguration> configs = new TreeSet<>();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(pluginDataResolver.getPluginTempFileDirectory()).thenReturn("target/temp");
		debFileReaderFactory = new DebFileReaderFactoryImpl(pluginDataResolver, serverPaths);
		config = new DebRepositoryConfigurationJaxImpl("project01", "MyTestRepoName");
		config.addBuildType(new DebRepositoryBuildTypeConfig("bt01", "potato", "main", ".+\\.deb").af(new Filter(".+\\.deb", "whezzy", "main")));
		configs.add(config);
		when(buildType.getBuildTypeId()).thenReturn("bt01");
		when(build.getBuildType()).thenReturn(buildType);
		when(build.isArtifactsExists()).thenReturn(true);
		when(build.getBuildTypeId()).thenReturn("bt01");
		when(debRepositoryConfigManager.findConfigurationsForBuildType(anyString())).thenReturn(configs);
	}

	@Test
	public void testAddArtifactsToRepositories() throws NonExistantRepositoryException {
		
		DebRepositoryBuildArtifactsPublisherImpl publisher = new DebRepositoryBuildArtifactsPublisherImpl(debRepositoryManager, debRepositoryConfigManager, debFileReaderFactory, new DebFileBuildArtifactsProcessorFactoryImpl());
		buildArtifacts = new MyBuildArtifacts();
		publisher.addArtifactsToRepositories(build, buildArtifacts);
		
		verify(debRepositoryManager, times(1)).addBuildPackages(eq(config), any(List.class));
	}
	
	static class MyBuildArtifacts implements BuildArtifacts {
		
		List<BuildArtifact> artifacts = new ArrayList<>();

		@Override
		public BuildArtifact getArtifact(String relativePath) {
			return null;
		}

		@Override
		public BuildArtifactHolder findArtifact(String relativePath) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BuildArtifact getRootArtifact() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isAvailable() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void iterateArtifacts(BuildArtifactsProcessor processor) {
			processor.processBuildArtifact(new MyBuildArtifact("directory/file.deb", "file.deb"));
			processor.processBuildArtifact(new MyBuildArtifact("directory/filename2.deb", "filename2.deb"));
			
		}
		
	}

	
	static class MyBuildArtifact implements BuildArtifact {
		
		String relativePath;
		String fileName;
		
		public MyBuildArtifact(String path, String file) {
			this.relativePath = path;
			this.fileName = file;
		}

		@Override
		public String getRelativePath() {
			return this.relativePath;
		}

		@Override
		public String getName() {
			return this.fileName;
		}

		@Override
		public boolean isDirectory() {
			return false;
		}

		@Override
		public boolean isArchive() {
			return false;
		}

		@Override
		public boolean isFile() {
			return true;
		}

		@Override
		public boolean isContainer() {
			return false;
		}

		@Override
		public long getSize() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getTimestamp() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return null;
		}

		@Override
		public Collection<BuildArtifact> getChildren() {
			return null;
		}
		
	}
}
