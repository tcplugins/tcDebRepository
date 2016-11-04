package debrepo.teamcity.settings;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import debrepo.teamcity.entity.DebRepositoryConfigurations;
import debrepo.teamcity.entity.helper.JaxHelper;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.service.DebRepositoryManager;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebRepositoryConfigFileChangeHandler implements ChangeListener, DebRepoConfigChangeHandler {

	final DebRepositoryManager myDebRepoManager;
	final JaxHelper<DebRepositoryConfigurations> jaxHelper;
	final PluginDataResolver myPluginDataResolver;
	File configFile;
	FileWatcher fw;
	final ServerPaths serverPaths;
	
	public DebRepositoryConfigFileChangeHandler(
			ServerPaths serverPaths, DebRepositoryManager debRepositoryManager, 
			JaxHelper<DebRepositoryConfigurations> jaxHelper, PluginDataResolver pluginDataResolver) {
		this.myDebRepoManager = debRepositoryManager;
		this.jaxHelper = jaxHelper;
		this.serverPaths = serverPaths;
		this.myPluginDataResolver = pluginDataResolver;
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
		try {
			DebRepositoryConfigurations repoConfigurations =  jaxHelper.read(configFile.getPath());
			this.myDebRepoManager.updateRepositoryConfigurations(repoConfigurations);
		} catch (FileNotFoundException e) {
			Loggers.SERVER.warn("DebRepositoryConfigFileChangeHandler :: Exception occurred attempting to reload DebRepositoryConfigurations. File not found: " + this.configFile.getPath());
			Loggers.SERVER.debug(e);
		} catch (JAXBException e) {
			Loggers.SERVER.warn("DebRepositoryConfigFileChangeHandler :: Exception occurred attempting to reload DebRepositoryConfigurations. Could not parse: " + this.configFile.getPath());
			Loggers.SERVER.debug(e);
		}
		
	}
	
	

}
