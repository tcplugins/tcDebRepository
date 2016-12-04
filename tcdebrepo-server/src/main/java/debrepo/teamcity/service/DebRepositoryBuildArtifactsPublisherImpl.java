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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.archive.DebFileReader;
import debrepo.teamcity.archive.DebFileReaderFactory;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.parameters.impl.ReferenceResolver;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts.BuildArtifactsProcessor;

public class DebRepositoryBuildArtifactsPublisherImpl implements DebRepositoryBuildArtifactsPublisher {
	
	private final DebRepositoryManager myDepRepositoryManager;
	private final DebRepositoryConfigurationManager myDepRepositoryConfigManager;
	private final DebFileReaderFactory myDebFileReaderFactory;
	

	public DebRepositoryBuildArtifactsPublisherImpl(DebRepositoryManager debRepositoryManager, 
							DebRepositoryConfigurationManager debRepositoryConfigManager,
							DebFileReaderFactory debFileReaderFactory) {
		this.myDepRepositoryManager = debRepositoryManager;
		this.myDepRepositoryConfigManager = debRepositoryConfigManager;
		this.myDebFileReaderFactory = debFileReaderFactory;
		Loggers.SERVER.info("DebRepositoryBuildArtifactsPublisherImpl :: Starting");
	}
	

	@Override
	public void removeArtifactsFromRepositories(SBuild build, BuildArtifacts buildArtifacts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addArtifactsToRepositories(SBuild build, BuildArtifacts buildArtifacts) {
		Loggers.SERVER.debug("DebRepositoryBuildArtifactsPublisherImpl :: Processing build: " + build.getBuildTypeName());
		if (build.isArtifactsExists()) {
			List<DebPackage> entities = new ArrayList<>();
			
			BuildArtifactsProcessor  processor = new MyBuildArtifactsProcessor(build, entities);
			buildArtifacts.iterateArtifacts(processor);
			
			ParametersProvider provider = build.getParametersProvider();
			ReferenceResolver resolver = new ReferenceResolver();
			
			
			// Get a list of configs for this build.
			Set<DebRepositoryConfiguration> configs = this.myDepRepositoryConfigManager.findConfigurationsForBuildType(build.getBuildTypeId());
			// iterate of the list of configs and check the filters match.
			for (DebRepositoryConfiguration config : configs) {
				for (DebRepositoryBuildTypeConfig bt : config.getBuildTypes()) {
					if (build.getBuildType().getBuildTypeId().equals(bt.getBuildTypeId())){
						for (Filter filter : bt.getDebFilters()) {
							for (DebPackage entity : entities) {
								if (filter.matches(entity.getFilename())) {
									DebPackage newEntity = populateEntity(entity, myDebFileReaderFactory.createFileReader(build));
									/* TODO: Support for dist and component being variables.
									if (ReferencesResolverUtil.containsReference(filter.getComponent())) {
										String component = filter.getComponent();
										
										newEntity.setComponent(resolver.resolve(filter.getComponent(), value, parameters).);
									} else {*/
										newEntity.setComponent(filter.getComponent());
										newEntity.setDist(filter.getDist());
									/*}*/
									newEntity.buildUri();	
									this.myDepRepositoryManager.addBuildPackage(config, newEntity);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private DebPackage populateEntity(DebPackage entity, DebFileReader debFileReader) {
		DebPackage e = DebPackageFactory.copy(entity);
		if (!e.isPopulated()) {
			try {
				e.populateMetadata(debFileReader.getMetaDataFromPackage(entity.getFilename()));
			} catch (IOException ex) {
				Loggers.SERVER.warn("DebRepositoryBuildArtifactsPublisherImpl :: Failed to read data from package: " 
									+ entity.getFilename());
				if (Loggers.SERVER.isDebugEnabled()) { Loggers.SERVER.debug(ex); }
			}
		}
		return e;
	}
	
	public static class MyBuildArtifactsProcessor implements BuildArtifacts.BuildArtifactsProcessor {
		
		private List<DebPackage> myEntities;
		private SBuild myBuild;

		public MyBuildArtifactsProcessor(SBuild build, List<DebPackage> entities) {
			this.myBuild = build;
			this.myEntities = entities;
		}

		@Override
		public Continuation processBuildArtifact(BuildArtifact artifact) {
			Loggers.SERVER.debug("DebRepositoryBuildArtifactsPublisherImpl :: Processing artifact: " 
						+ artifact.getRelativePath() + " " + artifact.getName());
			this.myEntities.add(DebPackageFactory.buildFromArtifact(this.myBuild, artifact.getRelativePath()));
			return Continuation.CONTINUE;
		}
		
	}

}
