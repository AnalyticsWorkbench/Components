package eu.sisob.components.linkcommunity;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.util.ConnectionType;
/**
 * @see LinkCommunitiesAgent
 * @author Evelyn Fricke
 *
 */
public class LinkCommunitiesAgentManager extends AgentManager
{
	public static final String COMPONENT_NAME ="Link Communities Analysis";
	
	public LinkCommunitiesAgentManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}
	    
	    
	 	/**
	 	 * Creates an Link Community Agent in a new thread.
	 	 */
	    @Override
	    protected void createAgent(JsonObject commandMessage) {
		super.createAgent(new LinkCommunitiesAgent(commandMessage));
	}
	    
	    @SuppressWarnings("unchecked")
		@Override
	    protected List<Filter> getFilterDescriptions() {
	    	String shortDescription = "Finds link communities on a network.";
	    	String longDescription = "Link communities are overlapping cohesive subgroups. The algorithm is implemented for unweighted, undirected graphs, one mode networks only.";
	    	JSONArray inputs = new JSONArray();
	    	inputs.add(new Input("in_1", "SISOB-Graph as JSONFile"));
	    	JSONArray outputs = new JSONArray();
	    	outputs.add(new Output("out_1", "SISOB-Graph as JSONFile"));
	    	JSONArray fields = new JSONArray();
	    	
	    	Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
	    	
	    	Filter filter = new Filter(COMPONENT_NAME, "Analysis", container);
			List<Filter> filters = new ArrayList<Filter>(1);
			filters.add(filter);
	    	return filters;
	    }
}
