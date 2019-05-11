package debrepo.teamcity.ebean.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import debrepo.teamcity.RepoDataFileType;
import debrepo.teamcity.ebean.DebMetaDataFileModel;
import debrepo.teamcity.ebean.server.DebRepositoryManagerImpl.DistComponentArchImpl;
import debrepo.teamcity.entity.DebPackageEntity;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DistComponentArchitecture;
import debrepo.teamcity.entity.helper.DebRepositoryToReleaseDescriptionBuilder;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import debrepo.teamcity.entity.helper.ReleaseDescriptionBuilder;
import debrepo.teamcity.service.DebReleaseFileGenerator;
import debrepo.teamcity.settings.DebRepositoryConfigurationChangePersister;
import debrepo.teamcity.service.DebRepositoryBaseTest;
import debrepo.teamcity.service.DebRepositoryConfigurationManager;
import debrepo.teamcity.service.DebRepositoryManager;
import debrepo.teamcity.service.NonExistantRepositoryException;
import io.ebean.EbeanServer;
import jetbrains.buildServer.serverSide.ServerPaths;

public class DebReleaseFileGeneratorTest extends DebRepositoryBaseTest {
	
	ServerPaths serverPaths;
	PluginDataResolver pluginDataResolver;
	EbeanServerProvider ebeanServerProvider;
	
	ReleaseDescriptionBuilder realReleaseDescriptionBuilder;
	
	DebRepositoryConfiguration c;
	
	@Before
	public void setuplocal() throws NonExistantRepositoryException, IOException {
		
		serverPaths = mock(ServerPaths.class);
		ebeanServerProvider = mock(EbeanServerProvider.class);
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		
		pluginDataResolver = new PluginDataResolverImpl(serverPaths);
		debRepositoryConfigurationChangePersister = mock(DebRepositoryConfigurationChangePersister.class);
		
		EbeanServer ebeanServer = EbeanServerProviderImpl.createEbeanServerInstance(pluginDataResolver);
		when(ebeanServerProvider.getEbeanServer()).thenReturn(ebeanServer);
		super.setup();
		debRepositoryConfigManager = (DebRepositoryConfigurationManager) debRepositoryManager;
		when(projectManager.findProjectById("project01")).thenReturn(project01);
		when(project01.getExternalId()).thenReturn("My_Project_Name");
		when(project01.getDescription()).thenReturn("My Project Name - Long description");
		
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
		realReleaseDescriptionBuilder = new DebRepositoryToReleaseDescriptionBuilder(projectManager);
		return new DebRepositoryManagerImpl(ebeanServerProvider, debRepositoryConfigurationFactory, debRepositoryConfigurationChangePersister, realReleaseDescriptionBuilder);
	}
	
	@Test
	public void testUpdateReleaseFiles() throws NonExistantRepositoryException {
		
		Set<DistComponentArchitecture> distComponentsToUpdate = new HashSet<>();
		
		distComponentsToUpdate.add(new DistComponentArchImpl("wheezy", "main", "amd64"));
	
		DebReleaseFileGenerator generator = (DebReleaseFileGenerator) this.debRepositoryManager;
		generator.updateReleaseFiles(c, distComponentsToUpdate);
		generator.updateReleaseFiles(c, distComponentsToUpdate);
		
		List<DebMetaDataFileModel> releaseFiles = DebMetaDataFileModel.find.query()
				.where()
					.eq("fileName", RepoDataFileType.Release.getFileName())
				.findList();
		assertEquals(2, releaseFiles.size());
		for (DebMetaDataFileModel f : releaseFiles) {
			System.out.println("Release filePath: " + f.getFilePath());
			System.out.println("Release fileContent: " + new String(f.getFileContent()));
		}
		
		List<DebMetaDataFileModel> packageFiles = DebMetaDataFileModel.find.query()
			.where()
			.disjunction()
			  .eq("fileName", RepoDataFileType.Packages.getFileName())
			  .eq("fileName", RepoDataFileType.PackagesGz.getFileName())
			.findList();
		assertEquals(2, packageFiles.size());
	}

	@Test
	public void testUpdateReleaseFilesAndRemoveOldPackagesFiles() throws NonExistantRepositoryException {
		
		Set<debrepo.teamcity.entity.DistComponentArchitecture> distComponentsToUpdate = new HashSet<>();
		
		distComponentsToUpdate.add(new DistComponentArchImpl("wheezy", "main", "amd64"));
		distComponentsToUpdate.add(new DistComponentArchImpl("wheezy", "main", "i386"));
	
		DebReleaseFileGenerator generator = (DebReleaseFileGenerator) this.debRepositoryManager;
		for (int i = 0; i < 100; i++) {
			DebPackageEntity entityN = DebPackageEntity.copy(entity4);
			entityN.setVersion("2.1." + i);
			entityN.setFilename("testpackage-amd64-" + entityN.getVersion() +".deb");
			entityN.setUri("ProjectName/BuildName/" + entityN.getBuildId() + "/" + entityN.getFilename());
			debRepositoryManager.addBuildPackage(c, entityN);
			
			DebPackageEntity entityN2 = DebPackageEntity.copy(entity4);
			entityN2.setVersion("2.1." + i);
			entityN2.setArch("i386");
			entityN2.setFilename("testpackage-i386-" + entityN2.getVersion() +".deb");
			entityN2.setUri("ProjectName/BuildName/" + entityN2.getBuildId() + "/" + entityN2.getFilename());
			debRepositoryManager.addBuildPackage(c, entityN2);

			generator.updateReleaseFiles(c, distComponentsToUpdate);
		}
		
		List<DebMetaDataFileModel> files = DebMetaDataFileModel.find.query()
				.where()
				.disjunction()
					.eq("fileName", RepoDataFileType.Packages.getFileName())
					.eq("fileName", RepoDataFileType.PackagesGz.getFileName())
				.findList();		
		
		
		for (DebMetaDataFileModel f : files) {
			System.out.println("############  Packages filePath: " + f.getFilePath() + "(" + f.getMd5() + ")");
			System.out.println("Packages fileContent: " + new String(f.getFileContent()));
		}
		assertEquals(400, files.size());
		
		for (DistComponentArchitecture dca :distComponentsToUpdate) {
			this.debRepositoryManager.cleanupPackagesFiles(c, dca);
		}
		
		files = DebMetaDataFileModel.find.query()
				.where()
				.disjunction()
				.eq("fileName", RepoDataFileType.Packages.getFileName())
				.eq("fileName", RepoDataFileType.PackagesGz.getFileName())
				.findList();		
		assertEquals(20, files.size());
	}
	
}
