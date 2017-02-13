package debrepo.teamcity.entity.helper;

import debrepo.teamcity.entity.DebRepositoryConfiguration;

public interface JaxDbFileRenamer {
	
	public boolean renameToBackup(DebRepositoryConfiguration config);

}
