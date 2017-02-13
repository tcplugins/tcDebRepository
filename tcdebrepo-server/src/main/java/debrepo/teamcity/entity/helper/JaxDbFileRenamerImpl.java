package debrepo.teamcity.entity.helper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import debrepo.teamcity.entity.DebRepositoryConfiguration;

public class JaxDbFileRenamerImpl implements JaxDbFileRenamer {
	
	private PluginDataResolver myPluginDataDirectoryResolver;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

	public JaxDbFileRenamerImpl(PluginDataResolver pluginDataDirectoryResolver){
		myPluginDataDirectoryResolver = pluginDataDirectoryResolver;
	}


	@Override
	public boolean renameToBackup(DebRepositoryConfiguration config) {
		String configFilePath;
		try {
			configFilePath = this.myPluginDataDirectoryResolver.getPluginDatabaseDirectory() 
					+ File.separator + config.getUuid().toString() + ".xml";
			File existingFile = new File(configFilePath);
			File destFile = new File(configFilePath + ".backup-" + simpleDateFormat.format(new Date()));
			if (existingFile.exists() && existingFile.canWrite() && existingFile.isFile()) {
				return existingFile.renameTo(destFile);
			}
		} catch (IOException e) {
			return false;
		}
		return false;
	}

}
