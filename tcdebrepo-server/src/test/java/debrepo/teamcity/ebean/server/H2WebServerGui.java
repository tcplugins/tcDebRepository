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
package debrepo.teamcity.ebean.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.Server;

public class H2WebServerGui {
	
	private static final String H2_DB_URL = "jdbc:h2:file:./target/tcDebRepository/database/tcDebRepositoryDB";

	public static void main(String[] args) throws SQLException {
        try {
        	Server.createWebServer().start();
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.
                getConnection(H2_DB_URL, "sa", "");
            System.out.println("Connection Established: "
                    + conn.getMetaData().getDatabaseProductName() + "/" + conn.getCatalog());
            System.out.println("Point your browser at: http://localhost:8082/");
            System.out.println(" and set the JDBC URL to: " + H2_DB_URL);
            
            System.out.println();
            System.out.println("You will need to exit this for unit tests to run correctly.");
            System.out.println("Press stop in the IDE or Ctrl + C if in the console.");

        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
