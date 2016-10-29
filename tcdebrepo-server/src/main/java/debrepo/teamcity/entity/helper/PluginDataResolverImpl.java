package debrepo.teamcity.entity.helper;

import java.io.File;
import java.io.IOException;

import jetbrains.buildServer.serverSide.ServerPaths;

public class PluginDataResolverImpl implements PluginDataResolver {
	
	private static final String TC_DEB_REPOSITORY_DATABASE_DIRECTORY_NAME = "database";
	private static final String TC_DEB_REPOSITORY_DIRECTORY_NAME = "tcDebRepository";
	private static final String TC_DEB_REPOSITORY_CONFIGURATION_FILENAME = "deb-repositories.xml";
	private final ServerPaths myServerPaths;
	
	public PluginDataResolverImpl(ServerPaths serverPaths) {
		this.myServerPaths = serverPaths;
	}

	@Override
	public String getPluginDatabaseDirectory() throws IOException {
		File dataDirPath = new File(this.myServerPaths.getPluginDataDirectory() 
									 + File.separator + TC_DEB_REPOSITORY_DIRECTORY_NAME
									 + File.separator + TC_DEB_REPOSITORY_DATABASE_DIRECTORY_NAME);
		
		if (!dataDirPath.exists()){
			dataDirPath.mkdirs();
		}
		
		if (dataDirPath.exists() && dataDirPath.isDirectory() && dataDirPath.canWrite()) {
			return dataDirPath.getAbsolutePath();
		} else {
			throw new IOException(dataDirPath.getAbsolutePath() + " is not writable");
		}
			
	}

	@Override
	public String getPluginConfigurationFile() throws IOException {
		File configFilePath = new File(this.myServerPaths.getConfigDir() 
				 + File.separator + TC_DEB_REPOSITORY_CONFIGURATION_FILENAME);
		return configFilePath.getAbsolutePath();
	}

}
