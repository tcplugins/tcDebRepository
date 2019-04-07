/*******************************************************************************
 *
 *  Copyright 2016, 2017 Net Wolf UK
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

import io.ebean.datasource.DataSourceConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import jetbrains.buildServer.util.FuncThrow;
import jetbrains.buildServer.util.Util;

public class EbeanServerProviderImpl implements EbeanServerProvider
{

	EbeanServer myEbeanServer = null;
	private ClassLoader myTeamCityClassLoader;
	private PluginDataResolver myPluginDataResolver;
	
	/** 
	 * EbeanServerProvider
	 * @param serverPaths The directory to store the H2 files in.
	 * @param teamCityClassLoader The classloader to use to create the instance. TeamCity will inject use useful one.
	 */
	public EbeanServerProviderImpl(PluginDataResolver pluginDataResolver, ClassLoader teamCityClassLoader) {
		Loggers.SERVER.info("EbeanServerProvider :: Getting EBeanServer via TeamCity classpath method.");
		this.myTeamCityClassLoader = teamCityClassLoader;
		this.myPluginDataResolver = pluginDataResolver;
	}
	
	public void init() {
		Loggers.SERVER.info("EbeanServerProvider :: Initialising EBeanServer via TeamCity classpath method.");
		try {
			for (Resource r : Util.doUnderContextClassLoader(myTeamCityClassLoader,
					new ClassLoaderPrinter(myTeamCityClassLoader)
					)) {
				Loggers.SERVER.info("Found resource " + r.getClass().getCanonicalName() );
			}
			this.myEbeanServer = Util.doUnderContextClassLoader(myTeamCityClassLoader,
					new EbeanServerProviderInstantiationFunction(myPluginDataResolver)
					);
		} catch (Exception e) {
			Loggers.SERVER.error("EbeanServerProvider :: Could not create eBean instance!");
			Loggers.SERVER.info(e);
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
	
	public class ClassLoaderPrinter implements FuncThrow<Resource[], Exception> {
		final ClassLoader classLoader;
		
		public ClassLoaderPrinter(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		@Override
		public Resource[] apply() throws Exception {
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.classLoader);
			return resolver.getResources("classpath:**");
		}
		
	}
	
	/**
	 * EbeanServer builder.
	 * Called by EbeanServerProviderInstantiationFunction.apply() and also
	 * is public so that it can be used by unit tests which don't have TeamCity running.
	 */
	public static EbeanServer createEbeanServerInstance(PluginDataResolver pluginDataResolver) {
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

	@Override
	public EbeanServer getEbeanServer() {
		return this.myEbeanServer;
	}

//	@Override
//	public EbeanServer getObject() throws Exception {
//		return this.myEbeanServer;
//	}
//
//	@Override
//	public Class<?> getObjectType() {
//		return EbeanServer.class;
//	}
//
//	@Override
//	public boolean isSingleton() {
//		return true;
//	}
}
