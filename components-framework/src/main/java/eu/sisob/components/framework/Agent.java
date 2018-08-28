package eu.sisob.components.framework;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.connection.factory.ConnectionClient;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.json.util.JSONFile;
import eu.sisob.components.framework.util.AgentStatus;
import eu.sisob.components.framework.util.ConnectionType;
import eu.sisob.components.framework.util.DataType;

public abstract class Agent implements Runnable {

    //protected Object outputFile; // FBA APRIN add for meta.data output file js meta data

    private String baseUrl;

    protected JsonObject dataStructure;

    protected MessageConnection messageConnection;

    protected DataConnection dataConnection;

    protected List<JsonObject> collectedDataMessages;

    protected JsonObject coordinationMessage;

    protected boolean isAlive;

    protected String workflowID;

    private AgentStatus agentStatus;

    private JsonObject dataRegisterEvent;

    protected String agentInstanceID;

    protected String agentID;

    protected String[] pipes;

    protected AgentManager agentManager;

    protected JSONObject filterParameters;

    protected ReentrantLock lock;

    protected boolean outputAgent;

    private boolean unregister = false;

    protected static Logger logger = Logger.getLogger(Agent.class.getName());

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

    // constructor
    public Agent(JsonObject coordinationMsg) {

        this.coordinationMessage = coordinationMsg;

        this.workflowID = this.coordinationMessage.get("runid").getAsString();

        this.agentStatus = AgentStatus.WAITING;

        this.agentInstanceID = this.coordinationMessage.get("instanceid").getAsString();

        this.agentID = this.coordinationMessage.get("agentid").getAsString();

        this.pipes = this.coordinationMessage.get("pipes").getAsString().split(",");

        this.dataStructure = new JsonObject();

        this.dataStructure.addProperty("runid", this.coordinationMessage.get("runid").getAsString());
        this.dataStructure.addProperty("pipes", this.coordinationMessage.get("pipes").getAsString());

        parseFilterParameters(this.coordinationMessage.get("parameters").getAsString());

        this.lock = new ReentrantLock();
    }

    // initialization
    public void initializeAgent() {
        try {

            JSONParser parser = new JSONParser();

            for (JsonObject selfdescription : agentManager.getSelfDescriptionMessages()) {
                Filter agentjson = Filter
                        .fromJSON((JSONObject) parser.parse(selfdescription.get("AgentDescription").getAsString()));
                if (agentjson.getContainer().getOutputs() == null || agentjson.getContainer().getOutputs().isEmpty()) {
                    outputAgent = true;
                }
            }

            if (agentManager.getConnectionType().equals(ConnectionType.MANAGER)) {

                // get the connection from the manager but create a new data
                // connection
                this.messageConnection = agentManager.getMessageConnection();
                this.dataConnection = ConnectionClient.getInstance().getDataConnection();
            } else if (agentManager.getConnectionType().equals(ConnectionType.SINGLE)) {

                // create new connections
                this.messageConnection = ConnectionClient.getInstance().getMessageConnection();
                this.messageConnection.setupConnection();
                this.dataConnection = ConnectionClient.getInstance().getDataConnection();
            } else if (agentManager.getConnectionType().equals(ConnectionType.AGENTBUNDLE)) {
                this.dataConnection = ConnectionClient.getInstance().getDataConnection();
            }

            if (this.pipes.length > 0) {

                
                for (String pipe : this.pipes) {

                    this.dataRegisterEvent = new JsonObject();

                    this.dataRegisterEvent.addProperty("runid", dataStructure.get("runid").getAsString());
                    this.dataRegisterEvent.addProperty("pipeid", pipe);

                    System.out.println("Register event " +  this.getAgentID());
                    System.out.println(this.dataRegisterEvent.toString());
                    if (!this.agentManager.getConnectionType().equals(ConnectionType.AGENTBUNDLE)) {
                        
                        this.messageConnection.register(eu.sisob.components.framework.util.Command.WRITE,
                                this.dataRegisterEvent, this);
                        System.out.println(this.agentID + " -- " + this.messageConnection);
                    } else {
                        
                        ConnectionClient.getInstance().getGlobalMessageConnection()
                                .register(eu.sisob.components.framework.util.Command.WRITE, this.dataRegisterEvent, this);
                    }

                }
            }
            setAlive(true);

        } catch (Exception e) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, e);
            //logger.log(Level.SEVERE, e.getMessage());
        }
    }

    // things regarding agent status
    // agent is done ..
    public void indicateDone() {
        // if this agent is an output agent
        if (outputAgent) {
            createResult();
        }
        this.setAgentWorkingStatus(AgentStatus.DONE);
        this.agentManager.notifyManager(this);
    }

    // if output agent then create result
    private void createResult() {

        String url = SISOBProperties.getResultUrl();

        if (url != null && !url.trim().isEmpty()) {
            baseUrl = url;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl.concat("/");
        }

        String name = "result_" + this.workflowID;

        String resultInfo = baseUrl + this.workflowID + "/" + this.agentInstanceID + "/index.html";

        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("runid", this.workflowID);
        resultJson.addProperty("instanceid", this.agentInstanceID);
        resultJson.addProperty("resultinfo", resultInfo);
        resultJson.addProperty("resultname", name);
        resultJson.addProperty("resultdescription", "");

        System.out.println(agentID + " " + agentInstanceID + "created result ");

        if (!agentManager.getConnectionType().equals(ConnectionType.AGENTBUNDLE)) {
            this.messageConnection.write(DataType.RESULT, resultJson);
        } else {
            ConnectionClient.getInstance().getGlobalMessageConnection().write(DataType.RESULT, resultJson);
        }
    }

    // finish agent
    public void finish() {
        this.setAgentWorkingStatus(AgentStatus.FINISH);
        agentManager.notifyManager(this);
    }

    public void indicateError(String msg, Throwable exception) {
        if (msg == null) {
            if (exception != null) {
                msg = exception.getMessage();
            } else {
                msg = "";
            }
        }

        // create new json object which contains all error information
        JsonObject errorMessage = new JsonObject();
        errorMessage.addProperty("runid", this.getWorkflowID());
        errorMessage.addProperty("agentid", this.agentID);
        errorMessage.addProperty("instanceid", this.getAgentInstanceID());
        errorMessage.addProperty("errormessage", msg);

        this.agentStatus = AgentStatus.ERROR;

        if (exception != null) {
            logger.log(Level.SEVERE, "Error in component " + this.agentID + ": " + msg, exception);
        } else {
            logger.severe("Error in component " + this.agentID + ": " + msg);
        }

        if (!agentManager.getConnectionType().equals(ConnectionType.AGENTBUNDLE)) {
            messageConnection.write(DataType.ERROR, errorMessage);
        } else {
            ConnectionClient.getInstance().getGlobalMessageConnection().write(DataType.ERROR, errorMessage);
        }

        this.agentManager.notifyManager(this);
    }

    public void indicateError(String msg) {
        indicateError(msg, null);
    }

    // the main stuff
    public abstract void executeAgent(JsonObject dataMessage);

    public abstract void executeAgent(List<JsonObject> dataMessages);

    protected abstract void uploadResults();

    // this saves the data to the data connection..
    protected void storeData(String workflowID, String pipeid, String data) {

        JsonArray jsonArray = new Gson().fromJson(data, JsonArray.class);
        for (JsonElement element : jsonArray) {

            String payload = element.getAsJsonObject().get("filedata").getAsString();
            String path = UUID.randomUUID().toString() + "." + element.getAsJsonObject().get("filetype").getAsString();
            dataConnection.setupConnection();
            dataConnection.writeData(path, payload);
            element.getAsJsonObject().remove("filedata");
            element.getAsJsonObject().addProperty("filedata", path);
        }
        JsonObject dataMessage = new JsonObject();
        dataMessage.addProperty("runid", workflowID);
        dataMessage.addProperty("pipeid", pipeid);
        dataMessage.add("payload", jsonArray);

        if (!agentManager.getConnectionType().equals(ConnectionType.AGENTBUNDLE)) {
            this.messageConnection.write(DataType.DATA, dataMessage);
        } else {
            ConnectionClient.getInstance().getGlobalMessageConnection().write(DataType.DATA, dataMessage);
        }
    }

    public void notifyMessage(eu.sisob.components.framework.util.Command cmd, JsonObject dataMessage) {
        if (unregister) {
            if (!agentManager.getConnectionType().equals(ConnectionType.AGENTBUNDLE)) {
                this.messageConnection.unregister(dataRegisterEvent);
            } else {
                ConnectionClient.getInstance().getGlobalMessageConnection().unregister(dataRegisterEvent);
            }
        }
        handleDataMessage(dataMessage);
    }

    private void handleDataMessage(JsonObject dataMessage) {
        // we do some locking in order to prevent some racing condition :)

        lock.lock();

        if (collectedDataMessages == null) {
            collectedDataMessages = new ArrayList<JsonObject>();
        }

        // got data message now download file from ftp.. blocking or not
        // blocking thats the question
        JsonArray payload = dataMessage.getAsJsonArray("payload");

        for (JsonElement file : payload) {
            String path = file.getAsJsonObject().get("filedata").getAsString();

            String data;

            if (agentManager.getConnectionType().equals(ConnectionType.AGENTBUNDLE)) {
                ConnectionClient.getInstance().getGlobalDataConnection().setupConnection();
                data = ConnectionClient.getInstance().getGlobalDataConnection().readData(path);
            } else {
                this.dataConnection.setupConnection();
                data = dataConnection.readData(path);
            }

            file.getAsJsonObject().remove("filedata");
            file.getAsJsonObject().addProperty("filedata", data);

            // remove temp data
            if (agentManager.getConnectionType().equals(ConnectionType.AGENTBUNDLE)) {
                ConnectionClient.getInstance().getGlobalDataConnection().setupConnection();
                ConnectionClient.getInstance().getGlobalDataConnection().removeData(path);
            } else {
                this.dataConnection.setupConnection();
                this.dataConnection.removeData(path);
            }
        }

        dataMessage.remove("payload");
        dataMessage.add("payload", payload);

        // add the data message to resultlist
        this.collectedDataMessages.add(dataMessage);

        // maybe we have more in pipes so we have to check
        if (this.pipes.length == this.collectedDataMessages.size()) {
            unregister = true;
            setAgentWorkingStatus(AgentStatus.RUNNING);
            agentManager.notifyManager(this);

            if (this.collectedDataMessages.size() == 1) {
                executeAgent(dataMessage);
            } else {
                sortDataMessages();
                executeAgent(this.collectedDataMessages);
            }
        }

        lock.unlock();
    }

    // sort the DataMessages, so that the order of the DataMessages fits the order of the pipes of the workflow
    private void sortDataMessages() {
        
        List<JsonObject> help = new ArrayList<>();

        for (String pipe : this.pipes) {
            for (JsonObject pipeObject : this.collectedDataMessages) {
                String pipeid = pipeObject.get("pipeid").getAsString();
                if (pipeid.equals(pipe)) {
                    help.add(pipeObject);
                }
            }
        }
        this.collectedDataMessages = help;
    }

    // checks every second if agent is alive, if not, agent is destroyed
    @Override
    public void run() {

        while (getAlive() == true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        }

        // close connection if connection type is single
        if (agentManager.getConnectionType().equals(ConnectionType.SINGLE)) {
            this.messageConnection.shutdown();
            this.messageConnection = null;
            this.dataConnection.shutdown();
            this.dataConnection = null;
        }
    }

    public void parseFilterParameters(String parameterString) throws IllegalArgumentException {
        try {
            filterParameters = (JSONObject) JSONValue.parse(parameterString);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Could not read parameter as JSONObject", ex);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((agentInstanceID == null) ? 0 : agentInstanceID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Agent other = (Agent) obj;
        if (agentInstanceID == null) {
            if (other.agentInstanceID != null) {
                return false;
            }
        } else if (!agentInstanceID.equals(other.agentInstanceID)) {
            return false;
        }
        return true;
    }

    public Vector<JSONFile> transformToText(Vector<JSONFile> dataSet) throws Exception {
        String outputPath = this.workflowID + "_" + this.agentInstanceID + "_workspace";

        JSONFile.writeJSONFileSet(dataSet, outputPath);

        File dir = new File(outputPath);
        Vector<JSONFile> fileSet = new Vector<JSONFile>();

        for (int i = 0; i < dir.listFiles().length; i++) {
            JSONFile dataFile = new JSONFile(dir.listFiles()[i], true);
            fileSet.add(dataFile);
        }

        File[] fileList = dir.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            File f = fileList[i];
            f.delete();
        }
        dir.delete();
        return fileSet;
    }

    public boolean isInputBASE64(Vector<JSONFile> dataSet) {
        for (JSONFile data : dataSet) {
            if (data.getSpecialFileType().trim().equals("BASE64")) {
                return true;
            }
        }
        return false;
    }

    // getter and setter
    public void setDataStructure(JsonObject dataMessage) {
        this.dataStructure = dataMessage;
    }

    public JsonObject getCoordinationMessage() {
        return coordinationMessage;
    }

    public void setCoordinationMessageStructure(JsonObject coordinationMessageStructure) {
        this.coordinationMessage = coordinationMessageStructure;
    }

    public boolean getAlive() {
        return this.isAlive;
    }

    public void setAlive(boolean alive) {
        this.isAlive = alive;
    }

    public String getWorkflowID() {
        return workflowID;
    }

    public void setWorkflowID(String workflowID) {
        this.workflowID = workflowID;
    }

    public AgentStatus getAgentStatus() {
        return agentStatus;
    }

    public void setAgentStatus(int agentStatus) {
        this.agentStatus = AgentStatus.fromId(agentStatus);
    }

    public void setAgentWorkingStatus(AgentStatus agentWorkingStatus) {
        this.agentStatus = agentWorkingStatus;
    }

    public String getAgentInstanceID() {
        return agentInstanceID;
    }

    public void setAgentInstanceID(String agentInstanceID) {
        this.agentInstanceID = agentInstanceID;
    }

    public String getAgentID() {
        return agentID;
    }

    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    public AgentManager getAgentListener() {
        return agentManager;
    }

    public void setAgentListener(AgentManager aListener) {
        this.agentManager = aListener;
    }

    public String[] getPipes() {
        return this.pipes;
    }

    public void setPipes(String[] pipes) {
        this.pipes = pipes;
    }

    public JSONObject getFilterParameters() {
        return filterParameters;
    }

    public void setFilterParameters(JSONObject parameters) {
        this.filterParameters = parameters;
    }
}
