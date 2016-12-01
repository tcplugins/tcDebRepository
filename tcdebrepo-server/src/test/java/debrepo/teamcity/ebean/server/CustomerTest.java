package debrepo.teamcity.ebean.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import debrepo.teamcity.ebean.Customer;
import debrepo.teamcity.entity.helper.PluginDataResolver;
import debrepo.teamcity.entity.helper.PluginDataResolverImpl;
import jetbrains.buildServer.serverSide.ServerPaths;

public class CustomerTest {

	@Mock ServerPaths serverPaths;
	PluginDataResolver pluginDataResolver;
	
	EbeanServerProvider ebeanServerProvider;
	
	@Test
	public void test() throws Exception {
		
		MockitoAnnotations.initMocks(this);
		when(serverPaths.getPluginDataDirectory()).thenReturn(new File("target"));
		
		pluginDataResolver = new PluginDataResolverImpl(serverPaths);
		ebeanServerProvider = new EbeanServerProvider(pluginDataResolver);
		
		Customer c = new Customer();
		c.setName("netwolfuk");
		c.save();
		
		assertEquals(1,Customer.find.where().ilike("name", "netwolfuk").findCount());
	}
	
}
