package eu.sisob.components.centrality;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.sisob.components.framework.componentdescription.*;
import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.util.ConnectionType;

public class CentralityManager extends AgentManager {

	public CentralityManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject commandMsg) {
		System.out.println("create centrality agent");
		boolean canStart = true;
		CentralityAgent agent = new CentralityAgent(commandMsg);
		this.getAgents().add(agent);
		agent.setAgentListener(this);
		agent.initializeAgent();
		if (canStart) {
			Thread runtime = new Thread(agent);
			runtime.start();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {

		String shortDescription = "This filter calculates centrality.";
		String longDescription = " The output will be a graph enriched by centrality measures.\n If 'extended metadata' is checked, string-metadata-attributes are kept during format conversions.";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in", "graph"));
		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "decorated output"));

		JSONArray fields = new JSONArray();

		fields.add(new SelectField("centrality measures", "value1", true, this.getAvailableScripts()));
		fields.add(new BooleanField("keep ids", "value2", true, false));
		//fields.add(new SelectField("centrality measures","value1" , true, this.getAvailableScripts())); // added by FB4
		fields.add(new BooleanField("extended metadata", "value3", true, false));

		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

		Filter filter = new Filter("Centrality", "Analysis", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
		// TODO Auto-generated method stub
	}

	private String[] getAvailableScripts() {

		// String[] scripts = {
		// "directedgraph_betweenness_centrality",
		// "undirectedgraph_betweenness_centrality",
		// "directedgraph_closeness_centrality",
		// "undirectedgraph_closeness_centrality",
		// "in_degree_centrality",
		// "out_degree_centrality",
		// "undirectedgraph_degree_centrality"
		// };

		String[] scripts = { "Degree", "Betweenness", "Closeness", "Indegree", "Outdegree", "Directed Betweenness",
				"Directed Closeness", "Eigenvector", "Strength (weighted Degree)",
				"Incoming Strength (weighted Indegree)", "Outgoing Strength (weighted Outdegree)" };

		String repolocation = SISOBProperties.getProperty("centrality.scriptrepository");
		if (repolocation != null) {
			File scriptRepo = new File(repolocation);
			File[] scriptFiles;
			int oldLength = scripts.length;
			if (scriptRepo.exists()) {

				scriptFiles = scriptRepo.listFiles();
				scripts = Arrays.copyOf(scripts, (scriptFiles.length + oldLength));

				for (int i = oldLength; i < scripts.length; i = i + 1) {

					scripts[i] = scriptFiles[i - oldLength].getName();
				}
			}
		}
		return scripts;
	}
}
