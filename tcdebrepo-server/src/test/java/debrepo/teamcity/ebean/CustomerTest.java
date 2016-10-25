package debrepo.teamcity.ebean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.ebean.server.EbeanServerProvider;
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
		c.name = "netwolfuk";
		c.save();
		
		//fail("Not yet implemented");
		assertEquals(1,Customer.find.where().ilike("name", "netwolfuk").findCount());
	}
	
}
