package debrepo.teamcity.settings;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import debrepo.teamcity.entity.DebRepositoryConfigurations;

public interface DebRepositoryConfigurationChangePersister {
	
	public DebRepositoryConfigurations readDebRespositoryConfigurationChanges() throws FileNotFoundException, JAXBException;
	public void writeDebRespositoryConfigurationChanges (DebRepositoryConfigurations configurations) throws JAXBException;

}
