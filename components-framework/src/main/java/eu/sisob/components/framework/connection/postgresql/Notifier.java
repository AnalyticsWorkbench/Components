package eu.sisob.components.framework.connection.postgresql;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.Command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Notifier implements Runnable {

    private Connection conn;
    private PostgresqlMessageConnection postgresqlMessageConnection;
    private org.postgresql.PGConnection pgconn;
    private boolean running;
    private static final String COORDINATION_NEW = "coordination_new";
    private static final String DESCRIPTION_DELETE = "agentdescription_delete";
    private static final String DATA = "data";
    private static final String COORDINATION_UPDATE = "coordination_update";
    private static final String RUN_UPDATE = "run_update";

    public Notifier(Connection conn, String channel, PostgresqlMessageConnection postgresqlMessageConnection) throws SQLException {
        this.conn = conn;
        this.postgresqlMessageConnection = postgresqlMessageConnection;
        this.pgconn = (org.postgresql.PGConnection) conn;
        running = true;
        Statement stmt = conn.createStatement();
        stmt.execute("LISTEN " + COORDINATION_NEW);
        stmt.execute("LISTEN " + DESCRIPTION_DELETE);
        stmt.execute("LISTEN " + DATA);
        stmt.execute("LISTEN " + COORDINATION_UPDATE);
        stmt.execute("LISTEN " + RUN_UPDATE);
        stmt.close();
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
                    for (int i = 0; i < notifications.length; i++) {
                        if (notifications[i].getName().equalsIgnoreCase(COORDINATION_NEW)) {
                            JsonObject payloadJson = new Gson().fromJson(notifications[i].getParameter(), JsonObject.class);
                            postgresqlMessageConnection.callManager(Command.WRITE, payloadJson.get("coordinationjson").getAsJsonObject());

                        } else if (notifications[i].getName().equalsIgnoreCase(DESCRIPTION_DELETE)) {
                            JsonObject payloadJson = new Gson().fromJson(notifications[i].getParameter(), JsonObject.class);
                            postgresqlMessageConnection.callManager(Command.DELETE, payloadJson);
                        } else if (notifications[i].getName().equalsIgnoreCase(DATA)) {
                            JsonObject payloadJson = new Gson().fromJson(notifications[i].getParameter(), JsonObject.class);

                            if (payloadJson.has("dataid"))
                                postgresqlMessageConnection.callAgent(Command.WRITE, payloadJson);
                            else
                                postgresqlMessageConnection.callAgent(Command.WRITE, payloadJson.get("datajson").getAsJsonObject());
                        } else if (notifications[i].getName().equalsIgnoreCase(COORDINATION_UPDATE) || notifications[i].getName().equalsIgnoreCase(RUN_UPDATE)) {
                        	JsonObject payloadJson = new Gson().fromJson(notifications[i].getParameter(), JsonObject.class);
                        	postgresqlMessageConnection.callReceiver(Command.UPDATE, payloadJson);
                        }
                    }
                }

                // wait a while before checking again for new
                // notifications
                Thread.sleep(300);
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    public void kill() throws SQLException {
        running = false;
        if (!conn.isClosed())
            conn.close();
    }
}
