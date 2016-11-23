package debrepo.teamcity.service;

import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;

public interface DebRepositoryConfigurationFactory {
	/**
	 * Creates a default instance of a DebRepositoryConfiguration
	 * @param uuid
	 * @param repositoryName
	 * @return
	 */
	public abstract DebRepositoryConfigurationJaxImpl createDebRepositoryConfiguration(String uuid, String repositoryName);
	public abstract DebRepositoryConfigurationJaxImpl copyDebRepositoryConfiguration(DebRepositoryConfigurationJaxImpl sourceConfig);

}
