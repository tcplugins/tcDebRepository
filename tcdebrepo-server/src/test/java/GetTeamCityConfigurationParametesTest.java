import org.junit.Test;

import debrepo.teamcity.Loggers;

public class GetTeamCityConfigurationParametesTest {
	
	@Test
	public void TestThatWeCanAccessTCConfig() {
		
		Loggers.SERVER.info("#################################################################################");
		Loggers.SERVER.info("#");
		Loggers.SERVER.info("teamcity.build.triggeredBy is: " + System.getProperty("teamcity.build.triggeredBy"));
		Loggers.SERVER.info("teamcity.build.triggeredBy is: " + System.getProperty("%teamcity.build.triggeredBy%"));
		Loggers.SERVER.info("#");
		Loggers.SERVER.info("#################################################################################");
	}

}
