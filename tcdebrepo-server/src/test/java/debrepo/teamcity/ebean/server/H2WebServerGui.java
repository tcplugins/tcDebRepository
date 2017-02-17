package debrepo.teamcity.ebean.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.Server;

public class H2WebServerGui {
	
	// 					"jdbc:h2:file:" + myDataDir.getAbsolutePath() + File.separator + "tcDebRepositoryDB;DB_CLOSE_ON_EXIT=FALSE");

	
	private static final String H2_DB_URL = "jdbc:h2:file:./target/tcDebRepository/database/tcDebRepositoryDB";

	public static void main(String[] args) throws SQLException {
		Server server = null;
        try {
            server = Server.createWebServer().start();
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
