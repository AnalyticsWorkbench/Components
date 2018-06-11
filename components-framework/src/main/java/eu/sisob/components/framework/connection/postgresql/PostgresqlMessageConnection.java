package eu.sisob.components.framework.connection.postgresql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.NotificationReceiver;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.connection.factory.ConnectionClient;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.util.Command;
import eu.sisob.components.framework.util.ConnectionType;
import eu.sisob.components.framework.util.DataType;

/**
 * @author Stefan Remberg
 */
public class PostgresqlMessageConnection implements MessageConnection {

    private static final String DATA = "data";

    private HashMap<JsonObject, Agent> agentList;
    private HashMap<JsonObject, AgentManager> managerList;
    private HashMap<JsonObject, NotificationReceiver> receiverList;
    private String url;
    private int port;
    private String database;
    private boolean isConnected;
    private Notifier notifier;
    private boolean isGlobal;

    protected static Logger logger = Logger.getLogger(PostgresqlMessageConnection.class.getName());

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


    public PostgresqlMessageConnection(String url, int port, String database) {
        this.url = url;
        this.port = port;
        this.database = database;
        this.managerList = new HashMap<>();
        this.agentList = new HashMap<>();
        this.receiverList = new HashMap<>();
        this.isGlobal = false;
    }

    @Override
    public boolean setupConnection() {
        this.isConnected = false;

        try {
            Class.forName("org.postgresql.Driver");
            isConnected = true;
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            isConnected = false;
        }

        if (SISOBProperties.getConnectionType().equals(ConnectionType.AGENTBUNDLE.name()) && isGlobal) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();

            Connection connection;

            try {
                connection = getConnection();
                notifier = new Notifier(connection, DATA, this);
                executorService.execute(notifier);

            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (SISOBProperties.getConnectionType().equals(ConnectionType.SINGLE.name()) || SISOBProperties.getConnectionType().equals(ConnectionType.MANAGER.name())) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();

            Connection connection;

            try {
                connection = getConnection();
                notifier = new Notifier(connection, DATA, this);
                executorService.execute(notifier);

            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }


        return isConnected;
    }

    /**
     * For each register create a new thread and add it to the map eventmask -> thread
     *
     * @param command
     * @param event
     * @param agent
     */
    @Override
    public void register(Command command, JsonObject event, Agent agent) {
        agentList.put(event, agent);
        if (SISOBProperties.getConnectionType().equals(ConnectionType.AGENTBUNDLE.name()) && !isGlobal) {
            ConnectionClient.getInstance().getGlobalMessageConnection().register(command, event, agent);
        }
    }

    @Override
    public void register(Command command, JsonObject event, AgentManager manager) {
        managerList.put(event, manager);
        if (SISOBProperties.getConnectionType().equals(ConnectionType.AGENTBUNDLE.name()) && !isGlobal) {
            ConnectionClient.getInstance().getGlobalMessageConnection().register(command, event, manager);
        }
    }
    
    public void register(Command command, JsonObject event, NotificationReceiver receiver) {
    	receiverList.put(event, receiver);
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void callManager(Command command, JsonObject callback) {

        // this is an agent description
        if (command.equals(Command.DELETE)) {
            for (Map.Entry<JsonObject, AgentManager> entry : managerList.entrySet()) {

                // find the right manager for the callback

                if (entry.getKey().get("AgentDescription").getAsString().equals(callback.get("AgentDescription").getAsString())) {

                    // call and break

                    entry.getValue().notifyMessage(command, callback);
                    break;
                }
            }
        } else {

            for (Map.Entry<JsonObject, AgentManager> entry : managerList.entrySet()) {
                // find the right manager for the callback
                if (entry.getKey().has("agentid") && (entry.getKey().get("agentid").getAsString().equals(callback.get("agentid").getAsString()))) {
                    // call and break
                    entry.getValue().notifyMessage(command, callback);
                    break;
                }
            }
        }

    }

    @Override
    public void callAgent(Command command, JsonObject callback) {
 
        // in case its a data message which is veeery large because of many files
        if (callback.has("dataid"))
            callback = readLargeDataMessage(callback.get("dataid").getAsString());
        
        System.out.println("Received message " + callback + " over connection: " + this + "\n" +
        "Managers: " + this.managerList.values().toString() + "\n" +
        "Agents: " + this.agentList.values().toString());
        if (this.agentList.values().size() > 0) {
            
            System.out.println("Hello World! " +  this.agentList.toString());
        }
        for (Map.Entry<JsonObject, Agent> entry : agentList.entrySet()) {
            System.out.println("Hello World! Notify agents.");
            if (entry.getKey().get("pipeid").getAsString().equals(callback.get("pipeid").getAsString()) && (entry.getKey().get("runid").getAsString().equals(callback.get("runid").getAsString()))) {
                entry.getValue().notifyMessage(command, callback);
                break;
            }
        }

    }
    
    public void callReceiver(Command command, JsonObject callback) {
    	for (Map.Entry<JsonObject, NotificationReceiver> entry : receiverList.entrySet()) {
    		if (callback.has("runjson") && "run".equalsIgnoreCase(entry.getKey().get("type").getAsString()) && callback.get("runjson").getAsJsonObject().get("runid").getAsString().equalsIgnoreCase(entry.getKey().get("runid").getAsString())) {
    			entry.getValue().notifyMessage(command, callback);    			
    		} else if (callback.has("coordinationjson") && "coordination".equalsIgnoreCase(entry.getKey().get("type").getAsString()) && callback.get("coordinationjson").getAsJsonObject().get("runid").getAsString().equalsIgnoreCase(entry.getKey().get("runid").getAsString()) && callback.get("coordinationjson").getAsJsonObject().get("instanceid").getAsString().equalsIgnoreCase(entry.getKey().get("instanceid").getAsString())) {
    			entry.getValue().notifyMessage(command, callback);
    		}
    	}
    }

    /**
     * Fix for the postgres limitation of 8000 byte string payload
     * @param id id of database entry
     * @return json object with data message
     */
    private JsonObject readLargeDataMessage(String id) {
        Connection connection = null;
        JsonObject jsonObject = null;

        try {
            connection = getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT datajson FROM DATAMESSAGE WHERE id = ?");
            preparedStatement.setInt(1, Integer.valueOf(id));

            ResultSet resultSet = preparedStatement.executeQuery();


            while (resultSet.next()) {
                jsonObject = new Gson().fromJson(resultSet.getString(1), JsonObject.class);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return jsonObject;

    }

    /**
     * Write agentdescription
     *
     * @param description corresponding agent json
     * @return
     */
    @Override
    public int writeDescription(JsonObject description) {

        Connection connection = null;
        PreparedStatement statement;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            // first remove all existsing

            String agentDescription = description.get("AgentDescription").getAsString();

            JsonObject jsonObject = new Gson().fromJson(agentDescription, JsonObject.class);

            PGobject dataObject = new PGobject();
            dataObject.setType("json");
            dataObject.setValue(agentDescription);

            connection.createStatement().execute("DELETE FROM agentdescription WHERE agentjson ->> 'name' = '" + jsonObject.get("name").getAsString() + "'");

            // add the new one

            statement = connection.prepareStatement("INSERT INTO agentdescription (agentjson, timestamp) VALUES (?,?)");
            statement.setObject(1, dataObject);
            Calendar calendar = Calendar.getInstance();
            statement.setTimestamp(2, new Timestamp(calendar.getTime().getTime()));
            statement.execute();

            connection.commit();

            statement.close();

            connection.close();
        } catch (SQLException e ) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            if (connection != null) {
                try {
                    System.err.print("Transaction is being rolled back");
                    connection.rollback();
                } catch(SQLException excep) {
                }
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);

                }
            }
        }


        return 0;
    }

    public Connection getConnection() throws SQLException {
        String connectionUrl = "jdbc:postgresql://" + url + ":" + port + "/" + database;
        return DriverManager.getConnection(connectionUrl, SISOBProperties.getMessageBackendUsername(), SISOBProperties.getMessageBackendPassword());
    }

    /**
     * Update command or agent description.
     *
     * @param dataType command or agentdescription
     * @param object   object to be persisted
     * @return
     */
    @Override
    public int update(DataType dataType, JsonObject object) {
        if (dataType.equals(DataType.COORDINATION)) {
            try {
                Connection connection = getConnection();

                PreparedStatement statement = connection.prepareStatement("UPDATE COORDINATIONMESSAGE SET COORDINATIONJSON = ? WHERE COORDINATIONJSON->>'runid' = ? AND COORDINATIONJSON->>'instanceid' = ?;");

                PGobject dataObject = new PGobject();
                dataObject.setType("json");
                dataObject.setValue(new Gson().toJson(object));

                statement.setObject(1, dataObject);
                statement.setString(2, object.get("runid").getAsString());
                statement.setString(3, object.get("instanceid").getAsString());

                statement.execute();
//
                statement.close();

                connection.close();

            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }


        } else if (dataType.equals(DataType.SELFDESCRIPTION)) {
            // here we have to do it a little different

            Connection connection;
            PreparedStatement statement;

            try {
                connection = getConnection();

                String agentDescription = object.get("AgentDescription").getAsString();

                JsonObject jsonObject = new Gson().fromJson(agentDescription, JsonObject.class);

                statement = connection.prepareStatement("UPDATE agentdescription SET agentjson = ?, timestamp = ? WHERE agentjson ->> 'name' = '" + jsonObject.get("name").getAsString() + "'");

                PGobject dataObject = new PGobject();
                dataObject.setType("json");
                dataObject.setValue(new Gson().toJson(jsonObject));

                statement.setObject(1, dataObject);
                Calendar calendar = Calendar.getInstance();
                statement.setTimestamp(2, new Timestamp(calendar.getTime().getTime()));

                statement.execute();
                statement.close();
                connection.close();

            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }

        } else if (dataType.equals(DataType.RUN)) {
            // here we have to do it a little different
            try {
            	Connection connection = getConnection();

                PreparedStatement statement = connection.prepareStatement("UPDATE RUNMESSAGE SET RUNJSON = ? WHERE RUNJSON->>'runid' = ?;");

                PGobject dataObject = new PGobject();
                dataObject.setType("json");
                dataObject.setValue(new Gson().toJson(object));
                statement.setObject(1, dataObject);
                statement.setString(2, object.get("runid").getAsString());
                statement.execute();
                statement.close();
                connection.close();

            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }

        }
        return 0;
    }

    @Override
    public int write(DataType type, JsonObject object) {
        Connection connection;
        PreparedStatement statement;
        
        String statementString = null;

        if (type.equals(DataType.DATA)) {
        	statementString = "INSERT INTO datamessage (datajson) VALUES (?)";
        } else if (type.equals(DataType.ERROR)) {
        	statementString = "INSERT INTO errormessage (errorjson) VALUES (?)";
    	} else if (type.equals(DataType.RESULT)) {
    		statementString = "INSERT INTO result (result) VALUES (?)";
    	} else if (type.equals(DataType.COORDINATION)) {
    		statementString = "INSERT INTO coordinationmessage (coordinationjson) VALUES (?)";
    	} else if (type.equals(DataType.RUN)) {
    		statementString = "INSERT INTO runmessage (runjson) VALUES (?)";
        } else {
        	throw new IllegalArgumentException("Unsupported DataType " + type);
        }
        
        if (statementString != null) {
	        try {
	            connection = getConnection();
	            statement = connection.prepareStatement(statementString);
	            PGobject dataObject = new PGobject();
	            dataObject.setType("json");
	            dataObject.setValue(new Gson().toJson(object));
	
	            statement.setObject(1, dataObject);
	            statement.execute();
	            statement.close();
	            connection.close();
	
	        } catch (SQLException e) {
	            logger.log(Level.SEVERE, e.getMessage());
	        }
        }

        return 0;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void unregister(JsonObject event) {

        if (agentList.containsKey(event))
            agentList.remove(event);
        else if (managerList.containsKey(event))
            managerList.remove(event);

        if (agentList.isEmpty() && managerList.isEmpty()) {
            try {
                notifier.kill();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }

        if (SISOBProperties.getConnectionType().equals(ConnectionType.AGENTBUNDLE.name()) && !isGlobal) {
            ConnectionClient.getInstance().getGlobalMessageConnection().unregister(event);
        }
    }

    @Override
    public boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public void setIsGlobal(boolean global) {
        this.isGlobal = global;
    }
}
