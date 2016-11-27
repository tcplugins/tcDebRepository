package debrepo.teamcity.entity;

import java.util.List;
import java.util.UUID;

public interface DebRepositoryConfiguration extends Comparable<DebRepositoryConfiguration> {
	
	public abstract String getProjectId();
	public abstract UUID getUuid();
	public abstract String getRepoName();
	public abstract void setRepoName(String repoName);
	public abstract boolean containsBuildType(String buildTypeid);
	public abstract boolean containsBuildTypeAndFilter(DebPackageEntity entity);
	public abstract List<DebRepositoryBuildTypeConfig> getBuildTypes();
	public abstract boolean addBuildType(DebRepositoryBuildTypeConfig buildTypeConfig);

}
