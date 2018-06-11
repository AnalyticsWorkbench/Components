package info.collide.workbench.components.isolatednodefilter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.util.ConnectionType;


public class SingleNodeFilterManager extends AgentManager {
	
	public static final String COMPONENT_NAME = "Isolated Node Filter";
	
	public SingleNodeFilterManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		 super(coordinationMessageTemplate, managerName, connectionType);
	}
	
	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		// create a new instance of tutorial agent
		createAgent(new SingleNodeFilterAgent(coordinationMessage));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "This filter removes nodes, which have no edges.";
    	String longDescription = "This filter removes nodes, which have no edges.";
    	
    	JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "network"));
    	
        JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "filtered network"));
    	
    	JSONArray fields = new JSONArray();
    	
    	Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
 		
 	    Filter filter = new Filter(COMPONENT_NAME, "Tools", container);
  		List<Filter> filters = new ArrayList<Filter>(1);
  		filters.add(filter);
  		return filters;
	}

}
