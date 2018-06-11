package eu.sisob.components.fdc;

import com.google.gson.JsonObject;
import info.collide.sqlspaces.commons.Tuple;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.util.ConnectionType;

public class FDCManager extends AgentManager{
	
	public static final String COMPONENT_NAME = "Force Directed Clustering";

	public FDCManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}
	

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		super.createAgent(new FDCAgent(coordinationMessage));
	}

	@Override
	protected List<Filter> getFilterDescriptions() {
		// TODO Auto-generated method stub
		String shortDescription = "Visualizes networks with cluster information with the Dwyer force directed layout enhancing it with convex hulls showing the clusters.";
		String longDescription = "Cluster information is a necessary is required! Possible sources for cluster information are community detection analyses.";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "SISOB Graph Data"));
		JSONArray outputs = null;
		JSONArray fields = null;
		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		Filter filter = new Filter(COMPONENT_NAME, "Graph Visualizations", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

}
