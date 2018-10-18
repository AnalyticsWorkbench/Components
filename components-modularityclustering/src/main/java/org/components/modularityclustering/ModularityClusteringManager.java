package org.components.modularityclustering;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.BooleanField;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.util.ConnectionType;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.componentdescription.FilterCategories;

/**
 * 
 * @author Laura Steinert
 */
public class ModularityClusteringManager extends AgentManager {

	public static final String NAMEOFEDGEWEIGHT = "value2";
	public static final String USEEDGEWEIGHT = "value1";
        
	public ModularityClusteringManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject commandMsg) {
		System.out.println("create N Cohesive Subgroups agent");
		// boolean canStart = true;
		ModularityClusteringAgent agent = new ModularityClusteringAgent(commandMsg);
		this.createAgent(agent);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "This filter clusters the graph based on modularity.";
		String longDescription = "This filter clusters the graph based on modularity.";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "sisob graph"));
		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "sisob graph"));

		JSONArray fields = new JSONArray();
		fields.add(new BooleanField("use edgeweights", USEEDGEWEIGHT, true, Boolean.TRUE));
		fields.add(new StringField("name of edgeweight - if used", NAMEOFEDGEWEIGHT, true, "weight"));

		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		Filter filter = new Filter("Modularity Clustering", FilterCategories.COMMUNITY_DETECTION, container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

}
