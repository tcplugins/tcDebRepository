package debrepo.teamcity.ebean.server;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.ebean.DebPackagesFileModel;
import debrepo.teamcity.ebean.server.DebRepositoryManagerImpl.DistComponentArchImpl;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.helper.DebRepositoryToReleaseDescriptionBuilder;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.entity.helper.ReleaseDescriptionBuilder;
import debrepo.teamcity.service.DebReleaseFileGenerator;
import debrepo.teamcity.service.DebReleaseFileGenerator.DistComponentArchitecture;
import debrepo.teamcity.service.DebRepositoryBaseTest;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebReleaseFileGeneratorTest extends DebRepositoryBaseTest {
	
	@Mock ServerPaths serverPaths;
	PluginDataResolver pluginDataResolver;
	
	DebRepositoryManager debRepositoryManager;
	ReleaseDescriptionBuilder realReleaseDescriptionBuilder;
	
	DebRepositoryConfiguration c;
	
	@Before
	public void setuplocal() throws NonExistantRepositoryException, IOException {
		MockitoAnnotations.initMocks(this);
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		when(projectManager.findProjectById("project01")).thenReturn(project01);
		when(project01.getExternalId()).thenReturn("My_Project_Name");
		when(project01.getDescription()).thenReturn("My Project Name - Long description");
		
		pluginDataResolver = new PluginDataResolverImpl(serverPaths);
		realReleaseDescriptionBuilder = new DebRepositoryToReleaseDescriptionBuilder(projectManager);
		debRepositoryManager = new DebRepositoryManagerImpl(EbeanServerProvider.createEbeanServerInstance(pluginDataResolver), debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister, realReleaseDescriptionBuilder);;
		debRepositoryConfigManager = (DebRepositoryConfigurationManager) debRepositoryManager;
		
		c = getDebRepoConfig1();
		c.setRepoName("blahBlah01");
		debRepositoryConfigManager.addDebRepository(c);
		
		debRepositoryManager.addBuildPackage(c, entity);
		debRepositoryManager.addBuildPackage(c, entity2);
		debRepositoryManager.addBuildPackage(c, entity3);
		debRepositoryManager.addBuildPackage(c, entity4);
		
		System.out.println(debRepositoryManager.getRepositoryStatistics(c, "myUrl").getTotalPackageCount());
		assertTrue(debRepositoryManager.getRepositoryStatistics(c, "myUrl").getTotalPackageCount() == 4);
	}
	
	@Override
	public DebRepositoryManager getDebRepositoryManager() throws NonExistantRepositoryException, IOException {
		setuplocal();
		return debRepositoryManager;
	}
	
	@Test
	public void testUpdateReleaseFiles() throws NonExistantRepositoryException {
		
		Set<DistComponentArchitecture> distComponentsToUpdate = new HashSet<>();
		
		distComponentsToUpdate.add(new DistComponentArchImpl("wheezy", "main", "amd64"));
	
		DebReleaseFileGenerator generator = (DebReleaseFileGenerator) this.debRepositoryManager;
		generator.updateReleaseFiles(c, distComponentsToUpdate);
		generator.updateReleaseFiles(c, distComponentsToUpdate);
		
		List<DebPackagesFileModel> files = DebPackagesFileModel.find.all();
		for (DebPackagesFileModel f : files) {
			System.out.println(f.getFilePath());
			System.out.println(new String(f.getPackagesFile()));
		}
	}

}
