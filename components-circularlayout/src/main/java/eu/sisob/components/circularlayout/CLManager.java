package eu.sisob.components.circularlayout;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import info.collide.sqlspaces.commons.Tuple;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.util.ConnectionType;

public class CLManager extends AgentManager{
	
	public static final String COMPONENT_NAME = "Circular Layout";

	public CLManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		super.createAgent(new CLAgent(coordinationMessage));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "The incoming network(s) are visualized using circular layout.";
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
