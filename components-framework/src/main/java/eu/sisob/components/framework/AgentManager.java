package eu.sisob.components.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.connection.factory.ConnectionClient;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.util.ClassLoaderUtility;
import eu.sisob.components.framework.util.Command;
import eu.sisob.components.framework.util.ConnectionType;
import eu.sisob.components.framework.util.DataType;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 */
public abstract class AgentManager implements ComponentInterface, Runnable {

    /* Defines the agent life status */
    private boolean isAlive;

    private String managerName;

    private List<JsonObject> selfDescriptionMessages;

    private ArrayList<Agent> agents = new ArrayList<Agent>();

    private MessageConnection messageConnection;

    private DataConnection dataConnection;

    private ConnectionType connectionType;

    private JsonObject coordinationMessageTemplate;

    protected static Logger logger = Logger.getLogger(AgentManager.class.getName());

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

    public AgentManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
        this.coordinationMessageTemplate = coordinationMessageTemplate;
        this.managerName = managerName;
        this.connectionType = connectionType;
    }

    protected abstract void createAgent(JsonObject coordinationMessage);
    
    protected void createAgent(Agent agent) {
		// add it to the agents list
		getAgents().add(agent);
		// tell the agent to which manager it belongs
		agent.setAgentListener(this);
		// initialize agent
		agent.initializeAgent();

		// start the agent
		Thread runTime = new Thread(agent);
		runTime.start();
    }

    /**
     * This method initializes the manager. First it setups the connection (create a new one or get the already existing).
     * If connected, register for notifications regarding new command messages. Tell the world the manager is alive
     * and publish a description of the manager.
     */
    public void initialize() {

        // if not agentbundle -> setup own connection
        if (!connectionType.equals(ConnectionType.AGENTBUNDLE)) {
            messageConnection = ConnectionClient.getInstance().getMessageConnection();
            messageConnection.setupConnection();
            if (messageConnection.isConnected())
                messageConnection.register(eu.sisob.components.framework.util.Command.WRITE, coordinationMessageTemplate, this);
        } else {
            ConnectionClient.getInstance().getGlobalMessageConnection().register(eu.sisob.components.framework.util.Command.WRITE, coordinationMessageTemplate, this);
        }

        setAlive(true);
        createAndSendDescription();
    }

    protected JSONObject readFormJSON(String filename) {
        
        String jsonString = this.readFile(filename);
        JSONParser parser = new JSONParser();
        JSONObject formObj = null;
        try {
            formObj = (JSONObject) parser.parse(jsonString);
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, "Invalid JSON declaration for " + filename + "!");
        }
        
        return formObj;
    }
    protected String readFile(String filename) {
        
        InputStream inStream = ClassLoaderUtility.getClassLoader().getResourceAsStream(filename);
        String content = "";
        String line;
        String subscript = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
            line = reader.readLine();

            while (line != null) {
                
                content = content + line;
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {

            logger.log(Level.SEVERE, "File " + filename + " not found!");
        } catch (IOException e) {

            logger.log(Level.SEVERE, "Cannot read " + filename + "! " + e.toString());
        }

        return content;
        
    }
    
    public void notifyMessage(Command command, JsonObject message) {
        if (command.equals(Command.WRITE))
            createAgent(message);
        else if (command.equals(Command.DELETE) && this.selfDescriptionMessages.contains(message))
            shutdown();
    }

    public void notifyManager(Agent agent) {

        switch (agent.getAgentStatus()) {

            case RUNNING: // agent running 
                updateAgentState(agent);
                break;
            case DONE: //agent done
                agent.setAlive(false);
                this.agents.remove(agent);
                updateAgentState(agent);
                break;
            case FINISH: // agent must be finished
                agent.setAlive(false);
                this.agents.remove(agent);
                break;
            case ERROR: //agent has an error
                agent.setAlive(false);
                updateAgentState(agent);
                break;
            default:
        }
    }

    public void updateAgentState(Agent agent) {
        JsonObject coordinationMessage = agent.getCoordinationMessage();

        if (coordinationMessage.has("agentstate")) {
            coordinationMessage.remove("agentstate");
            coordinationMessage.addProperty("agentstate", agent.getAgentStatus().getId());
        } else {
            coordinationMessage.addProperty("agentstate", agent.getAgentStatus().getId());
        }
        if (!connectionType.equals(ConnectionType.AGENTBUNDLE)) {
            messageConnection.update(DataType.COORDINATION, coordinationMessage);
        } else {
            ConnectionClient.getInstance().getGlobalMessageConnection().update(DataType.COORDINATION, coordinationMessage);
        }
    }


    public void run() {
        logger.log(Level.INFO, this.managerName + " Online!!!");

        while (getAlive() == true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.log(Level.WARNING, this.managerName + " Error!", e);
            }
        }
    }

    protected void createAndSendDescription() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                List<Filter> filters = AgentManager.this.getFilterDescriptions();

                AgentManager.this.selfDescriptionMessages = new ArrayList<JsonObject>();

                try {

                    for (Filter filter : filters) {
                        JsonObject filterJson = new JsonObject();
                        filterJson.addProperty("AgentDescription", filter.toJSONString());

                        if (!connectionType.equals(ConnectionType.AGENTBUNDLE)) {
                            // register for delete
                            messageConnection.register(eu.sisob.components.framework.util.Command.DELETE, filterJson, AgentManager.this);

                            // publish new agent description
                            messageConnection.writeDescription(filterJson);
                        } else {
                            ConnectionClient.getInstance().getGlobalMessageConnection().register(eu.sisob.components.framework.util.Command.DELETE, filterJson, AgentManager.this);
                            ConnectionClient.getInstance().getGlobalMessageConnection().writeDescription(filterJson);
                        }
                        AgentManager.this.selfDescriptionMessages.add(filterJson);
                    }

                    while (true && AgentManager.this.getAlive()) {


                        // every 50 seconds update agent description
                        for (JsonObject agentDescription : AgentManager.this.selfDescriptionMessages) {
                            if (!connectionType.equals(ConnectionType.AGENTBUNDLE)) {
                                messageConnection.update(DataType.SELFDESCRIPTION, agentDescription);
                            } else {
                                ConnectionClient.getInstance().getGlobalMessageConnection().update(DataType.SELFDESCRIPTION, agentDescription);
                            }
                        }
                        Thread.sleep(50000);
                    }

                } catch (InterruptedException ex) {
                    logger.log(Level.WARNING, "Interruption during self description update.", ex);
                }

            }

        }).start();
    }

    private void shutdown() {
        logger.info("I am shutting down " + getClass().getName() + " :-(");

        for (Agent a : agents)
            a.finish();

        if (!connectionType.equals(ConnectionType.AGENTBUNDLE)) {
            messageConnection.unregister(coordinationMessageTemplate);
        } else {
            ConnectionClient.getInstance().getGlobalMessageConnection().unregister(coordinationMessageTemplate);
        }

        if (connectionType.equals(ConnectionType.MANAGER) || connectionType.equals(ConnectionType.SINGLE))
            messageConnection.shutdown();

        this.setAlive(false);
        logger.log(Level.INFO, getClass().getName() + " shut down!");
    }

    // getter and setter

    protected abstract List<Filter> getFilterDescriptions();

    public boolean getAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        this.isAlive = alive;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public MessageConnection getMessageConnection() {
        return messageConnection;
    }

    public DataConnection getDataConnection() {
        return dataConnection;
    }

    public List<JsonObject> getSelfDescriptionMessages() {
        return this.selfDescriptionMessages;
    }

    public ArrayList<Agent> getAgents() {

        return agents;
    }

    public void setAgents(ArrayList<Agent> agents) {
        this.agents = agents;
    }
}
