package info.collide.components.cliquepercolation;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.FilterCategories;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.IntField;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.util.ConnectionType;


public class CliquepercolationManager extends AgentManager {
	
	public static final String COMPONENT_NAME = "Cliquepercolation Filter";
	
	public static final String K = "k";

	public CliquepercolationManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		// TODO Auto-generated method stub
		
		this.createAgent(new CliquepercolationAgent(coordinationMessage));
		
	}

    @SuppressWarnings("unchecked")
	@Override
    protected List<Filter> getFilterDescriptions() {
        
        String shortDescription = "Overlapping subgroup detection.";
    	String longDescription = "For detailled information on the method see 'Uncovering the overlapping community structure of complex networks in nature and society' by Palla et. al. (2005)";
    	JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "graph"));
    	JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "clustered graph."));
    	
    	JSONArray fields = new JSONArray();
   
        fields.add(new IntField("k", K, true, 3));
        
    	Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
    	
    	Filter filter = new Filter(COMPONENT_NAME, FilterCategories.COMMUNITY_DETECTION, container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
    	return filters;
    }

	

}
