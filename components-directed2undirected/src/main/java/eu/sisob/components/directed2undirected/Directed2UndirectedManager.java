package eu.sisob.components.directed2undirected;

import com.google.gson.JsonObject;
import info.collide.sqlspaces.commons.Tuple;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.BooleanField;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.util.ConnectionType;

public class Directed2UndirectedManager extends AgentManager{

	public static final String COMPONENT_NAME = "Directed 2 Undirected";
	public static final String METHOD = "method";
	public static final String REMOVE_WEIGHTS = "rem_weights";
	public static final String WEIGHT_NAME = "weight_name";
	public static final String WEIGHT_EXISTS = "weight_exists";
	
	public Directed2UndirectedManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		this.createAgent(new Directed2UndirectedAgent(coordinationMessage));
	}

	@Override
	protected List<Filter> getFilterDescriptions() {
		// TODO Auto-generated method stub
		ArrayList<Filter> filterList = new ArrayList<Filter>(1);
		String shortDescription = "This component will transform a directed into an undirected graph.";
		String longDescription = "This component will transform a directed into an undirected graph. Depending on the chosen transformation method edge weights will be available afterwards or not. Will destroy all other edge properties! Is not suited for multirelational graphs!";
		
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "network data"));
    	
		JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "network data"));    	
    	
    	JSONArray fields = new JSONArray();
    	String[] methods = {"maximum weight", "minimal weight", "mean weight", "sum of weights"};
    	fields.add(new SelectField("Transformation method", METHOD, true, methods));
    	fields.add(new BooleanField("Remove weights", REMOVE_WEIGHTS, true, false));
    	fields.add(new StringField("Name of the edge weight", WEIGHT_NAME, true, "weight"));
    	fields.add(new BooleanField("Edge weights exist", WEIGHT_EXISTS, true, true));
    	
    	Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
    	Filter filter = new Filter(COMPONENT_NAME, "Tools", container);
    	filterList.add(filter);
		return filterList;
	}

}
