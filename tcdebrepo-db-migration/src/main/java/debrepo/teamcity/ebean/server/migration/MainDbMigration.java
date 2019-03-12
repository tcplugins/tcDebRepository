/*******************************************************************************
 * Copyright 2017 Net Wolf UK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
	    System.setProperty("ddl.migration.version", "1.1.4");
	    System.setProperty("ddl.migration.name", "Inline the file hashes into the object");

	    // generate a migration using drops from a prior version
	    //System.setProperty("ddl.migration.pendingDropsFor", "1.1.0");
	    
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