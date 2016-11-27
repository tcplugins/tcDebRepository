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
