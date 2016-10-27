package debrepo.teamcity.entity.helper;

import java.io.IOException;

import debrepo.teamcity.entity.DebPackageStore;

public interface DebRepositoryDatabaseXmlPersister {
	public boolean persistDatabaseToXml(DebPackageStore debPackageStore) throws IOException;

}