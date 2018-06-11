package info.collide.components.blockmodel;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ConnectionType;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.BooleanField;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.FilterCategories;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;

/**
 * @author doberstein
 */
public class BlockmodelManager extends AgentManager {
	
	public static final String COMPONENT_NAME = "Blockmodel Analysis";
	public static final String CLUSTER_COUNT = "clustCount";
	
	public static final String NULL = "null";
	public static final String REG = "regular";
	public static final String COM = "complete";
	public static final String RDO = "row dominant";
	public static final String CDO = "column dominant";
	public static final String RRE = "row regular";
	public static final String CRE = "column regular";
	public static final String DNC = "do not care";
	
	public BlockmodelManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		this.createAgent(new BlockmodelAgent(coordinationMessage));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		
		ArrayList<Filter> filters = new ArrayList<>();
		
		String shortDescription = "This filter generates a blockmodel for a given graph.";
		
		String longDescription = "The first input contains the similarity values for the nodes of the graph (e.g. generated with the similarity filter) in .stf format. "+
		"The second input is the graph in .sgf format. You can choose the number of clusters and the blocktypes. The blocks are generated with the \"crit.fun\" function. "+ 
				"For further information about this function see: https://cran.r-project.org/web/packages/blockmodeling/blockmodeling.pdf";
		
		JSONArray inputs = new JSONArray();

		inputs.add(new Input("in_1", "node similarities (table)"));

		inputs.add(new Input("in_2", "graph data"));
		
		JSONArray outputs = new JSONArray();

		outputs.add(new Output("out_1", "clustered graph"));
		outputs.add(new Output("out_2", "image"));
		
		JSONArray fields = new JSONArray();
		
		fields.add(new StringField("Number of clusters", CLUSTER_COUNT, true, "4"));

		fields.add(new BooleanField("null block", NULL, true, true));
		fields.add(new BooleanField("regular block", REG, true, true));
		fields.add(new BooleanField("complete block", COM, true, true));
		fields.add(new BooleanField("row dominant block", RDO, true, true));
		fields.add(new BooleanField("column dominant block", CDO, true, true));
		fields.add(new BooleanField("row regular block", RRE, true, true));
		fields.add(new BooleanField("column regular block", CRE, true, true));
		fields.add(new BooleanField("do not care block", DNC, true, true));
		
		
		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
     	Filter filter = new Filter(COMPONENT_NAME, FilterCategories.MODELLING, container);
     	
     	filters.add(filter);
		
		return filters;
	}

}
