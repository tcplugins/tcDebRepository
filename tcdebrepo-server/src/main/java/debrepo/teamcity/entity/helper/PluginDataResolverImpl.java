/*******************************************************************************
 * Copyright 2016 Net Wolf UK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package debrepo.teamcity.entity.helper;

import java.io.File;
import java.io.IOException;

import debrepo.teamcity.Loggers;
import jetbrains.buildServer.serverSide.ServerPaths;

public class PluginDataResolverImpl implements PluginDataResolver {
	
	private static final String TC_DEB_REPOSITORY_DATABASE_DIRECTORY_NAME = "database";
	private static final String TC_DEB_REPOSITORY_TEMP_DIRECTORY_NAME = "temp";
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
	public String getPluginConfigurationFile() {
		return this.myServerPaths.getConfigDir() + File.separator + TC_DEB_REPOSITORY_CONFIGURATION_FILENAME;
	}

	@Override
	public String getPluginTempFileDirectory() {
		// TODO Auto-generated method stub
		File tempDirPath = new File(this.myServerPaths.getPluginDataDirectory() 
				 + File.separator + TC_DEB_REPOSITORY_DIRECTORY_NAME
				 + File.separator + TC_DEB_REPOSITORY_TEMP_DIRECTORY_NAME);

		if (!tempDirPath.exists()){
			tempDirPath.mkdirs();
		}
		
		if (tempDirPath.exists() && tempDirPath.isDirectory() && tempDirPath.canWrite()) {
			return tempDirPath.getAbsolutePath();
		} else {
			Loggers.SERVER.error("PluginDataResolverImpl :: temp dir not writable: " + tempDirPath.getAbsolutePath());
			return tempDirPath.getAbsolutePath();
		}
	}

}
