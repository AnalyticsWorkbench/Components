package eu.sisob.components.framework.connection.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.connection.factory.ConnectionClient;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.util.Command;
import eu.sisob.components.framework.util.ConnectionType;
import eu.sisob.components.framework.util.DataType;

public class MqttMessageConnection implements MessageConnection {

    private final String url;
    private final int port;
    private final ConnectionType connectionType;
    private String mqttConnectionUrl;
    private MqttClient subscriptionclient;
    private String clientId;
    private boolean isConnected;
    private Map<JsonObject, AgentManager> managerList;
    private Map<JsonObject, Agent> agentList;
    private boolean isGlobal;

    private static final String AGENTDESCRIPTION_NEW = "AGENTDESCRIPTION/NEW";
    private static final String AGENTDESCRIPTION_DELETE = "AGENTDESCRIPTION/DELETE";
    private static final String DATATOPIC = "DATA";
    private static final String ERRORTOPIC = "ERROR";
    private static final String COORDINATION_WRITE = "COORDINATION/NEW";
    private static final String COORDINATION_UPDATE = "COORDINATION/UPDATE";
    private static final String RESULT = "RESULT";

    protected static Logger logger = Logger.getLogger(MqttMessageConnection.class.getName());
    private MqttConnectOptions mqttConnectOptions;

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

    public MqttMessageConnection(String url, int port) {
        this.url = url;
        this.port = port;
        this.managerList = new HashMap<JsonObject, AgentManager>();
        this.agentList = new HashMap<JsonObject, Agent>();
        this.isGlobal = false;
        this.connectionType = ConnectionType.valueOf(SISOBProperties.getConnectionType());
    }

    /**
     * Setup mqtt connection.
     *
     * @return true or false if connected or not
     */
    @Override
    public boolean setupConnection() {

        isConnected = false;

        mqttConnectionUrl = "tcp://" + url + ":" + port;
        clientId = generateClientId();

        try {
            subscriptionclient = new MqttClient(mqttConnectionUrl, clientId, null);

            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);

            subscriptionclient.connect(mqttConnectOptions);

            if (connectionType.equals(ConnectionType.AGENTBUNDLE) && isGlobal) {
                String[] topics = new String[]{COORDINATION_WRITE
                        , AGENTDESCRIPTION_DELETE, DATATOPIC};
                subscriptionclient.subscribe(topics);
            } else if (connectionType.equals(ConnectionType.MANAGER)) {
                String[] topics = new String[]{COORDINATION_WRITE
                        , AGENTDESCRIPTION_DELETE, DATATOPIC};
                subscriptionclient.subscribe(topics);
            }

            subscriptionclient.setCallback(new MqttMessageCallback(this));

            isConnected = true;
        } catch (MqttException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            isConnected = false;
        }

        return isConnected;
    }

    /**
     * Generate uuid for mqtt client
     *
     * @return uuid for client
     */
    private String generateClientId() {
        return MqttClient.generateClientId();
    }

    private MqttClient generateNewClient() throws MqttException {
        return new MqttClient(mqttConnectionUrl, generateClientId(), null);
    }

    @Override
    public void register(Command command, JsonObject event, Agent agent) {
        if (connectionType.equals(ConnectionType.SINGLE)) {
            try {
                if (command.equals(Command.WRITE)) {
                    subscriptionclient.subscribe(DATATOPIC);
                }
            } catch (MqttException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (connectionType.equals(ConnectionType.AGENTBUNDLE) && !isGlobal) {
            ConnectionClient.getInstance().getGlobalMessageConnection().register(command, event, agent);
        }
        agentList.put(event, agent);
    }

    /**
     * Register for topics
     *
     * @param command command, should be delete for agentdescription and write for command information
     * @param event   event mask
     * @param manager corresponding agent manager
     */
    @Override
    public void register(Command command, JsonObject event, AgentManager manager) {
        if (connectionType.equals(ConnectionType.SINGLE)) {
            try {
                if (command.equals(Command.WRITE))
                    subscriptionclient.subscribe(COORDINATION_WRITE);

                else if (command.equals(Command.DELETE))
                    subscriptionclient.subscribe(AGENTDESCRIPTION_DELETE, 2);

            } catch (MqttException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (connectionType.equals(ConnectionType.AGENTBUNDLE) && !isGlobal) {
            ConnectionClient.getInstance().getGlobalMessageConnection().register(command, event, manager);
        }
        managerList.put(event, manager);
    }

    /**
     * Check if mqtt client is connected
     *
     * @return true for connected else false
     */
    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void callManager(Command command, JsonObject callback) {

        // this is an agent description
        if (command.equals(Command.DELETE)) {

            if (callback.has("broadcast")) {

                // send all agent descriptions
                for (Map.Entry<JsonObject, AgentManager> entry : managerList.entrySet()) {

                    if (entry.getKey().has("AgentDescription"))
                        writeDescription(entry.getKey());
                }

            } else {

                for (Map.Entry<JsonObject, AgentManager> entry : managerList.entrySet()) {

                    // find the right manager for the callback
                    if (entry.getKey().has("AgentDescription")) {
                        if (entry.getKey().get("AgentDescription").getAsString().equals(callback.get("AgentDescription").getAsString())) {

                            // call and break
                            entry.getValue().notifyMessage(command, callback);
                            break;
                        }
                    }
                }
            }
        } else {
            for (Map.Entry<JsonObject, AgentManager> entry : managerList.entrySet()) {

                // find the right manager for the callback

                if (entry.getKey().has("agentid")) {

                    if (entry.getKey().get("agentid").getAsString().equals(callback.get("agentid").getAsString())) {

                        // call and break

                        entry.getValue().notifyMessage(command, callback);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void callAgent(Command command, JsonObject callback) {
        for (Map.Entry<JsonObject, Agent> entry : agentList.entrySet()) {

            if (entry.getKey().get("pipeid").getAsString().equals(callback.get("pipeid").getAsString()) && (entry.getKey().get("runid").getAsString().equals(callback.get("runid").getAsString()))) {
                // call the agent
                entry.getValue().notifyMessage(command.WRITE, callback);
                break;
            }
        }
    }

    /**
     * Publish agent description to topic /agentdescription/new
     *
     * @param description
     * @return
     */
    @Override
    public synchronized int writeDescription(JsonObject description) {
        try {
            MqttClient messageClient = generateNewClient();
            messageClient.connect(mqttConnectOptions);

            MqttTopic agentTopic = messageClient.getTopic(AGENTDESCRIPTION_NEW);

            byte[] descriptionByte = convertToByte(description);

            agentTopic.publish(new MqttMessage(descriptionByte));

            messageClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Convert a given JSON object to a byte array
     *
     * @param jsonObject the JSON object to be converted
     * @return transformed byte array
     */
    private byte[] convertToByte(JsonObject jsonObject) {
        Gson gson = new Gson();

        String json = gson.toJson(jsonObject);

        return json.getBytes();
    }

    @Override
    public synchronized int update(DataType dataType, JsonObject object) {
        if (dataType.equals(DataType.COORDINATION)) {
            try {
                MqttClient messageClient = generateNewClient();
                messageClient.connect(mqttConnectOptions);
                MqttTopic commandTopic = messageClient.getTopic(COORDINATION_UPDATE);
                byte[] commandByte = convertToByte(object);


                MqttMessage message = new MqttMessage();
                message.setPayload(commandByte);
                message.setQos(2);
                commandTopic.publish(message);
                messageClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (dataType.equals(DataType.SELFDESCRIPTION)) {
            writeDescription(object);
        }

        return 0;
    }

    /**
     * Publish data to the mqtt broker.
     *
     * @param type   if we have data objects or error obejcts
     * @param object object to be published
     * @return
     */
    @Override
    public int write(DataType type, JsonObject object) {
        if (type.equals(DataType.DATA)) {
            try {
                synchronized (this) {
                    MqttClient messageClient = generateNewClient();
                    messageClient.connect(mqttConnectOptions);
                    MqttTopic dataTopic = messageClient.getTopic(DATATOPIC);
                    dataTopic.publish(new MqttMessage(convertToByte(object)));
                    messageClient.disconnect();
                }
            } catch (MqttException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (type.equals(DataType.ERROR)) {
            try {
                synchronized (this) {
                    MqttClient messageClient = generateNewClient();
                    messageClient.connect(mqttConnectOptions);
                    MqttTopic errorTopic = messageClient.getTopic(ERRORTOPIC);
                    errorTopic.publish(new MqttMessage(convertToByte(object)));
                    messageClient.disconnect();
                }
            } catch (MqttException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (type.equals(DataType.SELFDESCRIPTION)) {
            try {
                synchronized (this) {
                    MqttClient messageClient = generateNewClient();
                    messageClient.connect(mqttConnectOptions);
                    MqttTopic errorTopic = messageClient.getTopic(AGENTDESCRIPTION_DELETE);
                    errorTopic.publish(new MqttMessage(convertToByte(object)));
                    messageClient.disconnect();
                }
            } catch (MqttException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        } else if (type.equals(DataType.RESULT)) {
            try {
                synchronized (this) {
                    MqttClient messageClient = generateNewClient();
                    messageClient.connect(mqttConnectOptions);
                    MqttTopic resultTopic = messageClient.getTopic(RESULT);
                    resultTopic.publish(new MqttMessage(convertToByte(object)));
                    messageClient.disconnect();
                }
            } catch (MqttException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        return 0;
    }

    /**
     * shutdown mqtt connection
     */
    @Override
    public void shutdown() {
        try {
            subscriptionclient.disconnect();
        } catch (MqttException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        subscriptionclient = null;
    }

    /**
     * Unregister agent or manager
     *
     * @param event event mask
     */
    @Override
    public void unregister(JsonObject event) {
        if (agentList.containsKey(event)) {
            agentList.remove(event);
        } else if (managerList.containsKey(event))
            managerList.remove(event);
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public void setIsGlobal(boolean global) {
        this.isGlobal = global;
    }

    public void reconnect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        try {
            subscriptionclient.connect(options);
        } catch (MqttException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
