package org.components.similarity;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.util.ConnectionType;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

/**
 * @author doberstein
 */

public class SimilarityManager extends AgentManager {

	public static final String COMPONENT_NAME = "Similarity Filter";
	public static final String METHOD = "method";

	public SimilarityManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject commandMsg) {
		SimilarityAgent agent = new SimilarityAgent(commandMsg);
		this.createAgent(agent);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {

		String shortDescription = "This filter calculates the similarity values for the nodes of a graph";
		String longDescription = "The input needs to be a graph in \"sgf\" format. Based on the selected measure the filter generates the similarities and the output is in \"stf\" format";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in", "graph in sgf format"));
		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "node similarity in stf format"));

		JSONArray fields = new JSONArray();

		String[] methods = {"regular", "structural"};
		
		fields.add(new SelectField("similarity measure", METHOD, true, methods));

		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

		Filter filter = new Filter(COMPONENT_NAME, "Analysis", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
		// TODO Auto-generated method stub
	}
}
