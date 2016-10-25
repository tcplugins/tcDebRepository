package debrepo.teamcity.ebean;

import static org.junit.Assert.*;

import org.avaje.datasource.DataSourceConfig;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.io.File;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;

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
	
	public EbeanServer getObject() throws Exception {

		  ServerConfig config = new ServerConfig();
		  config.setName("db");
		  //config.setDatabase
		  config.loadFromProperties();
		  
		  DataSourceConfig dsConfig = config.getDataSourceConfig();
		  dsConfig.setUrl("jdbc:h2:file:~/h2-test-db;DB_CLOSE_ON_EXIT=FALSE");
		  
		  config.setDataSourceConfig(dsConfig);
		  
		  System.out.println(config.getDataSourceConfig().getUsername());
		  
		  // load test-ebean.properties if present for running tests
		  // typically using H2 in memory database
		  //config.loadTestProperties();

		  // set as default and register so that Model can be
		  // used if desired for save() and update() etc
		  config.setDefaultServer(true);
		  config.setRegister(true);

		  return EbeanServerFactory.create(config);
		}

}
