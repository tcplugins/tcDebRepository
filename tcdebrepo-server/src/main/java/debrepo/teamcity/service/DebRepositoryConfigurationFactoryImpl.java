package debrepo.teamcity.service;

import java.util.UUID;

import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig.Filter;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;

public class DebRepositoryConfigurationFactoryImpl implements DebRepositoryConfigurationFactory {
	
	@Override
	public DebRepositoryConfigurationJaxImpl createDebRepositoryConfiguration(String projectId, String repositoryName) {
		return new DebRepositoryConfigurationJaxImpl(projectId, repositoryName);
	}

	@Override
	public DebRepositoryConfiguration copyDebRepositoryConfiguration(DebRepositoryConfiguration sourceConfig) {
		DebRepositoryConfigurationJaxImpl newConfig = new DebRepositoryConfigurationJaxImpl(sourceConfig.getProjectId(), sourceConfig.getRepoName());
		newConfig.setUuid(UUID.fromString(sourceConfig.getUuid().toString()));
		for (DebRepositoryBuildTypeConfig btConfig : sourceConfig.getBuildTypes()) {
			DebRepositoryBuildTypeConfig newBtConfig = new DebRepositoryBuildTypeConfig(btConfig.getBuildTypeId());
			for (Filter f : btConfig.getDebFilters()) {
				newBtConfig.addFilter(new Filter(f.getId(), f.getRegex(), f.getDist(), f.getComponent()));
			}
			newConfig.addBuildType(newBtConfig);
		}
		return newConfig;
	}
	
	@Override
	public DebRepositoryConfiguration copyDebRepositoryConfiguration(DebRepositoryConfigurationManager manager, String uuid) {
		return copyDebRepositoryConfiguration(manager.getDebRepositoryConfiguration(uuid));
	}

}
