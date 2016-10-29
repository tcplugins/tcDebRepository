package debrepo.teamcity.entity.helper;

import java.io.IOException;

import debrepo.teamcity.entity.DebPackageStore;

public interface XmlPersister<T> {
	public boolean persistToXml(T xmlStore) throws IOException;

}