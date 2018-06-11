/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.components.graphgenerator;

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
import eu.sisob.components.framework.componentdescription.FilterCategories;

/**
 * @author doberstein
 */
public class GraphGeneratorManager extends AgentManager {

	public static final String NODES = "nodes";
	public static final String METHOD = "method";
	public static final String DIRECTED = "directed";
	public static final String EDGE_PROB = "edge_prob";
	public static final String LOOPS = "loops";
	public static final String FWBURN = "fwburn";
	public static final String BWBURN = "bwburn";
	public static final String PAPOWER = "papower";
	public static final String EDGES_ADDED = "edges_added";
	public static final String COMPONENT_NAME = "Graph Generator Filter";

	public GraphGeneratorManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
		System.out.println("commandMsg: " + commandMsg);
	}

	@Override
	protected void createAgent(JsonObject commandMsg) {
		System.out.println("create Graph Generator agent");
		GraphGeneratorAgent agent = new GraphGeneratorAgent(commandMsg);
		this.createAgent(agent);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {

		String shortDescription = "This filter generates a graph.";
		String longDescription = "The letter in the brackets indicates which fields are used in which method."
				+ " [R] =  Random; [P] = Preferential attachment; [F] = Forest-fire. For further informatrion see "
				+ "the igraph documentation: http://igraph.org/r/doc/igraph.pdf. (erdos.renyi.game for Random, sample_pa for Preferential attachment, sample_forestfire for Forest-fire)";
		JSONArray inputs = null;
		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "decorated output"));

		JSONArray fields = new JSONArray();
		
		/*since the different algorithms need different inputs some fields are only used for 
		certain algorithms
		the letter in the beginning of each field indicates for which algorithm the field is needed
		R=Random; P=Preferential-attachment; F=Forest-fire
		with the new UI, when it is possible to hide fields depending on the chosen method, this filter can be reworked
		to only show the fitting fields */
		String[] methods = { "Random", "Preferential attachment","Forest-fire" };
		fields.add(new SelectField("method", METHOD, true, methods));
		fields.add(new StringField("n [R, P, F]", NODES, true, "1"));
		String[] directed_selection = {"FALSE","TRUE"};
		fields.add(new SelectField("directed [R, P, F]", DIRECTED, true, directed_selection));
		fields.add(new StringField("edge probability [R]", EDGE_PROB, false, "0.3"));
		String[] loop_selection = {"FALSE", "TRUE"};
		fields.add(new SelectField("loops [R]", LOOPS, true, loop_selection));
		fields.add(new StringField("forward burn. [F]", FWBURN, false, "0.3"));
		fields.add(new StringField("backward burn. [F]", BWBURN, false, "0"));
		fields.add(new StringField("pref. att. power [P]", PAPOWER, false, "1"));
		fields.add(new StringField("edges added per step [P]", EDGES_ADDED, false, "3"));

		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

		Filter filter = new Filter(COMPONENT_NAME, FilterCategories.INPUT, container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

}
