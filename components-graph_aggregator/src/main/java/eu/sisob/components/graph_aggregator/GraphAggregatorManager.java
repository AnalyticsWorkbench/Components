/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.components.graph_aggregator;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.BooleanField;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.FilterCategories;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.util.ConnectionType;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;

/**
 *
 * @author hecking
 */
public class GraphAggregatorManager extends AgentManager {
	
	public static final String COMPONENT_NAME = "Graph Aggregator";
	public static final String METHOD = "method";
	public static final String BY_LABEL = "by_label";
 
    public GraphAggregatorManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

    @Override
    protected void createAgent(JsonObject commandMsg) {
		System.out.println("create GraphAggregator agent");
		GraphAggregatorAgent agent = new GraphAggregatorAgent(commandMsg);
		this.createAgent(agent);
	}

    @SuppressWarnings("unchecked")
	@Override
    protected List<Filter> getFilterDescriptions() {
        
        String shortDescription = "Aggregation of input graphs.";
    	String longDescription = "This filter aggregates a series of input graphs by the specified aggregation function. "
                + "If \"Match vertices by label\" is deselected vertices are matched by their id.";
    	JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "graphs"));
    	JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "aggregated graph."));
    	
    	JSONArray fields = new JSONArray();
   
        fields.add(
                new SelectField("script", METHOD, true, 
                new String[] {"intersection", "union"}));
        
        fields.add(new BooleanField("Match nodes by label", BY_LABEL, true, true));
        
    	Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
    	
    	Filter filter = new Filter(COMPONENT_NAME, FilterCategories.FILTER, container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
    	return filters;
    }
}
