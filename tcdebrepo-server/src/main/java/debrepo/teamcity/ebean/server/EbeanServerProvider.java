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
import jetbrains.buildServer.serverSide.ServerPaths;

public class EbeanServerProvider {

	final EbeanServer myEbeanServer;

	public EbeanServerProvider(ServerPaths serverPaths) {
		this.myEbeanServer = createEbeanServerInstance(serverPaths.getPluginDataDirectory());
	}

	public EbeanServer createEbeanServerInstance(File pluginDataDirectory) {

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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Loggers.SERVER.debug("tcDebRepository:: ebean Properties are: " + props);
				
				ServerConfig config = new ServerConfig();
				config.setName("db");
				config.loadFromProperties();

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
