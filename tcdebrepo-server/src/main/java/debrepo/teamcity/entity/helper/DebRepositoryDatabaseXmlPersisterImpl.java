package debrepo.teamcity.entity.helper;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebPackageStoreEntity;

public class DebRepositoryDatabaseXmlPersisterImpl implements XmlPersister<DebPackageStore> {
	
	private final PluginDataResolver myPluginDataDirectoryResolver;
	private final JaxHelper<DebPackageStoreEntity> myDebRepositoryDatabaseJaxHelper;
	
	public DebRepositoryDatabaseXmlPersisterImpl(PluginDataResolver pluginDataDirectoryResolver, JaxHelper<DebPackageStoreEntity> debRepositoryDatabaseJaxHelper){
		this.myPluginDataDirectoryResolver = pluginDataDirectoryResolver;
		this.myDebRepositoryDatabaseJaxHelper = debRepositoryDatabaseJaxHelper;
		
	}
	
	@Override
	public boolean persistToXml(DebPackageStore debPackageStore) throws IOException {
		synchronized (debPackageStore) {
			
			DebPackageStoreEntity wrapperEntity = new DebPackageStoreEntity();
			wrapperEntity.setUuid(debPackageStore.getUuid());
			
			wrapperEntity.setPackages(debPackageStore.findAll());
			
			String configFilePath = this.myPluginDataDirectoryResolver.getPluginDatabaseDirectory() 
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
