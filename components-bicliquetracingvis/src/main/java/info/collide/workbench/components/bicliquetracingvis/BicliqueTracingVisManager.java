package info.collide.workbench.components.bicliquetracingvis;

import com.google.gson.JsonObject;


import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.util.ConnectionType;

public class BicliqueTracingVisManager extends AgentManager {

    protected static final String SIMILARITY_MEASURE_PARAM = "sim_measure";
	public BicliqueTracingVisManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		super.createAgent(new BicliqueTracingVisAgent(coordinationMessage));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "This component creates a visualization of the evolution of clusters and their groups.";
		String longDescription = "The Similarity Coloring value should be the same as the similarity measure used during the analysis.";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "SISOB Graph"));
		JSONArray fields = new JSONArray();
                
		fields.add(new SelectField("Similarity coloring", SIMILARITY_MEASURE_PARAM, true, "Jaccard", "Inclusion"));
		Container container = new Container(shortDescription, longDescription, inputs, null, fields);
		Filter filter = new Filter("Biclique Tracing Visualization", "Graph Visualizations",
				container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

	

}
