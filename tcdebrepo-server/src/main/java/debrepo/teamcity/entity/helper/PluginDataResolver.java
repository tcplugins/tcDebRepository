package debrepo.teamcity.entity.helper;

import java.io.IOException;

public interface PluginDataResolver {
	
	public String getPluginDatabaseDirectory() throws IOException;
	public String getPluginConfigurationFile();

}
