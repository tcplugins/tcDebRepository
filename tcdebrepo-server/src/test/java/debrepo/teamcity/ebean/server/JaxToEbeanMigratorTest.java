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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.ebean.server.EbeanServerProvider;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebPackageStoreEntity;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.helper.DebRepositoryDatabaseJaxHelperImpl;
import debrepo.teamcity.entity.helper.DebRepositoryDatabaseXmlPersisterImpl;
import debrepo.teamcity.entity.helper.JaxDbFileRenamer;
import debrepo.teamcity.entity.helper.JaxDbFileRenamerImpl;
import debrepo.teamcity.entity.helper.JaxHelper;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.entity.helper.XmlPersister;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationFactoryImpl;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.ServerPaths;

public class JaxToEbeanMigratorTest {
	
	
	@Mock ServerPaths jaxServerPaths, ebeanServerPaths;
	PluginDataResolver jaxPluginDataResolver, ebeanPluginDataResolver;
	@Mock protected ProjectManager projectManager;
	
	JaxHelper<DebPackageStoreEntity> jaxHelper = new DebRepositoryDatabaseJaxHelperImpl();
	XmlPersister<DebPackageStore, DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister;
	@Mock protected DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister;
	@Mock JaxDbFileRenamer jaxDbFileRenamer;
	
	EbeanServerProvider ebeanServerProvider;
	DebRepositoryManager jaxDebRepositoryManager, ebeanDebRepositoryManager;
	
	protected DebRepositoryConfigurationManager debRepositoryConfigManager;
	protected DebRepositoryConfigurationFactory debRepositoryConfigurationFactory = new DebRepositoryConfigurationFactoryImpl();
	
	@Before
	public void setuplocal() throws NonExistantRepositoryException, IOException {
		MockitoAnnotations.initMocks(this);
		when(jaxServerPaths.getPluginDataDirectory()).thenReturn(new File("src/test/resources/testplugindata"));
		when(ebeanServerPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		
		jaxPluginDataResolver = new PluginDataResolverImpl(jaxServerPaths);
		ebeanPluginDataResolver = new PluginDataResolverImpl(ebeanServerPaths);
		ebeanServerProvider = new EbeanServerProvider(ebeanPluginDataResolver);
		
		debRepositoryDatabaseXmlPersister = new DebRepositoryDatabaseXmlPersisterImpl(jaxPluginDataResolver, jaxHelper);
		jaxDebRepositoryManager = new debrepo.teamcity.service.DebRepositoryManagerImpl(projectManager, debRepositoryDatabaseXmlPersister, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		ebeanDebRepositoryManager = new debrepo.teamcity.ebean.server.DebRepositoryManagerImpl(ebeanServerProvider.getEbeanServer(), debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister);
		debRepositoryConfigManager = (DebRepositoryConfigurationManager) ebeanDebRepositoryManager;
		
	}

	@Test
	public void testMigrate() throws NonExistantRepositoryException {
		DebRepositoryConfigurationJaxImpl c = new DebRepositoryConfigurationJaxImpl("project01", "MyStoreName");
		//c.setUuid(UUID.fromString("eafee234-c753-4a7b-9221-6b208eac4ab6"));
		c.setUuid(UUID.fromString("a187bd92-b22d-43ea-98ce-55ec2cedb942"));
		debRepositoryConfigManager.addDebRepository(c);
		jaxDebRepositoryManager.initialisePackageStore(c);
		JaxToEbeanMigrator migrator = new JaxToEbeanMigrator(jaxDebRepositoryManager, ebeanDebRepositoryManager, jaxDbFileRenamer);
		migrator.migrate(c);
		verify(jaxDbFileRenamer, times(1)).renameToBackup(c);
	}
	
}
