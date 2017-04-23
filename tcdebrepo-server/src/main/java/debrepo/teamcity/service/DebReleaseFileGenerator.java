package debrepo.teamcity.service;

import java.util.Set;

import debrepo.teamcity.entity.DebRepositoryConfiguration;

public interface DebReleaseFileGenerator {
	
	public abstract void updateReleaseFiles(DebRepositoryConfiguration config, Set<DistComponentArchitecture> distComponentsToUpdate) throws NonExistantRepositoryException;
	public abstract void updatePackagesFiles(DebRepositoryConfiguration config, DistComponentArchitecture distCompArch) throws NonExistantRepositoryException;
	public abstract void updateAllReleaseFiles(DebRepositoryConfiguration config) throws NonExistantRepositoryException;
	
	public static interface DistComponentArchitecture {
		public String getDist();
		public String getComponent();
		public String getArch();
	}

}