package debrepo.teamcity.entity.helper;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.entity.DebRepositoryBuildTypeConfig;
import debrepo.teamcity.entity.DebRepositoryConfiguration;
import debrepo.teamcity.entity.DebRepositoryConfigurations;

import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import jetbrains.buildServer.serverSide.ServerPaths;

public class DebRepositoryConfigurationJaxHelperImplTest {
	
	@Mock ServerPaths serverPaths;
	PluginDataResolver pathResolver;
	JaxHelper<DebRepositoryConfigurations> configJaxHelper= new DebRepositoryConfigurationJaxHelperImpl();
	DebRepositoryConfigurations repositoryConfigurations = new DebRepositoryConfigurations();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		pathResolver = new PluginDataResolverImpl(serverPaths);
		when(serverPaths.getConfigDir()).thenReturn("target");
		
	}
	
	@Test
	public void testReadString() throws JAXBException, IOException {
		DebRepositoryConfiguration config01 = new DebRepositoryConfiguration("project01", "TestRepoName01");
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt01").af("*.deb").af("/prod/somthing*.deb"));
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt02", "*.deb"));
		repositoryConfigurations.getDebRepositoryConfigurations().add(config01);
		configJaxHelper.write(repositoryConfigurations, pathResolver.getPluginConfigurationFile());	
		
		DebRepositoryConfigurations readConfig = configJaxHelper.read(pathResolver.getPluginConfigurationFile());
		assertEquals(config01, readConfig.getDebRepositoryConfigurations().get(0));
	}

	@Test
	public void testWrite() throws JAXBException, IOException {
		DebRepositoryConfiguration config01 = new DebRepositoryConfiguration("project01", "TestRepoName01");
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt01").af("*.deb").af("/prod/somthing*.deb"));
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt02", "*.deb"));
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt03", "*.deb"));
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt04", "*.deb"));
		config01.addBuildType(new DebRepositoryBuildTypeConfig("bt05", "*.deb"));
		repositoryConfigurations.getDebRepositoryConfigurations().add(config01);
		configJaxHelper.write(repositoryConfigurations, pathResolver.getPluginConfigurationFile());
	}

}
