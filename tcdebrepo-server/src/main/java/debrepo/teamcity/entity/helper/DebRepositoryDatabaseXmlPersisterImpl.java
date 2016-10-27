package debrepo.teamcity.entity.helper;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebPackageStoreEntity;

public class DebRepositoryDatabaseXmlPersisterImpl implements DebRepositoryDatabaseXmlPersister {
	
	private final PluginDataDirectoryResolver myPluginDataDirectoryResolver;
	private final DebRepositoryDatabaseJaxHelper myDebRepositoryDatabaseJaxHelper;
	
	public DebRepositoryDatabaseXmlPersisterImpl(PluginDataDirectoryResolver pluginDataDirectoryResolver, DebRepositoryDatabaseJaxHelper debRepositoryDatabaseJaxHelper){
		this.myPluginDataDirectoryResolver = pluginDataDirectoryResolver;
		this.myDebRepositoryDatabaseJaxHelper = debRepositoryDatabaseJaxHelper;
		
	}
	
	public boolean persistDatabaseToXml(DebPackageStore debPackageStore) throws IOException{
		synchronized (debPackageStore) {
			
			DebPackageStoreEntity wrapperEntity = new DebPackageStoreEntity();
			wrapperEntity.setUuid(debPackageStore.getUuid());
			
			wrapperEntity.setPackages(debPackageStore.findAll());
			
			String configFilePath = this.myPluginDataDirectoryResolver.getPluginDataDirectory() 
									+ File.separator + debPackageStore.getUuid().toString() + ".xml";
					
			try {
				this.myDebRepositoryDatabaseJaxHelper.write(wrapperEntity, configFilePath);
				return true;
			} catch (JAXBException jaxbException){
				Loggers.SERVER.debug(jaxbException);
				return false;
			} finally {
				wrapperEntity = null;
			}
		}
	}

}
