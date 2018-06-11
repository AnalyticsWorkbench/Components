package info.collide.workbench.components.graphpropertyfilter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.util.ConnectionType;


/**
 * This class removes nodes and edges with given properties and values  
 * @author ziebarth
 */
public class GraphPropertyFilterManager extends AgentManager{
	
	public static final String COMPONENT_NAME = "Graph Property Filter";
	
	public static final String parameterFilterType = "filtertype";
	public static final String parameterProperty = "property";
	public static final String parameterOperator = "operator";
	public static final String parameterValue = "value";
	
	

	public GraphPropertyFilterManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}
	
	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		this.createAgent(new GraphPropertyFilterAgent(coordinationMessage));
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "This filter removes nodes or edges with the given properties and values";
    	String longDescription = "This filter removes nodes or edges with the given properties and values";
    	
    	JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "network"));
    	
        JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "filtered network"));
		
    	JSONArray fields = new JSONArray();
    	
    	String[] types = {
    			"nodes",
    			"edges",
    	};
    	fields.add(new SelectField("remove", parameterFilterType, true, types));
    	
    	fields.add(new StringField("property", parameterProperty, true));    	
    	
    	String[] comparison = {
    			"=",
    			"!=",
    			">",
    			"<",
    			">=",
    			"<=",
    	};
    	fields.add(new SelectField("operator", parameterOperator, true, comparison));
    	fields.add(new StringField("value", parameterValue, true));
        
	    Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		
	    Filter filter = new Filter(COMPONENT_NAME, "Tools", container);
 		List<Filter> filters = new ArrayList<Filter>(1);
 		filters.add(filter);
 		return filters;
	}

}
