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
package debrepo.teamcity.ebean.server;

import java.io.File;
import java.io.IOException;

import org.avaje.datasource.DataSourceConfig;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import jetbrains.buildServer.util.FuncThrow;
import jetbrains.buildServer.util.Util;

public class EbeanServerProvider {

	EbeanServer myEbeanServer = null;
	
	/**
	 * EbeanServerProvider constructor for unit tests that does not need TeamCity running.
	 * 
	 * @param pluginDataResolver Used to find the directory to store the H2 files in.
	 */
	public EbeanServerProvider(PluginDataResolver pluginDataResolver) {
		Loggers.SERVER.info("EbeanServerProvider :: Getting EBeanServer via testing method.");
		this.myEbeanServer = createEbeanServerInstance(pluginDataResolver);
	}
	
	/** 
	 * EbeanServerProvider
	 * @param serverPaths The directory to store the H2 files in.
	 * @param teamCityClassLoader The classloader to use to create the instance. TeamCity will inject use useful one.
	 */
	public EbeanServerProvider(PluginDataResolver pluginDataResolver, ClassLoader teamCityClassLoader) {
		Loggers.SERVER.info("EbeanServerProvider :: Getting EBeanServer via TeamCity classpath method.");
		try {
			this.myEbeanServer = Util.doUnderContextClassLoader(teamCityClassLoader,
					new EbeanServerProviderInstantiationFunction(pluginDataResolver)
					);
		} catch (Exception e) {
			Loggers.SERVER.error("EbeanServerProvider :: Could not create eBean instance!");
			Loggers.SERVER.debug(e);
		}
	}
	
	public class EbeanServerProviderInstantiationFunction implements FuncThrow<EbeanServer, Exception> {
		final PluginDataResolver resolver;
		
		public EbeanServerProviderInstantiationFunction(PluginDataResolver pluginDataResolver) {
			this.resolver = pluginDataResolver;
		}

		@Override
		public EbeanServer apply() throws Exception {
			return createEbeanServerInstance(this.resolver);
		}
		
	}
	

	public EbeanServer createEbeanServerInstance(PluginDataResolver pluginDataResolver) {
		File myDataDir;
		try {
			myDataDir = new File(pluginDataResolver.getPluginDatabaseDirectory());
		} catch (IOException e) {
			Loggers.SERVER.error("tcDebRepository EbeanServerProvider : Failed to get database directory location");
			Loggers.SERVER.debug(e);
			return null;
		}

		if (myDataDir.exists() && myDataDir.isDirectory() && myDataDir.canWrite()) {
			ServerConfig config = new ServerConfig();
			config.setName("db");
			config.loadFromProperties();

			DataSourceConfig dsConfig = config.getDataSourceConfig();
			dsConfig.setUrl(
					"jdbc:h2:file:" + myDataDir.getAbsolutePath() + File.separator + "tcDebRepositoryDB;DB_CLOSE_ON_EXIT=FALSE");

			config.setDataSourceConfig(dsConfig);

			Loggers.SERVER.debug(config.getDataSourceConfig().getUsername());

			// set as default and register so that Model can be
			// used if desired for save() and update() etc
			config.setDefaultServer(true);
			config.setRegister(true);
			config.addPackage("debrepo.teamcity.ebean");
			
			Loggers.SERVER.debug("EbeanServerProvider :: ebean Properties are: " + config.getProperties().toString());

			return EbeanServerFactory.create(config);
		}
		return null;
	}

	public EbeanServer getEbeanServer() {
		return myEbeanServer;
	}
}
