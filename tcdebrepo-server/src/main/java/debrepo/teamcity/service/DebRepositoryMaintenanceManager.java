package debrepo.teamcity.service;

import java.util.List;

import debrepo.teamcity.ebean.DebFileModel;

public interface DebRepositoryMaintenanceManager {
	
	public abstract int getTotalPackageCount();
	public abstract int getTotalFileCount();
	public abstract int getTotalRepositoryCount();
	public abstract int getDanglingFileCount();
	public abstract int getAssociatedFileCount();

	public abstract List<DebFileModel> getDanglingFiles();
	public abstract void removeDanglingFiles() throws DebRepositoryPersistanceException;
	
}
