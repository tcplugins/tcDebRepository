package debrepo.teamcity.ebean.server;

import java.io.File;

import org.avaje.datasource.DataSourceConfig;

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
				ServerConfig config = new ServerConfig();
				config.setName("db");
				config.loadFromProperties();

				DataSourceConfig dsConfig = config.getDataSourceConfig();
				dsConfig.setUrl(
						"jdbc:h2:file:" + myDataDir.getAbsolutePath() + File.separator + "tcDebRepositoryDB;DB_CLOSE_ON_EXIT=FALSE");

				config.setDataSourceConfig(dsConfig);

				System.out.println(config.getDataSourceConfig().getUsername());

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

	public EbeanServer getEbeanServer() {
		return myEbeanServer;
	}
}
