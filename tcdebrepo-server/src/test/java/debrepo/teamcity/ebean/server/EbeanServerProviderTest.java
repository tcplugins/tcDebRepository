package debrepo.teamcity.ebean.server;

import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.ebean.DebMetaDataFileModel;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import jetbrains.buildServer.serverSide.ServerPaths;

public class EbeanServerProviderTest {

	@Mock ServerPaths serverPaths;
	PluginDataResolver pluginDataResolver;
	
	@Test
	public void test() {

		MockitoAnnotations.initMocks(this);
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		
		pluginDataResolver = new PluginDataResolverImpl(serverPaths);
		
		EbeanServerProviderImpl provider = new EbeanServerProviderImpl(
				pluginDataResolver, 
				this.getClass().getClassLoader()
			);
		
		provider.init();
		//EbeanServer ebeanServer = provider.getEbeanServer();
		
		DebMetaDataFileModel.find.all();
	}

}
