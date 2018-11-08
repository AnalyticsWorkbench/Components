package eu.sisob.components.centrality;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ClassLoaderUtility;

import eu.sisob.components.rwrapper.RWrapperAgent;

import java.util.logging.Level;

public class CentralityAgent extends RWrapperAgent {

    String scriptPrefix = "eu/sisob/components/centrality/";
    private boolean keepIds;
    private boolean extendedMetadata;


    public CentralityAgent(JsonObject commandMsg) {
        super(commandMsg);

        String scriptSelectionObject = commandMsg.get("parameters").getAsString();
        JsonObject jsonObject = new Gson().fromJson(scriptSelectionObject, JsonObject.class);
        String scriptSelection = jsonObject.get("value1").getAsString();
        if (jsonObject.has("value2")) {
            keepIds = jsonObject.get("value2").getAsBoolean();
        } else {
            keepIds = true;
            logger.log(Level.WARNING, "could not find the selected KeepID ++++++ the user deosn't check for checkbox");
        }
        if (jsonObject.has("value3")) {
            extendedMetadata = jsonObject.get("value3").getAsBoolean();
            logger.log(Level.WARNING, "could not find the selected extendedMetadata ++++++ the user deosn't check for checkbox");
        } else {
            extendedMetadata = true;
        }
        String scriptName = "undirectedgraph_degree_centrality.R";
        System.out.printf(scriptName);
        if (scriptSelection.equals("Degree")) {
            scriptName = "undirectedgraph_degree_centrality.R"; //1
        } else if (scriptSelection.equals("Betweenness")) {
            scriptName = "undirectedgraph_betweenness_centrality.R";
        } else if (scriptSelection.equals("Closeness")) {  //2 fklsj dfj    'oksdhfy ewhroq nk;mansdf
            scriptName = "undirectedgraph_closeness_centrality.R";
        } else if (scriptSelection.equals("Indegree")) {
            scriptName = "in_degree_centrality.R";
        } else if (scriptSelection.equals("Outdegree")) {
            scriptName = "out_degree_centrality.R";
        } else if (scriptSelection.equals("Directed Betweenness")) {
            scriptName = "directedgraph_betweenness_centrality.R";
        } else if (scriptSelection.equals("Directed Closeness")) {
            scriptName = "directedgraph_closeness_centrality.R";
        } else if (scriptSelection.equals("Eigenvector")) {
            scriptName = "eigenvector_centrality.R";
        } else if (scriptSelection.equals("Strength (weighted Degree)")) {
            scriptName = "undirectedgraph_degree_strength.R";
        } else if (scriptSelection.equals("Incoming Strength (weighted Indegree)")) {
            scriptName = "in_degree_strength.R";
        } else if (scriptSelection.equals("Outgoing Strength (weighted Outdegree)")) {
            scriptName = "out_degree_strength.R";
        } else if (scriptSelection.equals("0")) {
            scriptSelection = "Degree";
            scriptName = "undirectedgraph_degree_centrality.R";
            logger.log(Level.WARNING, "could not find the selected script ++++++ the user choiced no centrality filetr! +++ Default is Degree-centrality");
        }

        if (scriptName != null) { // FBA never happen always true / we can handle null in front too ReDux
            this.scriptStream = ClassLoaderUtility.getClassLoader().getResourceAsStream(scriptPrefix + scriptName);
        } else {
            indicateError("could not find the selected script");
        }
    }

    @Override
    public void executeAgent(JsonObject dataMessage) {
        logger.fine(agentName + ": agent");

        this.input = new Gson().toJson(dataMessage.get("payload"));
        if (this.prepareAgentWorkspace(this.input, keepIds, extendedMetadata)) {

            if (this.executeRScript(null, keepIds)) {
                this.uploadResults();
            }
        }
    }
}
