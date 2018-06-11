package eu.sisob.components.cosinesimilarity;

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

public class CosineSimilarityManager extends AgentManager {
	
	public CosineSimilarityManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}
	
	@Override
	protected void createAgent(JsonObject commandMessage) {
		super.createAgent(new CosineSimilarityAgent(commandMessage));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "This component creates a word-word similarity network.";
		String longDescription = "This component creates a word-word network including their similarity as a measure.";		
		
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "SISOB Graph"));
		
		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "SISOB Graph"));
		
		JSONArray fields = new JSONArray();		
        
		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		Filter filter = new Filter("Cosine Similarity", "Analysis", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

}
