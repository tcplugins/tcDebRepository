/*******************************************************************************
 *
 *  Copyright 2016 Net Wolf UK
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  
 *******************************************************************************/
package debrepo.teamcity.settings;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import debrepo.teamcity.entity.DebRepositoryConfigurations;
import debrepo.teamcity.entity.helper.JaxHelper;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import jetbrains.buildServer.log.Loggers;

public class DebRepositoryConfigurationChangePersisterImpl implements DebRepositoryConfigurationChangePersister {
	
	private JaxHelper<DebRepositoryConfigurations> jaxHelper;
	private File configFile;

	public DebRepositoryConfigurationChangePersisterImpl(JaxHelper<DebRepositoryConfigurations> jaxHelper, 
			PluginDataResolver pluginDataResolver) {
		this.jaxHelper = jaxHelper;
		
		Loggers.SERVER.info("DebRepositoryConfigurationChangePersisterImpl :: Registering");
		this.configFile = new File(pluginDataResolver.getPluginConfigurationFile());
		
	}

	@Override
	public DebRepositoryConfigurations readDebRespositoryConfigurationChanges() throws FileNotFoundException, JAXBException {
		synchronized (configFile) {
			return jaxHelper.read(configFile.getPath());
		}
	}

	@Override
	public void writeDebRespositoryConfigurationChanges(DebRepositoryConfigurations configurations) throws JAXBException {
		synchronized (configFile) {
			jaxHelper.write(configurations, configFile.getPath());
		}
	}

}
