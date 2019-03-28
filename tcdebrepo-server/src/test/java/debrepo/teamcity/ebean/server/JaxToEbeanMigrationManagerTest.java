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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.Loggers;
import debrepo.teamcity.entity.DebPackageStore;
import debrepo.teamcity.entity.DebPackageStoreEntity;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurationJaxImpl;
import debrepo.teamcity.entity.DebRepositoryConfigurations;
import debrepo.teamcity.entity.helper.DebRepositoryConfigurationJaxHelperImpl;
import debrepo.teamcity.entity.helper.DebRepositoryDatabaseJaxHelperImpl;
import debrepo.teamcity.entity.helper.DebRepositoryToReleaseDescriptionBuilder;
import debrepo.teamcity.entity.helper.JaxDbFileRenamer;
import debrepo.teamcity.entity.helper.JaxHelper;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.entity.helper.ReleaseDescriptionBuilder;
import debrepo.teamcity.entity.helper.XmlPersister;
import debrepo.teamcity.service.DebReleaseFileGenerator;
import debrepo.teamcity.service.DebRepositoryConfigurationFactory;
import debrepo.teamcity.service.DebRepositoryConfigurationFactoryImpl;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersisterImpl;
import io.ebean.EbeanServer;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.ServerPaths;

public class JaxToEbeanMigrationManagerTest {
	
	@Mock ServerPaths jaxServerPaths, ebeanServerPaths;
	PluginDataResolver jaxPluginDataResolver, ebeanPluginDataResolver;
	@Mock protected ProjectManager projectManager;
	ReleaseDescriptionBuilder releaseDescriptionBuilder;
	@Mock SProject project;
	
	//JaxHelper<DebPackageStoreEntity> jaxHelper = new DebRepositoryDatabaseJaxHelperImpl();
	XmlPersister<DebPackageStore, DebRepositoryConfiguration> debRepositoryDatabaseXmlPersister;
	@Mock protected DebRepositoryConfigurationChangePersister debRepositoryConfigurationChangePersister;
	@Mock JaxDbFileRenamer jaxDbFileRenamer;
	
	@Mock EbeanServerProvider ebeanServerProvider;
	DebRepositoryManager jaxDebRepositoryManager, ebeanDebRepositoryManager;
	DebRepositoryConfigurationJaxImpl config;
	
	protected DebRepositoryConfigurationManager debRepositoryConfigManager;
	protected DebRepositoryConfigurationFactory debRepositoryConfigurationFactory = new DebRepositoryConfigurationFactoryImpl();
	

	@Test
	public void testDoMigration() throws FileNotFoundException, JAXBException {
		
		MockitoAnnotations.initMocks(this);
		when(jaxServerPaths.getPluginDataDirectory()).thenReturn(new File("src/test/resources/testplugindata"));
		when(jaxServerPaths.getConfigDir()).thenReturn("src/test/resources/testplugindata/config");
		when(ebeanServerPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		when(projectManager.findProjectById("project01")).thenReturn(project);
		when(project.getExternalId()).thenReturn("My_Project_Name");
		when(project.getDescription()).thenReturn("My Project Name - Long description");
		
		jaxPluginDataResolver = new PluginDataResolverImpl(jaxServerPaths);
		ebeanPluginDataResolver = new PluginDataResolverImpl(ebeanServerPaths);
		releaseDescriptionBuilder = new DebRepositoryToReleaseDescriptionBuilder(projectManager);
		
		EbeanServer ebeanServer = EbeanServerProviderImpl.createEbeanServerInstance(ebeanPluginDataResolver);
		when(ebeanServerProvider.getEbeanServer()).thenReturn(ebeanServer);

		
		DebRepositoryManager ebeanDebRepositoryManager = new DebRepositoryManagerImpl(
				ebeanServerProvider, 
				debRepositoryConfigurationFactory, 
				debRepositoryConfigurationChangePersister,
				releaseDescriptionBuilder);
		
		
		
		DebRepositoryConfigurationChangePersister noOpChangePersister = new DebRepositoryConfigurationChangePersisterImpl (
		 		new NoOpConfigChangePersister(), 
		 		jaxPluginDataResolver
		 );
		
		JaxToEbeanMigrationManager m = new JaxToEbeanMigrationManager(
				ebeanDebRepositoryManager, 
				(DebReleaseFileGenerator) ebeanDebRepositoryManager, 
				projectManager, 
				jaxPluginDataResolver, 
				noOpChangePersister, 
				new NoOpJaxDBPersister(), 
				debRepositoryConfigurationFactory, 
				jaxDbFileRenamer);
		
		m.doMigration();
		
		verify(jaxDbFileRenamer, times(2)).renameToBackup(any(DebRepositoryConfiguration.class));
	}
	
	public static class NoOpJaxDBPersister extends DebRepositoryDatabaseJaxHelperImpl implements JaxHelper<DebPackageStoreEntity> {

		@Override
		public void write(@NotNull DebPackageStoreEntity packages,
				@NotNull String configFilePath) throws JAXBException {
			Loggers.SERVER.info("Not persisting the DB changes for JAX Persister because this is inside a test.");
		}
		
	}
	public static class NoOpConfigChangePersister extends DebRepositoryConfigurationJaxHelperImpl implements JaxHelper<DebRepositoryConfigurations> {
		
		@Override
		public void write(DebRepositoryConfigurations jaxObject, String configFilePath) throws JAXBException {
			Loggers.SERVER.info("Not persisting the config changes for JAX Persister because this is inside a test.");
		}
		
	}

}
