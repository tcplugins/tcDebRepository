/*******************************************************************************
 * Copyright 2017 Net Wolf UK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package debrepo.teamcity.ebean.server;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebPackageStoreEntity;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.helper.DebRepositoryDatabaseXmlPersisterImpl;
import debrepo.teamcity.entity.helper.JaxDbFileRenamer;
import debrepo.teamcity.entity.helper.JaxHelper;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.XmlPersister;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import jetbrains.buildServer.serverSide.ProjectManager;

public class JaxToEbeanMigrationManager {
	
	private DebRepositoryManager myEbeanDebRepositoryManager;
	private XmlPersister<DebPackageStore, DebRepositoryConfiguration> myDebRepositoryDatabaseXmlPersister;
	private DebRepositoryConfigurationChangePersister jaxConfigChangePersister;
	private DebRepositoryManager myJaxDebRepositoryManager;
	private DebRepositoryConfigurationManager myJaxDebRepositoryConfigurationManager;
	private JaxDbFileRenamer myJaxDbFileRenamer;
	
	public JaxToEbeanMigrationManager(DebRepositoryManager ebeanDebRepositoryManager, 
									  ProjectManager projectManager,
									  PluginDataResolver pluginDataResolver,
									  DebRepositoryConfigurationChangePersister configurationJaxHelper,
									  JaxHelper<DebPackageStoreEntity> jaxDatabasePersister,
									  DebRepositoryConfigurationFactory debRepositoryConfigurationFactory,
									  JaxDbFileRenamer jaxDbFileRenamer) {
		this.myEbeanDebRepositoryManager = ebeanDebRepositoryManager;
		this.myDebRepositoryDatabaseXmlPersister = new DebRepositoryDatabaseXmlPersisterImpl(pluginDataResolver, jaxDatabasePersister);
		
		this.jaxConfigChangePersister = configurationJaxHelper;
		this.myJaxDbFileRenamer = jaxDbFileRenamer;
		this.myJaxDebRepositoryManager = new debrepo.teamcity.service.DebRepositoryManagerImpl(
														 projectManager, 
														 myDebRepositoryDatabaseXmlPersister, 
														 debRepositoryConfigurationFactory, 
														 jaxConfigChangePersister
													);
		this.myJaxDebRepositoryConfigurationManager = (DebRepositoryConfigurationManager) this.myJaxDebRepositoryManager;
	}
	
	public void doMigration() throws FileNotFoundException, JAXBException {
		Loggers.SERVER.info("JaxToEbeanMigrationManager :: Starting XML to EBean migration");
		JaxToEbeanMigrator migrator = new JaxToEbeanMigrator(myJaxDebRepositoryManager, myEbeanDebRepositoryManager, myJaxDbFileRenamer);
		this.myJaxDebRepositoryConfigurationManager.updateRepositoryConfigurations(this.jaxConfigChangePersister.readDebRespositoryConfigurationChanges());
		for (DebRepositoryConfiguration config : this.myJaxDebRepositoryConfigurationManager.getAllConfigurations()) {
			Loggers.SERVER.info("JaxToEbeanMigrationManager :: Will now migrate repo called " + config.getRepoName() + " (" + config.getUuid().toString() + ")");
			try {
				migrator.migrate(config);
			} catch (NonExistantRepositoryException e) {
				Loggers.SERVER.warn("JaxToEbeanMigrationManager :: Repo was not found in config whilst attempting migration of: "+ config.getRepoName() + "(" + config.getUuid().toString() + ")");
			}
		}
	}
	
}
