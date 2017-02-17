package debrepo.teamcity.ebean.server.migration;

import java.io.IOException;

import com.avaje.ebean.config.Platform;
import com.avaje.ebean.dbmigration.DbMigration;

/**
 * Generate the DB Migration.
 */
public class MainDbMigration {

  /**
   * Generate the next "DB schema DIFF" migration.
   * <p>
   * These migration are typically run using FlywayDB, Liquibase
   * or Ebean's own built in migration runner.
   * </p>
   */
  public static void main(String[] args) throws IOException {

	  MainDbMigration migration = new MainDbMigration();
	  migration.generateMigrationFiles();
  }
  
  public void generateMigrationFiles() throws IOException {
	    // optionally specify the version and name
	    System.setProperty("ddl.migration.version", "1.1.0");
	    System.setProperty("ddl.migration.name", "Refactor DebPackageModel to split out DebFileModel and add some Indexes");

	    // generate a migration using drops from a prior version
	    //System.setProperty("ddl.migration.pendingDropsFor", "1.0");
	    
	    System.setProperty("disableTestProperties", "true");
	    
	    DbMigration migration = new DbMigration();
	    migration.setPathToResources("src/main/resources");
	    
	    //migration.addPlatform(Platform.POSTGRES, "pg");
	    migration.addPlatform(Platform.H2, "h2");
	    migration.addPlatform(Platform.MYSQL, "mysql");
	    migration.addPlatform(Platform.SQLSERVER, "sqlserver");
	    migration.addPlatform(Platform.ORACLE, "oracle");
	    
	    migration.generateMigration();

  }
}