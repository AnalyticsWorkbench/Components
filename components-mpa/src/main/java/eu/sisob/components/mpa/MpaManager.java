package eu.sisob.components.mpa;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.util.ConnectionType;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

public class MpaManager extends AgentManager {
	
	public static final String COMPONENT_NAME = "Main Path Analysis Filter";
	public static final String METHOD_VALUE = "method";
	public static final String WEIGHT_VALUE = "weight";
	public static final String THRESHOLD_VALUE = "threshold";
	public static final String RANGE_VALUE = "range";

	public MpaManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		
		// now simplified:
		this.createAgent(new MpaAgent(coordinationMessage));

	}

    @SuppressWarnings("unchecked")
	@Override
    protected List<Filter> getFilterDescriptions() {
        
        String shortDescription = "This filter performs a main path analysis.";
    	String longDescription = "The threshold needs to be a double value between 0 and 1, e.g. 0.7. A value of 1 will give you the classical form of MPA, lower values allow more branches.";
    	JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "directed acyclic graph"));
    	JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "main path as subgraph."));
        outputs.add(new Output("out_2", "decorated input."));
        
    	
    	JSONArray fields = new JSONArray();
    	String[] methods = {"SPC", "SPC with existing weights - pre", "SPC with existing weights - post", "SPC (norm.) with existing weights - post", "Existing weights only"};
        fields.add(new SelectField("method", METHOD_VALUE, true, methods));

        fields.add(new StringField("name of existing weights - if used", WEIGHT_VALUE, true, "weight"));
        
        fields.add(new StringField("threshold", THRESHOLD_VALUE, true, "1"));
    	
        String[] mpdetectionmethods = {"local", "global", "globallocal"};
        fields.add(new SelectField("range", RANGE_VALUE, true, mpdetectionmethods));
        
    	Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
    	
    	Filter filter = new Filter(COMPONENT_NAME, "Analysis", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
    	return filters;
    }

}
