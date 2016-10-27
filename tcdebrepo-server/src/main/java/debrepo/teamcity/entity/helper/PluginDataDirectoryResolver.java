package debrepo.teamcity.entity.helper;

import java.io.IOException;

public interface PluginDataDirectoryResolver {
	
	public String getPluginDataDirectory() throws IOException;

}
