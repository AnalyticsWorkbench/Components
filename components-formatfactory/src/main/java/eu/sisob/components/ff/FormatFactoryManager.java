package eu.sisob.components.ff;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.util.ConnectionType;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;

public class FormatFactoryManager extends AgentManager {
	
	public static final String INPUT_PARAMETER = "input_format";
	public static final String OUTPUT_PARAMETER = "output_format";

	public FormatFactoryManager(JsonObject templateCommand, String mngId, ConnectionType connectionType) {
		super(templateCommand, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject commandJson) {
		FormatFactoryAgent ffa = new FormatFactoryAgent(commandJson);
		getAgents().add(ffa);
		/* Adding the listeners */
		ffa.setAgentListener(this);
		ffa.initializeAgent();

	        /* Creating the agent runtime */
		Thread runTime = new Thread(ffa);
	        /* Executing the agent */
		runTime.start();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "This filter converts the given input format to the given output format";
		String longDescription = null;
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "Data"));
		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "Data"));
		JSONArray fields = new JSONArray();
		
		fields.add(new SelectField("Input", INPUT_PARAMETER, true, "Pajek", "GML", "SISOB Graph Format", "SISOB Data Table Format - Nodes","SISOB Data Table Format - Edges", "UCINET DL Format","CSV Standard","CSV Excel"));
		fields.add(new SelectField("Output", OUTPUT_PARAMETER, true, "SISOB Graph Format", "SISOB Data Table Format - Nodes","SISOB Data Table Format - Edges","Pajek","GML", "UCINET DL Format","CSV Standard","CSV Excel"));
		
		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		Filter filter = new Filter("Format Transformation", "Data Converters", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}
	
}
