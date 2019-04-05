package debrepo.teamcity.service;

import java.util.Set;

import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DistComponentArchitecture;

public interface DebReleaseFileGenerator {
	
	public abstract void updateReleaseFiles(DebRepositoryConfiguration config, Set<? extends DistComponentArchitecture> dcas)
			throws NonExistantRepositoryException;
	
	public abstract void updatePackagesFiles(DebRepositoryConfiguration config, DistComponentArchitecture distCompArch) 
			throws NonExistantRepositoryException;
	
	public abstract void updateAllReleaseFiles(DebRepositoryConfiguration config) throws NonExistantRepositoryException;
	
}
