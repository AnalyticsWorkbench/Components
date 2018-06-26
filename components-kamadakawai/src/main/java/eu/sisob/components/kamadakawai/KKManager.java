package eu.sisob.components.kamadakawai;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;


import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.util.ConnectionType;

public class KKManager extends AgentManager{
	
	public static final String COMPONENT_NAME = "Kamada-Kawai";

	public KKManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		super.createAgent(new KKAgent(coordinationMessage));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		
		
		String shortDescription = "The incoming network(s) are visualized using the Kamada-Kawai layout algorithm.";
		String longDescription = null;
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
