package info.collide.workbench.components.edgefilter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.FilterCategories;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.util.ConnectionType;


/**
 * This filter keeps edges which have source and target nodes that
 * are equal or not equal to the provided parameters  depending on the chosen operator.
 * @author ziebarth
 */
public class EdgeFilterManager extends AgentManager{
	
	public static final String COMPONENT_NAME = "Edge Filter";
	public static final String PARAMETER_OPERATOR = "operator";
	
	public static final String PARAMETER_SOURCE_ATTRIBUTE = "source attribute";
	public static final String PARAMETER_SOURCE_VALUE = "source value";
	public static final String PARAMETER_TARGET_ATTRIBUTE = "target attribute";
	public static final String PARAMETER_TARGET_VALUE = "target value";
	
	public static final String operatorEqual = "==";
	public static final String operatorNotEqual = "!=";
	
   	public static final String[] operatorTypes = {operatorEqual, operatorNotEqual};
	
	

	public EdgeFilterManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}
	
	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		this.createAgent(new EdgeFilterAgent(coordinationMessage));
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
    	
 
    	
    	fields.add(new SelectField(PARAMETER_OPERATOR, PARAMETER_OPERATOR, true, operatorTypes));
    	fields.add(new StringField(PARAMETER_SOURCE_ATTRIBUTE,PARAMETER_SOURCE_ATTRIBUTE, true));
    	fields.add(new StringField(PARAMETER_SOURCE_VALUE,PARAMETER_SOURCE_VALUE, true));
    	
    	fields.add(new StringField(PARAMETER_TARGET_ATTRIBUTE,PARAMETER_TARGET_ATTRIBUTE, true));
    	fields.add(new StringField(PARAMETER_TARGET_VALUE,PARAMETER_TARGET_VALUE, true));
        
	    Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		
	    Filter filter = new Filter(COMPONENT_NAME, FilterCategories.FILTER, container);
 		List<Filter> filters = new ArrayList<Filter>(1);
 		filters.add(filter);
 		return filters;
	}

}
