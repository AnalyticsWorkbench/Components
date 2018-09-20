package eu.sisob.components.framework.connection.postgresql;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.connection.interfaces.DataConnection;

/**
 * Data conection for use with postgresql.
 * @author Stefan Remberg
 * Masterarbeit 15
 */
public class PostgresqlDataConnection implements DataConnection {

    private String url;
    private int port;
    private String database;
    private boolean isConnected;

    protected static Logger logger = Logger.getLogger(PostgresqlDataConnection.class.getName());

    {
        boolean found = false;
        for (Handler h : logger.getHandlers()) {
            if (h instanceof ConsoleHandler) {
                found = true;
                h.setLevel(Level.ALL);
            }
        }
        if (!found) {
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(Level.ALL);
            logger.addHandler(ch);
        }
        logger.setLevel(Level.ALL);

    }

    public PostgresqlDataConnection(String url, int port, String database) {
        this.url = url;
        this.port = port;
        this.database = database;
    }

    @Override
    public boolean setupConnection() {

        this.isConnected = false;

        try {
            Class.forName("com.impossibl.postgres.jdbc.PGDriver");
            isConnected = true;
        } catch (ClassNotFoundException e) {
            isConnected = false;
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return isConnected;
    }

    /**
     * Persist data to postgresql database
     * @return fileid
     */
    @Override
    public void writeData(String path, String payload) {
        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(true);

            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO DATA VALUES (?,?)");

            pstmt.setString(1, path);
            InputStream in = new ByteArrayInputStream(payload.getBytes());
            pstmt.setBinaryStream(2, in);

            pstmt.executeUpdate();
            pstmt.close();
            connection.close();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    /**
     * Create a new connection
     * @return sql connection
     * @throws SQLException - error if connection fails
     */
    public Connection getConnection() throws SQLException {
        String connectionUrl = "jdbc:postgresql://" + url + ":" + port + "/" + database;
        return DriverManager.getConnection(connectionUrl, SISOBProperties.getDataBackendUsername(), SISOBProperties.getDataBackendPassword());
    }

    /**
     * Read data from PAYLOAD table of postgresql database with given fileid.
     * @return string containing the data
     */
    @Override
    public String readData(String path) {
        Connection connection;
        ResultSet resultSet;
        String data = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            PreparedStatement pstmt = connection.prepareStatement("SELECT DATA FROM DATA WHERE ID = ?");
            pstmt.setString(1, path);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                data = new String (resultSet.getBytes(1));
            }

            pstmt.close();
            resultSet.close();
            connection.close();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return data;
    }

    @Override
    public void removeData(String path) {
        Connection connection;

        try {
            connection = getConnection();
            connection.setAutoCommit(true);
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM DATA WHERE ID = ?");
            pstmt.setString(1, path);

            pstmt.execute();

            pstmt.close();
            connection.close();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void shutdown() {

    }
}