package eu.sisob.components.maxflow;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.util.ConnectionType;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;

/**
 * 
 * @author Laura Steinert 
 * based on the MpaManager by Tobias Hecking
 * 
 */
public class MaxFlowManager extends AgentManager {
	
	public static final String COMPONENT_NAME = "Max Flow Analysis";

	public MaxFlowManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}
	

	@Override
	protected void createAgent(JsonObject commandMsg) {
		System.out.println("create Max Flow agent");
		MaxFlowAgent agent = new MaxFlowAgent(commandMsg);
		this.createAgent(agent);
	}

    @SuppressWarnings("unchecked")
	@Override
    protected List<Filter> getFilterDescriptions() {        
        String shortDescription = "This filter calculates the maximum flow for a given flow graph.";
    	String longDescription = "This filter calculates the maximum flow for a given flow graph.";
    	JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "flow graph"));
    	JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "flow graph with max. flow as flow"));
    	Container container = new Container(shortDescription, longDescription, inputs, outputs, null);    	
    	Filter filter = new Filter(COMPONENT_NAME, "Analysis", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
    	return filters;
    }

}
