package eu.sisob.components.fgl;

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

public class FGLManager extends AgentManager{
	
	public static final String COMPONENT_NAME = "Foresighted Graph Layout";
	public static final String LAYOUT = "layout";
	public static final String STABILIZATION_METHOD = "stab_method";

	public FGLManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		super.createAgent(new FGLAgent(coordinationMessage));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "The incoming network(s) are visualized using Foresighted Graph Layout.";
		String longDescription = "This filter is based on the Foresighted Graph Layout by Diehl et. al. (2000)";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "SISOB Graph Data"));
		JSONArray outputs = null;
		JSONArray fields = new JSONArray();
		fields.add(new SelectField("Drawing Algorithm", LAYOUT, true, "Circular", "Kamada-Kawai", "Fruchterman-Rheingold"));		
		fields.add(new SelectField("Stabilization Method", STABILIZATION_METHOD, true, "Default","Degree","Flicker", "Gap","Isolation"));		
		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		Filter filter = new Filter(COMPONENT_NAME, "Graph Visualizations", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}
	
}
