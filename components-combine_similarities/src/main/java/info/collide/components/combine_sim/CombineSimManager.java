package info.collide.components.combine_sim;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ConnectionType;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.BooleanField;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;

/**
 * @author doberstein
 */
public class CombineSimManager extends AgentManager {

	public static final String COMPONENT_NAME = "Linear Combination";
	public static final String SIGMA1 = "sig1";
	public static final String SIGMA2 = "sig2";
	

	public CombineSimManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		this.createAgent(new CombineSimAgent(coordinationMessage));

	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		
		ArrayList<Filter> filters = new ArrayList<>();
		
		String shortDescription = "This filter performs a linear combination of matrices (e.g. similarity matrices).";
		
		String longDescription = "The two inputs need to be two tables in \"stf\" format. The nodes in the matrices do not have to be "
				+ "named the same, but they have to be in the same order. Output in \"stf\" format.";
		
		JSONArray inputs = new JSONArray();

		inputs.add(new Input("in_1", "data1"));
		inputs.add(new Input("in_2", "data2"));
		
		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "data"));
		
		JSONArray fields = new JSONArray();
		
		
		fields.add(new StringField("Sigma1", SIGMA1, true, "1"));
		fields.add(new StringField("Sigma2", SIGMA2, true, "1"));
		 

		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

     	Filter filter = new Filter(COMPONENT_NAME, "Tools", container);
     	
     	filters.add(filter);
		
		return filters;
	}

}
