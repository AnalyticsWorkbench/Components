package eu.sisob.components.supergraph;

import com.google.gson.JsonObject;


import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.util.ConnectionType;

public class SuperGraphManager extends AgentManager{
	
	public static final String COMPONENT_NAME = "Super Graph";
	public static final String METHOD = "method";

	public SuperGraphManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		super.createAgent(new SuperGraphAgent(coordinationMessage));
	}

	@Override
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "The incoming network(s) are visualized using Super Graph Layout.";
		String longDescription = "This filter is based on the Super Graph Layout by Diehl et. al. (2000)";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "SISOB Graph Data"));
		JSONArray outputs = null;
		JSONArray fields = new JSONArray();
		fields.add(new SelectField("Layout", METHOD, true, "Circular", "Kamada-Kawai", "Fruchterman-Rheingold"));
		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		Filter filter = new Filter(COMPONENT_NAME, "Graph Visualizations", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

}
