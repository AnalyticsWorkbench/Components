package eu.sisob.components.findnodesubset;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;


import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.util.ConnectionType;

public class FindNodeSubsetManager extends AgentManager{

	public static final String COMPONENT_NAME = "Find Node Subset";
	public static final String LARGEST_DETERMINER = "largest_determiner";
	public static final String THRESHOLD = "threshold";
	public static final String EDGE_WEIGHT_NAME = "edge_w_name";
	public static final String MIN_SIZE = "min_size";
	public static final String IDEAL_NUM = "ideal_num";
	
	public FindNodeSubsetManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}
	
	@Override
	protected void createAgent(JsonObject commandMsg) {
		System.out.println("create Find Node Subset agent");
//		boolean canStart = true;
		FindNodeSubsetAgent agent = new FindNodeSubsetAgent(commandMsg);
		this.createAgent(agent);
	}
	
	  @SuppressWarnings("unchecked")
		@Override
	    protected List<Filter> getFilterDescriptions() {
	        
	        String shortDescription = "This filter finds a subset of nodes in a weighted graph.";
	    	String longDescription = "This filter finds a subset of nodes in a weighted graph. For this it sequentially deletes " +
	    			"the edges with the lowest weight according to an increasing threshold. Then it considers the largest component of the graph." +
	    			"'Largest' is either defined by the number of nodes or by the sum of edge weights of edges within the component." +
	    			"The nodeset returned is either that just before the seed node is no longer in the largest component" +
	    			"or is determined by the time the sum of edge weights and the average edge weight * SumOfAllEdgeWeights intersect.";
	    	JSONArray inputs = new JSONArray();
	        inputs.add(new Input("in_1", "graph"));
	    	JSONArray outputs = new JSONArray();
	    	outputs.add(new Output("out_1", "the found subset"));
	        outputs.add(new Output("out_2", "decorated input"));
	      
	        JSONArray fields = new JSONArray();
	        SelectField largestComponent = new SelectField("Largest Component Determiner", LARGEST_DETERMINER, true, "Number of Nodes", "Sum of Edge Weights");
	        fields.add(largestComponent);
	        SelectField findTheta = new SelectField("Threshold to select", THRESHOLD, true, "Intersection of avg. edge weight and sum of weights", "Just before seed node is lost", "Given number of recommendations");
	        fields.add(findTheta);
	        StringField edgeWeightName = new StringField("Name of the Edge Weight", EDGE_WEIGHT_NAME, true, "weight");
	        fields.add(edgeWeightName);
	        StringField minSize = new StringField("min. Size of component (only for Seed)", MIN_SIZE, true, "1");
	        fields.add(minSize);
	        StringField fixedNumber = new StringField("Ideal number of recommendations (only for FixedNumber)", IDEAL_NUM, true, "10");
	        fields.add(fixedNumber);
	        
	        Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
	    	
	    	Filter filter = new Filter(COMPONENT_NAME, "Analysis", container);
			List<Filter> filters = new ArrayList<Filter>(1);
			filters.add(filter);
	    	return filters;
	    }
}
