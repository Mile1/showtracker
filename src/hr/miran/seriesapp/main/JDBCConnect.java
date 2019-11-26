package hr.miran.seriesapp.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
public class JDBCConnect {
	
	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;
	private static Connection veza;

	public void closeConnectionToDatabase(Connection veza) {
		try {
			veza.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Connection connectToUserDb() {
			
			dbUrl = "jdbc:mysql://45.77.198.97:3306/showtracker?useSSL=false";
			dbUsername = "showuser";
			dbPassword = "Patak12#";
	
		try {
			veza = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return veza;			
	}
}