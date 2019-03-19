package debrepo.teamcity.ebean.server.migration;

import java.io.IOException;

import org.junit.Test;

public class MainDbMigrationTest {

	@Test
	public void testGenerateMigrationFiles() throws IOException {
		MainDbMigration migration= new MainDbMigration();
		migration.generateMigrationFiles();
	}

}
