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
package debrepo.teamcity.settings;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import debrepo.teamcity.entity.DebRepositoryConfigurations;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebRepositoryConfigFileChangeHandler implements ChangeListener, DebRepoConfigChangeHandler {

	final DebRepositoryManager myDebRepoManager;
	final DebRepositoryConfigurationManager myDebRepoConfigManager;
	final PluginDataResolver myPluginDataResolver;
	final DebRepositoryConfigurationChangePersister myChangePersister;
	File configFile;
	FileWatcher fw;
	
	public DebRepositoryConfigFileChangeHandler(
			ServerPaths serverPaths, DebRepositoryManager debRepositoryManager, DebRepositoryConfigurationManager debRepositoryConfigManager, 
			PluginDataResolver pluginDataResolver, DebRepositoryConfigurationChangePersister changePersister) {
		this.myDebRepoManager = debRepositoryManager;
		this.myDebRepoConfigManager = debRepositoryConfigManager;
		this.myPluginDataResolver = pluginDataResolver;
		this.myChangePersister = changePersister;
		Loggers.SERVER.info("DebRepositoryConfigFileChangeHandler :: Starting");
	}
	
	public void register(){
		Loggers.SERVER.info("DebRepositoryConfigFileChangeHandler :: Registering");
		this.configFile = new File(myPluginDataResolver.getPluginConfigurationFile());
		
		this.fw = new FileWatcher(configFile);

		this.changeOccured("Startup");
		
		this.fw.registerListener(this);
		this.fw.start();
		
		Loggers.SERVER.info("DebRepositoryConfigFileChangeHandler :: Watching for changes to file: " + this.configFile.getPath());
	}

	@Override
	public void changeOccured(String requestor) {
		Loggers.SERVER.info("DebRepositoryConfigFileChangeHandler :: Handling change to file: " + this.configFile.getPath() + " requested by " + requestor);
		Loggers.SERVER.debug("DebRepositoryConfigFileChangeHandler :: My instance is: " + this.toString() + " :: DebRepositoryManager: " + myDebRepoManager.toString());
		this.handleConfigFileChange();

	}

	@Override
	public void handleConfigFileChange() {
		synchronized (configFile) {
			try {
				DebRepositoryConfigurations repoConfigurations =  myChangePersister.readDebRespositoryConfigurationChanges();
				this.myDebRepoConfigManager.updateRepositoryConfigurations(repoConfigurations);
			} catch (FileNotFoundException e) {
				Loggers.SERVER.warn("DebRepositoryConfigFileChangeHandler :: Exception occurred attempting to reload DebRepositoryConfigurations. File not found: " + this.configFile.getPath());
				Loggers.SERVER.debug(e);
			} catch (JAXBException e) {
				Loggers.SERVER.warn("DebRepositoryConfigFileChangeHandler :: Exception occurred attempting to reload DebRepositoryConfigurations. Could not parse: " + this.configFile.getPath());
				Loggers.SERVER.debug(e);
			}
		}
	}

}
