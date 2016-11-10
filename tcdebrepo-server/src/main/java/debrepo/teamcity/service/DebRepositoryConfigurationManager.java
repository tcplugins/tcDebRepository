package debrepo.teamcity.service;

import java.util.List;
import java.util.Set;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurations;

public interface DebRepositoryConfigurationManager {

	public void updateRepositoryConfigurations(DebRepositoryConfigurations repoConfigurations);
	public List<DebRepositoryConfiguration> getConfigurationsForProject(String projectId);
	public Set<DebRepositoryConfiguration> findConfigurationsForBuildType(String buildTypeId);
	
}
