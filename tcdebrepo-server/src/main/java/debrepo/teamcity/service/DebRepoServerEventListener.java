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

import debrepo.teamcity.Loggers;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.util.executors.ExecutorsFactory;

public class DebRepoServerEventListener extends BuildServerAdapter {

	private final DebRepositoryBuildArtifactsPublisher myPublisher;
	private final DebRepositoryBuildArtifactsCleaner myCleaner;
	private final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	private final DebRepositoryManager myDebRepositoryManager;
	private final SBuildServer myBuildServer;

	public DebRepoServerEventListener(
			DebRepositoryBuildArtifactsPublisher publisher,
			DebRepositoryBuildArtifactsCleaner cleaner, 
			DebRepositoryManager maintenanceManager,
			DebRepositoryConfigurationManager configurationManager, 
			SBuildServer sBuildServer) 
	{
		myPublisher = publisher;
		myCleaner = cleaner;
		myDebRepositoryManager = maintenanceManager;
		myDebRepositoryConfigurationManager = configurationManager;
		myBuildServer = sBuildServer;
	}

	public void register() {
		myBuildServer.addListener(this);
		Loggers.SERVER.info("DebRepoServerEventListener :: Registering");
	}

	@Override
	public void serverStartup() {
		ExecutorsFactory.newExecutor("debRepoStartupThread")
				.execute(new DebRepoOnBootExecutor(
						myDebRepositoryConfigurationManager, 
						myDebRepositoryManager)
					);
	}

	@Override
	public void serverShutdown() {
		// TODO Auto-generated method stub
	}

	@Override
	public void cleanupFinished() {
		myCleaner.removeDetachedDebFilesFromRepositories();
	}

	@Override
	public void buildArtifactsChanged(SBuild build) {
		this.myPublisher.removeArtifactsFromRepositories(build,
				build.getArtifacts(BuildArtifactsViewMode.VIEW_DEFAULT));

	}

	@Override
	public void beforeBuildFinish(SRunningBuild build) {
		this.myPublisher.addArtifactsToRepositories(build, 
				build.getArtifacts(BuildArtifactsViewMode.VIEW_DEFAULT));
	}
}
