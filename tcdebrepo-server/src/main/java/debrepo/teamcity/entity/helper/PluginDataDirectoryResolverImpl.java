package debrepo.teamcity.entity.helper;

import java.io.File;
import java.io.IOException;

import jetbrains.buildServer.serverSide.ServerPaths;

public class PluginDataDirectoryResolverImpl implements PluginDataDirectoryResolver {
	
	private final ServerPaths myServerPaths;
	
	public PluginDataDirectoryResolverImpl(ServerPaths serverPaths) {
		this.myServerPaths = serverPaths;
	}

	@Override
	public String getPluginDataDirectory() throws IOException {
		File dataDirPath = new File(this.myServerPaths.getPluginDataDirectory() 
									 + File.separator + "tcDebRepository"
									 + File.separator + "database");
		
		if (!dataDirPath.exists()){
			dataDirPath.mkdirs();
		}
		
		if (dataDirPath.exists() && dataDirPath.isDirectory() && dataDirPath.canWrite()) {
			return dataDirPath.getAbsolutePath();
		} else {
			throw new IOException(dataDirPath.getAbsolutePath() + " is not writable");
		}
			
	}

}
