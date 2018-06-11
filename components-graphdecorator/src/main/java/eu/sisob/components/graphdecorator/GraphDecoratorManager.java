package eu.sisob.components.graphdecorator;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.util.ConnectionType;

public class GraphDecoratorManager extends AgentManager {

	public static final String COMPONENT_NAME = "Graph Decorator";
	public static final String MERGE_STRATEGY = "merge_strat";

	public GraphDecoratorManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject commandMessage) {
		super.createAgent(new GraphDecoratorAgent(commandMessage));
	}

	@Override
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "This filter will decorate a graph with the statistical information of a data table.";
		String longDescription = "This filter will decorate a SISOB Graph with the statistical information of a SISOB Data Table. "
                        + "The edges or nodes from the graph and the data from the table are mapped by 'label'. If no label is given in the "
                        + "input data, the 'id' node property / 'id' column will be used as label instead.";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "SISOB Graph"));
		inputs.add(new Input("in_2", "SISOB Data Table"));
		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "Decorated SISOB Graph"));
		JSONArray fields = new JSONArray();
		fields.add(new SelectField("Decorate Graph With", MERGE_STRATEGY, true, "SISOB Data Table Format - Nodes", "SISOB Data Table Format - Edges"));
		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		Filter filter = new Filter(COMPONENT_NAME, "Tools", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

}
