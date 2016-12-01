package debrepo.teamcity.ebean.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.avaje.datasource.DataSourceConfig;
import org.avaje.datasource.DataSourceFactory;
import org.avaje.datasource.DataSourcePool;
import org.avaje.datasource.Factory;
import org.avaje.datasource.pool.ConnectionPool;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.FuncThrow;
import jetbrains.buildServer.util.Util;

public class EbeanServerProvider {

	EbeanServer myEbeanServer = null;
	
	/**
	 * EbeanServerProvider constructor for unit tests that does not need TeamCity running.
	 * 
	 * @param pluginDataResolver Used to find the directory to store the H2 files in.
	 */
	protected EbeanServerProvider(PluginDataResolver pluginDataResolver) {
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

			// load test-ebean.properties if present for running tests
			// typically using H2 in memory database
			// config.loadTestProperties();

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

	public EbeanServer createEbeanServerInstance2(File pluginDataDirectory) {

		if (pluginDataDirectory.exists() && pluginDataDirectory.isDirectory() && pluginDataDirectory.canWrite()) {
			File myDataDir = new File(pluginDataDirectory + File.separator + "tcDebRepository");
			if (!myDataDir.exists()) {
				if (myDataDir.mkdir()){
					Loggers.SERVER.info("tcDebRepository EbeanServerProvider : Created directory " + myDataDir.getAbsolutePath());
				} else {
				    Loggers.SERVER.error("tcDebRepository EbeanServerProvider : Failed to create directory " + myDataDir.getAbsolutePath());
				}
			}

			if (myDataDir.exists() && myDataDir.isDirectory() && myDataDir.canWrite()) {
				Properties props = null;
				try {
					final ClassLoader[] classLoaders = {EbeanServerProvider.class.getClassLoader(), ClassLoader.getSystemClassLoader()}; 
					for (ClassLoader cl : classLoaders){
						props = PropertiesLoaderUtils.loadAllProperties("ebean.properties", cl);
					}
				} catch (IOException e) {
					Loggers.SERVER.error("EbeanServerProvider :: Failed to load ebean.properties");
					Loggers.SERVER.debug(e);
				}
				
				Loggers.SERVER.debug("tcDebRepository:: ebean Properties are: " + props.toString());
				
				ServerConfig config = new ServerConfig();
				config.setName("db");
				config.loadFromProperties();
				config.addPackage("debrepo.teamcity.ebean");

				DataSourceConfig dsConfig = config.getDataSourceConfig();
				
				dsConfig.setUrl(
						"jdbc:h2:file:" + myDataDir.getAbsolutePath() + File.separator + "tcDebRepositoryDB;DB_CLOSE_ON_EXIT=FALSE");

				
//				DataSourcePool pool = new ConnectionPool("db", dsConfig);
//				
//				config.setDataSource(pool);
				config.setDataSourceConfig(dsConfig);
				Loggers.SERVER.debug("tcDebRepository:: ebean dsConfig are: " + dsConfig);

				Loggers.SERVER.debug(dsConfig.getUsername());
				Loggers.SERVER.debug(config.getDataSourceConfig().getUsername());

				// load test-ebean.properties if present for running tests
				// typically using H2 in memory database
				// config.loadTestProperties();

				// set as default and register so that Model can be
				// used if desired for save() and update() etc
				config.setDefaultServer(true);
				config.setRegister(true);

				return EbeanServerFactory.create(config);
			}
		}
		return null;
	}
	
	private URL findPropertiesFileUrlInVariousClassloaders(String propertiesFile) {
		final ClassLoader[] classLoaders = {EbeanServerProvider.class.getClassLoader(), ClassLoader.getSystemClassLoader()}; 
		URL url = null;
		for (ClassLoader cl : classLoaders){
			if (cl != null){
				url = cl.getResource(propertiesFile);
		        if (url != null){
		        	break;
		        }
			}
		}
		return url;
	}

	public EbeanServer getEbeanServer() {
		return myEbeanServer;
	}
}
