package debrepo.teamcity.service;

import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;

public class DebRepositoryConfigurationFactoryImpl implements DebRepositoryConfigurationFactory {

	@Override
	public DebRepositoryConfigurationJaxImpl createDebRepositoryConfiguration(String uuid, String repositoryName) {
		return new DebRepositoryConfigurationJaxImpl(uuid, repositoryName);
	}

	@Override
	public DebRepositoryConfigurationJaxImpl copyDebRepositoryConfiguration(
			DebRepositoryConfigurationJaxImpl sourceConfig) {
		// TODO Need tio implement deep copy.
		return sourceConfig;
	}

}
