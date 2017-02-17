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

import java.util.Collection;
import java.util.List;

import debrepo.teamcity.DebPackage;
import debrepo.teamcity.Loggers;
import debrepo.teamcity.ebean.DebPackageModel;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import jetbrains.buildServer.serverSide.ProjectManager;

public class DebRepositoryBuildArtifactsCleanerImpl implements DebRepositoryBuildArtifactsCleaner {
	

	final DebRepositoryConfigurationManager myDebRepositoryConfigurationManager;
	final DebRepositoryManager myDebRepositoryManager;
	final ProjectManager myProjectManager;
	
	public DebRepositoryBuildArtifactsCleanerImpl(
									ProjectManager projectManager,
									DebRepositoryConfigurationManager debRepositoryConfigurationManager,
									DebRepositoryManager debRepositoryManager
								) {
		myDebRepositoryConfigurationManager = debRepositoryConfigurationManager;
		myDebRepositoryManager = debRepositoryManager;
		myProjectManager = projectManager;
	}

	@Override
	public void removeDetachedArtifactsFromRepositories() {
		Loggers.SERVER.info("Starting removeDetachedArtifactsFromRepositories.");
		for (DebRepositoryConfiguration c : myDebRepositoryConfigurationManager.getAllConfigurations()) {
			DebPackageModel.db().beginTransaction();
			try {
				Collection<String> filenames = myDebRepositoryManager.findUniqueFilenames(c.getRepoName());
				for (DebPackage dp : myDebRepositoryManager.findAllByFilenames(c.getRepoName(), filenames)) {
					Loggers.SERVER.debug(dp.getFilename());
				}
			} catch (NonExistantRepositoryException e) {
				continue;
			} finally {
				DebPackageModel.db().commitTransaction();
			}
		}

	}

}
