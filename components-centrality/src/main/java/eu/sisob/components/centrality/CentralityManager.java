/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import org.json.simple.JSONObject;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;

/**
 *
 * @author walter
 *
 */
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
	private String[] getAvailableScripts() {

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
	@Override
	protected List<Filter> getFilterDescriptions() {

		String shortDescription = "This filter calculates centrality.";
        String longDescription = " The output will be a graph enriched by centrality measures.\n If 'extended metadata' is checked, string-metadata-attributes are kept during format conversions.";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "activity stream"));
		JSONArray outputs = new JSONArray();
		System.out.println("Centrallity input :");
		System.out.println(inputs);
		outputs.add(new Output("out_1", "activity stream"));
		JSONArray fields = new JSONArray();
		System.out.println(fields);
		fields.add(new SelectField("centrality measures", "value1", true, this.getAvailableScripts()));
		fields.add(new BooleanField("keep ids", "value2", true, false));
		fields.add(new BooleanField("extended metadata", "value3", true, false));

		String jsTransformMeta = this.readFile("Centrality-transform-meta.js");
		String jsUpdateForm = this.readFile("Centrality-Update-form.js");
		JSONObject form = this.readFormJSON("Centrality-form.json");
		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields, null , null , jsTransformMeta); // FBA 3 MATADATA
		Filter filter = new Filter("Centrality", "Analysis", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}
}
