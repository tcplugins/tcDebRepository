/*******************************************************************************
 * Copyright 2016, 2017 Net Wolf UK
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
import java.util.Date;
import java.util.List;
import java.util.Set;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.archive.DebFileReader;
import debrepo.teamcity.archive.DebFileReaderFactory;
import debrepo.teamcity.archive.DebPackageReadException;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.service.DebRepositoryManager.DebPackageRemovalBean;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.parameters.impl.ReferenceResolver;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts.BuildArtifactsProcessor;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.serverSide.buildLog.MessageAttrs;

public class DebRepositoryBuildArtifactsPublisherImpl implements DebRepositoryBuildArtifactsPublisher {
	
	private final DebRepositoryManager myDepRepositoryManager;
	private final DebRepositoryConfigurationManager myDepRepositoryConfigManager;
	private final DebFileReaderFactory myDebFileReaderFactory;
	private final DebFileBuildArtifactsProcessorFactory myDebFileBuildArtifactsProcessorFactory;
	

	public DebRepositoryBuildArtifactsPublisherImpl(DebRepositoryManager debRepositoryManager, 
							DebRepositoryConfigurationManager debRepositoryConfigManager,
							DebFileReaderFactory debFileReaderFactory,
							DebFileBuildArtifactsProcessorFactory debFileBuildArtifactsProcessorFactory) {
		this.myDepRepositoryManager = debRepositoryManager;
		this.myDepRepositoryConfigManager = debRepositoryConfigManager;
		this.myDebFileReaderFactory = debFileReaderFactory;
		this.myDebFileBuildArtifactsProcessorFactory = debFileBuildArtifactsProcessorFactory;
		Loggers.SERVER.info("DebRepositoryBuildArtifactsPublisherImpl :: Starting");
	}
	

	@Override
	public void removeArtifactsFromRepositories(SBuild build, BuildArtifacts buildArtifacts) {
		// Get a list of configs for this build.
		Set<DebRepositoryConfiguration> configs = this.myDepRepositoryConfigManager.findConfigurationsForBuildType(build.getBuildTypeId());
		Loggers.SERVER.info("DebRepositoryBuildArtifactsPublisherImpl#removeArtifactsFromRepositories :: found " + configs.size() + " repos interested in " + build.getFullName());
		List<DebPackage> entitiesToKeep = new ArrayList<>();
		
		BuildArtifactsProcessor  processor = myDebFileBuildArtifactsProcessorFactory.getBuildArtifactsProcessor(build, entitiesToKeep);
		buildArtifacts.iterateArtifacts(processor);
		
		Loggers.SERVER.info("DebRepositoryBuildArtifactsPublisherImpl#removeArtifactsFromRepositories :: found " + entitiesToKeep.size() + " artifacts in " + build.getFullName() + " # " + String.valueOf(build.getBuildId()));
		for (DebRepositoryConfiguration config : configs) {
			myDepRepositoryManager.removeBuildPackages(new DebPackageRemovalBean(config, build.getBuildTypeId(), build.getBuildId(), entitiesToKeep));
		}
	}

	@Override
	public void addArtifactsToRepositories(SBuild build, BuildArtifacts buildArtifacts) {
		Loggers.SERVER.debug("DebRepositoryBuildArtifactsPublisherImpl :: Processing build: " + build.getBuildTypeName());
		if (build.isArtifactsExists()) {
			List<DebPackage> entities = new ArrayList<>();
			
			BuildArtifactsProcessor  processor = myDebFileBuildArtifactsProcessorFactory.getBuildArtifactsProcessor(build, entities);
			buildArtifacts.iterateArtifacts(processor);
			
			ParametersProvider provider = build.getParametersProvider();
			ReferenceResolver resolver = new ReferenceResolver();
			
			BuildLog buildLog = build.getBuildLog();
			buildLog.openBlock("Processing Debian Artifacts", "", MessageAttrs.attrs());

			// Get a list of configs for this build.
			Set<DebRepositoryConfiguration> configs = this.myDepRepositoryConfigManager.findConfigurationsForBuildType(build.getBuildTypeId());
			// iterate of the list of configs and check the filters match.
			for (DebRepositoryConfiguration config : configs) {
				List<DebPackage> packagesToAdd = new ArrayList<>();
				for (DebRepositoryBuildTypeConfig bt : config.getBuildTypes()) {
					if (build.getBuildType().getBuildTypeId().equals(bt.getBuildTypeId())){
						for (Filter filter : bt.getDebFilters()) {
							for (DebPackage entity : entities) {
								if (!"".equals(entity.getFilename().trim()) && filter.matches(entity.getFilename())) {
									DebPackage newEntity = populateEntity(entity, myDebFileReaderFactory.createFileReader(build), buildLog);
									/* TODO: Support for dist and component being variables.
									if (ReferencesResolverUtil.containsReference(filter.getComponent())) {
										String component = filter.getComponent();
										
										newEntity.setComponent(resolver.resolve(filter.getComponent(), value, parameters).);
									} else {*/
										newEntity.setComponent(filter.getComponent());
										newEntity.setDist(filter.getDist());
									/*}*/
									newEntity.buildUri();	
									packagesToAdd.add(newEntity);
								}
							}
						}
					}
				}
				// If we have some matches, pass them all to the manager and add/persist them.
				if (! packagesToAdd.isEmpty()) {
					try {
						this.myDepRepositoryManager.addBuildPackages(config, packagesToAdd);
						for (DebPackage p : packagesToAdd) {
							buildLog.message(String.format("Added debian package '%s' to repository '%s' at '%s/%s/binary-%s/%s'", 
									p.getPackageName(), config.getRepoName(), 
									p.getDist(), p.getComponent(), p.getArch(), p.getFilename()),
									Status.NORMAL, MessageAttrs.attrs());
						}
					} catch (NonExistantRepositoryException e) {
						buildLog.message("Unable to add package. Specified repository was not found", Status.WARNING, MessageAttrs.attrs());
						Loggers.SERVER.warn("DebRepositoryBuildArtifactsPublisherImpl#addArtifactsToRepositories :: Failed to add " + packagesToAdd.size() + " packages to non-existant repository " + config.getRepoName() + " (" + config.getUuid().toString() + ")");
					}
				} else {
					buildLog.message("No matching Debian packages found", Status.NORMAL, MessageAttrs.attrs());
				}
			}
			buildLog.closeBlock("Processing Debian Artifacts", "", new Date(), MessageAttrs.DEFAULT_FLOW_ID);

		}
	}
	
	private DebPackage populateEntity(DebPackage entity, DebFileReader debFileReader, BuildLog buildLog) {
		DebPackage e = DebPackageFactory.copy(entity);
		if (!e.isPopulated()) {
			try {
				e.populateMetadata(debFileReader.getMetaDataFromPackage(entity.getFilename()));
			} catch (IOException ex) {
				buildLog.message("IOException occurred trying to read debian package content in " + entity.getFilename() + ". See teamcity log for stacktrace (DEBUG)", Status.ERROR, MessageAttrs.attrs());
				Loggers.SERVER.warn("DebRepositoryBuildArtifactsPublisherImpl :: Failed to read data from package: " 
									+ entity.getFilename());
				if (Loggers.SERVER.isDebugEnabled()) { Loggers.SERVER.debug(ex); }
			} catch (DebPackageReadException e1) {
				buildLog.message("DebPackageReadException occurred trying to read debian package content in " + entity.getFilename() + ". " + e1.getMessage(), Status.ERROR, MessageAttrs.attrs());
				Loggers.SERVER.warn("DebRepositoryBuildArtifactsPublisherImpl :: Failed to read data from package: " 
						+ entity.getFilename());
				if (Loggers.SERVER.isDebugEnabled()) { Loggers.SERVER.debug(e1); }

			}
		}
		return e;
	}

}
