package eu.sisob.components.centrality;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.components.rwrapper.RWrapperAgent;
import info.collide.util.ClassLoaderUtility;

public class CentralityAgent extends RWrapperAgent {

	String scriptPrefix = "eu/sisob/components/centrality/";
	private boolean keepIds;
	private boolean extendedMetadata;

	public CentralityAgent(JsonObject commandMsg) {
		super(commandMsg);

		String scriptSelectionObject = commandMsg.get("parameters").getAsString();
		JsonObject jsonObject = new Gson().fromJson(scriptSelectionObject, JsonObject.class);
		String scriptSelection = jsonObject.get("value1").getAsString();

		String scriptName = null;

		if (scriptSelection.equals("Degree")) {
			scriptName = "undirectedgraph_degree_centrality.R";
		} else if (scriptSelection.equals("Betweenness")) {
			scriptName = "undirectedgraph_betweenness_centrality.R";
		} else if (scriptSelection.equals("Closeness")) {
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
		}

		if (scriptName != null) {
			this.scriptStream = ClassLoaderUtility.getClassLoader().getResourceAsStream(scriptPrefix + scriptName);
		} else {
			indicateError("could not find the selected script");
		}

		keepIds = jsonObject.get("value2").getAsBoolean();
		extendedMetadata = jsonObject.get("value3").getAsBoolean();
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
