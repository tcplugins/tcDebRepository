package debrepo.teamcity.ebean.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.ebean.Customer;
import debrepo.teamcity.ebean.server.EbeanServerProvider;
import jetbrains.buildServer.plugins.classLoaders.TeamCityClassLoader;
import jetbrains.buildServer.serverSide.ServerPaths;

public class CustomerTest {

	@Mock ServerPaths serverPaths;
	
	EbeanServerProvider ebeanServerProvider;
	
	@Test
	public void test() throws Exception {
		
		MockitoAnnotations.initMocks(this);
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		
		ebeanServerProvider = new EbeanServerProvider(serverPaths);
		
		//EbeanServer server = ebeanServerProvider.createEbeanServerInstance(pluginDataDirectory);
		
		Customer c = new Customer();
		c.setName("netwolfuk");
		c.save();
		
		//fail("Not yet implemented");
		assertEquals(1,Customer.find.where().ilike("name", "netwolfuk").findCount());
	}
	
}
