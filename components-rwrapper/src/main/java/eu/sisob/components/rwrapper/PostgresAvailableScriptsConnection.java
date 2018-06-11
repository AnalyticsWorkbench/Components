package eu.sisob.components.rwrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import eu.sisob.components.framework.SISOBProperties;

public class PostgresAvailableScriptsConnection {

	public PostgresAvailableScriptsConnection() throws ClassNotFoundException, SQLException {
//		Class.forName("org.postgresql.Driver");
//		String tableName = "rscriptlinkgroups";
//		if(!checkTableExists(tableName)){
//			System.out.println(tableName + " table does not exist, I'm going to create it!");
//			createTable(tableName, "(id serial primary key, link json");
//			
//		}
		
	}

	public boolean checkTableExists(String tableName) throws SQLException {
		String query = "SELECT relname FROM pg_class WHERE relname = ?';";
		boolean result;
		try (Connection conn = createMessageConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
			Object[] parameters = { tableName };
			try (ResultSet rs = executeQuery(pst, parameters);) {
				result = rs.isBeforeFirst();
			}

		}

		return result;
	}

	public Connection createMessageConnection() throws SQLException {
		String host = SISOBProperties.getServerName();
		int port = SISOBProperties.getServerPort();
		String database = SISOBProperties.getMessageBackendInternalName();
		String username = SISOBProperties.getMessageBackendUsername();
		String password = SISOBProperties.getMessageBackendPassword();
		String connectionUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
		return DriverManager.getConnection(connectionUrl, username, password);
	}

	public ResultSet executeQuery(PreparedStatement pst, Object[] parameters) throws SQLException {

		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] instanceof String) {
				pst.setString(i + 1, (String) parameters[i]);
			}
			if (parameters[i] instanceof Integer) {
				pst.setInt(i + 1, (Integer) parameters[i]);
			}
		}

		return pst.executeQuery();
	}
	
	public void createTable(String tableName, String fieldsString) throws SQLException{
		String query = "CREATE TABLE " + tableName + " " + fieldsString + ";";
		try(Connection conn = createMessageConnection(); 
				PreparedStatement pst = conn.prepareStatement(query);
				ResultSet rs = executeQuery(pst, new Object[0])){
		}
	}
}
