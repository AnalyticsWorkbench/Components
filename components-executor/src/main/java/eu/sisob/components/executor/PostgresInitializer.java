package eu.sisob.components.executor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import eu.sisob.components.framework.SISOBProperties;

public class PostgresInitializer {

    private boolean databackend;
    private boolean messagebackend;

    public PostgresInitializer() throws ClassNotFoundException {
    	
    	this.databackend = SISOBProperties.getDataBackend().equalsIgnoreCase("postgresql");
    	this.messagebackend = SISOBProperties.getMessageBackend().equalsIgnoreCase("postgresql");
    	
    	if (!this.databackend && !this.messagebackend) {
    		System.out.println("PostgreSQL is neither used as data backend nor as message backend - exiting!");
    	}
    	
        Class.forName("org.postgresql.Driver");

    }

    public static void main(String[] args) {
    	try {
	        PostgresInitializer postgresInitializer = new PostgresInitializer();
	        postgresInitializer.initialize();
    	} catch (ClassNotFoundException ex) {
    		System.out.println("Could not find driver for PostgreSQL");
    	}

    }

    public Connection getDataConnection() throws SQLException {
    	String host = SISOBProperties.getDataServerName();
    	int port = SISOBProperties.getDataPort();
    	String database = SISOBProperties.getDataBackendInternalName();
    	String username = SISOBProperties.getDataBackendUsername();
    	String password = SISOBProperties.getDataBackendPassword();
        String connectionUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        return DriverManager.getConnection(connectionUrl, username, password);
    }
    
    public Connection getMessageConnection() throws SQLException {
    	String host = SISOBProperties.getServerName();
    	int port = SISOBProperties.getServerPort();
    	String database = SISOBProperties.getMessageBackendInternalName();
    	String username = SISOBProperties.getMessageBackendUsername();
    	String password = SISOBProperties.getMessageBackendPassword();
        String connectionUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        return DriverManager.getConnection(connectionUrl, username, password);
    }

    public void initialize() {

        try {

            if (messagebackend) {
            	Connection messageConnection = getMessageConnection();
            	Statement messageStatement = messageConnection.createStatement();
            	ResultSet messageResultSet;

                System.out.println("Lean back while i'm setting up your database :-)");

                // check for command

                messageResultSet = messageStatement.executeQuery("SELECT relname FROM pg_class WHERE relname = 'coordinationmessage';");

                if (!messageResultSet.isBeforeFirst()) {
                    System.out.println("Coordinationmessage table does not exist, I'm going to create it!");
                    messageStatement.execute("CREATE TABLE coordinationmessage (id serial primary key, coordinationjson json)");
                }

                messageResultSet = messageStatement.executeQuery("SELECT relname FROM pg_class WHERE relname = 'datamessage';");

                if (!messageResultSet.isBeforeFirst()) {
                    System.out.println("Datamessage table does not exist, I'm going to create it!");
                    messageStatement.execute("CREATE TABLE datamessage (id serial primary key, datajson json)");
                }

                messageResultSet = messageStatement.executeQuery("SELECT relname FROM pg_class WHERE relname = 'agentdescription';");

                if (!messageResultSet.isBeforeFirst()) {
                    System.out.println("Agentdescription table does not exist, I'm going to create it!");
                    messageStatement.execute("CREATE TABLE agentdescription (id serial primary key, agentjson json, timestamp TIMESTAMP)");
                }

                messageResultSet = messageStatement.executeQuery("SELECT relname FROM pg_class WHERE relname = 'errormessage';");

                if (!messageResultSet.isBeforeFirst()) {
                    System.out.println("Errormessage table does not exist, I'm going to create it!");
                    messageStatement.execute("CREATE TABLE errormessage (id serial primary key, errorjson json)");
                }

                System.out.println("Creating trigger");
                String triggerStatement;
                System.out.println("Trigger coordination_new");
                triggerStatement = "DROP TRIGGER IF EXISTS coordination_new ON coordinationmessage";
                messageStatement.execute(triggerStatement);
                triggerStatement="CREATE OR REPLACE FUNCTION coordination_new() RETURNS TRIGGER AS $$ " +
                                "BEGIN " +
                                "PERFORM pg_notify('coordination_new', row_to_json(NEW)::text); " +
                                "return NEW; " +
                                "END; " +
                                "$$ LANGUAGE plpgsql;";
                messageStatement.execute(triggerStatement);
                triggerStatement = "CREATE TRIGGER coordination_new BEFORE INSERT ON coordinationmessage FOR EACH ROW EXECUTE PROCEDURE coordination_new();";
                messageStatement.execute(triggerStatement);

                System.out.println("Trigger coordination_update");

                triggerStatement = "DROP TRIGGER IF EXISTS coordination_update ON coordinationmessage";
                messageStatement.execute(triggerStatement);
                triggerStatement="CREATE OR REPLACE FUNCTION coordination_update() RETURNS TRIGGER AS $$ " +
                        "BEGIN " +
                        "PERFORM pg_notify('coordination_update', row_to_json(NEW)::text); " +
                        "return NEW; " +
                        "END; " +
                        "$$ LANGUAGE plpgsql;";
                messageStatement.execute(triggerStatement);
                triggerStatement = "CREATE TRIGGER coordination_update BEFORE UPDATE ON coordinationmessage FOR EACH ROW EXECUTE PROCEDURE coordination_update();";
                messageStatement.execute(triggerStatement);

                System.out.println("Trigger data");

                triggerStatement = "DROP TRIGGER IF EXISTS data ON datamessage";
                messageStatement.execute(triggerStatement);
                triggerStatement="CREATE OR REPLACE FUNCTION data() RETURNS TRIGGER AS $$ " +
                        "DECLARE    payload    text;" +
                        "BEGIN " +
                        "payload := row_to_json(NEW)::text;" +
                        "IF octet_length( payload ) > 8000 THEN " +
                        "payload := ('{\"dataid\": \"' || NEW.id || '\"}')::json::text; " +
                        "PERFORM pg_notify('data', payload); " +
                        "ELSE "+
                        "PERFORM pg_notify('data', payload); " +
                        "END IF; " +
                        "return NEW; " +
                        "END; " +
                        "$$ LANGUAGE plpgsql;";
                messageStatement.execute(triggerStatement);
                triggerStatement = "CREATE TRIGGER data BEFORE INSERT ON datamessage FOR EACH ROW EXECUTE PROCEDURE data();";
                messageStatement.execute(triggerStatement);
                
                messageStatement.close();
                messageConnection.close();
            }

            if (databackend) {
            	
            	Connection dataConnection = getDataConnection();
            	Statement dataStatement = dataConnection.createStatement();
            	ResultSet dataResultSet;

                System.out.println("All message tables complete. Now the data table...");

                dataResultSet = dataStatement.executeQuery("SELECT relname FROM pg_class WHERE relname = 'data';");

                if (!dataResultSet.isBeforeFirst()) {
                    System.out.println("Data table does not exist, I'm going to create it!");
                    dataStatement.execute("CREATE TABLE data (id VARCHAR(200) primary key, data bytea)");
                }

                System.out.println("All done. You're good to go :)");
                
                dataStatement.close();
                dataConnection.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isDatabackend() {
        return databackend;
    }

    public void setDatabackend(boolean databackend) {
        this.databackend = databackend;
    }

    public boolean isMessagebackend() {
        return messagebackend;
    }

    public void setMessagebackend(boolean messagebackend) {
        this.messagebackend = messagebackend;
    }

}
