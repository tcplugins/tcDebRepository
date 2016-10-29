package debrepo.teamcity.entity.helper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.serverSide.ServerPaths;

public class PluginDataResolverImplTest {
	
	@Mock ServerPaths serverPaths;
	File f;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGetPluginDatabaseDirectory() throws IOException {
		f = new File(".BuildServer/pluginData");
		when(serverPaths.getPluginDataDirectory()).thenReturn(f);
		when(serverPaths.getConfigDir()).thenReturn(f.getAbsolutePath());
		PluginDataResolverImpl resolver = new PluginDataResolverImpl(serverPaths);
		System.out.println(resolver.getPluginDatabaseDirectory());
		assertEquals(f.getAbsolutePath() + "/tcDebRepository/database", resolver.getPluginDatabaseDirectory());
	}

	@Test
	public void testGetPluginConfigurationFile() throws IOException {
		f = new File(".BuildServer/config");
		when(serverPaths.getPluginDataDirectory()).thenReturn(f);
		when(serverPaths.getConfigDir()).thenReturn(f.getAbsolutePath());
		PluginDataResolverImpl resolver = new PluginDataResolverImpl(serverPaths);
		System.out.println(resolver.getPluginConfigurationFile());
		assertEquals(f.getAbsolutePath() + "/deb-repositories.xml", resolver.getPluginConfigurationFile());
	}

}
