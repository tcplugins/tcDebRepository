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
