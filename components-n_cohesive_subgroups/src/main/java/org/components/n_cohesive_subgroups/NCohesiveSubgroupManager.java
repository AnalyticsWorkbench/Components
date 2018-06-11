/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.components.n_cohesive_subgroups;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.util.ConnectionType;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.componentdescription.FilterCategories;

/**
 *
 * @author hecking
 */
public class NCohesiveSubgroupManager extends AgentManager {

	public static final String N = "n";
	public static final String METHOD = "method";

	public NCohesiveSubgroupManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject commandMsg) {
		System.out.println("create N Cohesive Subgroups agent");
//		boolean canStart = true;
		NCohesiveSubgroupsAgent agent = new NCohesiveSubgroupsAgent(commandMsg);
		this.createAgent(agent);
		
//		agent.setAgentListener(this);
//		this.getAgents().add(agent);
//		agent.initializeAgent();
//		if (canStart) {
//			Thread runtime = new Thread(this);
//			runtime.start();
//		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {

		String shortDescription = "This filter identifies cohesive substructures in graphs.";
		String longDescription = "The filter searches for discrepant cohesive subgroups in the input graphs. "
				+ "The parameter n regulates the relaxivity of the subgroups. The output will be a graph enriched by group allocations.";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in", "graph"));
		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "decorated output"));

		JSONArray fields = new JSONArray();

		fields.add(new StringField("n", N, true, "1"));
		fields.add(new SelectField("method", METHOD, true, this.getAvailableMethods()));

		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

		Filter filter = new Filter("N Cohesive Subgroups", FilterCategories.COMMUNITY_DETECTION, container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
		// TODO Auto-generated method stub
	}

	private String[] getAvailableMethods() {

		// String[] scripts = {
		// "directedgraph_betweenness_centrality",
		// "undirectedgraph_betweenness_centrality",
		// "directedgraph_closeness_centrality",
		// "undirectedgraph_closeness_centrality",
		// "in_degree_centrality",
		// "out_degree_centrality",
		// "undirectedgraph_degree_centrality"
		// };

		String[] methods = { "n-cliques", "n-clans" };

		return methods;
	}
}
