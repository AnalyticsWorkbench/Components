package eu.sisob.components.rwrapper;
// from package eu.sisob.components.framework.connection.postgresql;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.Command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RScriptNotifier implements Runnable {

	private Connection conn;
	private RScriptConnection postgresqlRScriptConnection;
	private org.postgresql.PGConnection pgconn;
	private boolean running;
	// skip notifications caused by rwrapper changing a script in the database
	// based on a notification
	// to avoid infinite loops
	private int skip;

	private static final String ADD_RSCRIPT = "addrscript";
	private static final String DELETE_RSCRIPT = "deleterscript";

	public RScriptNotifier(Connection conn, RScriptConnection postgresqlRScriptConnection) throws SQLException {
		this.conn = conn;
		this.postgresqlRScriptConnection = postgresqlRScriptConnection;
		this.pgconn = (org.postgresql.PGConnection) conn;
		running = true;
		try (Statement stmt = conn.createStatement()) {
			stmt.execute("LISTEN " + ADD_RSCRIPT);
			stmt.execute("LISTEN " + DELETE_RSCRIPT);
		}

		skip = 0;
	}

	@Override
	public void run() {
		while (running) {
			try {
				// issue a dummy query to contact the backend
				// and receive any pending notifications.
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT 1");
				rs.close();
				stmt.close();

				org.postgresql.PGNotification notifications[] = pgconn.getNotifications();
				if (notifications != null) {
					System.out.println("NOtifified!!!");
					for (int i = 0; i < notifications.length; i++) {
						JsonObject payloadJson = new Gson().fromJson(notifications[i].getParameter(), JsonObject.class);
						if (notifications[i].getName().equalsIgnoreCase(ADD_RSCRIPT)) {
							try (Statement stmtUnlisten = conn.createStatement()) {
								stmtUnlisten.execute("UNLISTEN " + ADD_RSCRIPT);
								stmtUnlisten.execute("UNLISTEN " + DELETE_RSCRIPT);
							}
							postgresqlRScriptConnection.callManager(Command.WRITE, payloadJson);
							try (Statement stmtUnlisten = conn.createStatement()) {
								stmtUnlisten.execute("LISTEN " + ADD_RSCRIPT);
								stmtUnlisten.execute("LISTEN " + DELETE_RSCRIPT);
							}
						} else if (notifications[i].getName().equalsIgnoreCase(DELETE_RSCRIPT)) {
							try (Statement stmtUnlisten = conn.createStatement()) {
								stmtUnlisten.execute("UNLISTEN " + ADD_RSCRIPT);
								stmtUnlisten.execute("UNLISTEN " + DELETE_RSCRIPT);
							}
							postgresqlRScriptConnection.callManager(Command.DELETE, payloadJson);
							try (Statement stmtUnlisten = conn.createStatement()) {
								stmtUnlisten.execute("LISTEN " + ADD_RSCRIPT);
								stmtUnlisten.execute("LISTEN " + DELETE_RSCRIPT);
							}
						}
					}

				}

				// wait a while before checking again for new
				// notifications
				Thread.sleep(2000);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void kill() throws SQLException {
		running = false;
		if (!conn.isClosed())
			conn.close();
	}
}
