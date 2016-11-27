package debrepo.teamcity.service;

import debrepo.teamcity.entity.DebRepositoryConfiguration;

public interface DebRepositoryConfigurationFactory {
	/**
	 * Creates a default instance of a DebRepositoryConfiguration
	 * @param uuid
	 * @param repositoryName
	 * @return
	 */
	public abstract DebRepositoryConfiguration createDebRepositoryConfiguration(String projectId, String repositoryName);
	public abstract DebRepositoryConfiguration copyDebRepositoryConfiguration(DebRepositoryConfiguration sourceConfig);
	public abstract DebRepositoryConfiguration copyDebRepositoryConfiguration(DebRepositoryConfigurationManager manager, String uuid);

}
