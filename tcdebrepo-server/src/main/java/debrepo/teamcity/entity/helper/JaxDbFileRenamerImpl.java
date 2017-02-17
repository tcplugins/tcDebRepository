/*******************************************************************************
 * Copyright 2017 Net Wolf UK
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
