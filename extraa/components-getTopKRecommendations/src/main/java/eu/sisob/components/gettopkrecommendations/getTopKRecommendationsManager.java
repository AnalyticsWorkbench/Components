package eu.sisob.components.gettopkrecommendations;

import com.google.gson.JsonObject;


import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.util.ConnectionType;

public class getTopKRecommendationsManager extends AgentManager{
	
	public static final String COMPONENT_NAME = "Get Top k Recommendations";
	public static final String K = "k";
	public static final String REC_ATTRIBUTE = "rec_att";
	
	public getTopKRecommendationsManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}
	
	@Override
	protected void createAgent(JsonObject commandMessage) {
		super.createAgent(new getTopKRecommendationsAgent(commandMessage));
	}
	
	  @SuppressWarnings("unchecked")
		@Override
	    protected List<Filter> getFilterDescriptions() {
	        
	        String shortDescription = "This filter gets the top k recommendations.";
	    	String longDescription = "This filter gets the top k recommendations using betweenness centrality";
	    	JSONArray inputs = new JSONArray();
	        inputs.add(new Input("in_1", "graph"));
	    	JSONArray outputs = new JSONArray();
	    	outputs.add(new Output("out_1", "the top k recommendations"));
	        outputs.add(new Output("out_2", "input"));
	      
	        JSONArray fields = new JSONArray();
	        StringField k = new StringField("k", K, true, "10");
	        fields.add(k);
	        StringField nameRecommendationFlag = new StringField("Attribute that indicates Recommendation", REC_ATTRIBUTE, true, "isInNodeSubset");
	        fields.add(nameRecommendationFlag);
//	        StringField weightName = new StringField("Weight Name", "value3", true, "isInNodeSubset");
//	        fields.add(weightName);
	        
	        Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
	    	
	    	Filter filter = new Filter(COMPONENT_NAME, "Analysis", container);
			List<Filter> filters = new ArrayList<Filter>(1);
			filters.add(filter);
	    	return filters;
	    }
}
