package eu.sisob.components.duplexer;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.util.ConnectionType;
import org.json.simple.JSONArray;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;

public class DuplexerManager extends AgentManager {

	public DuplexerManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject commandMsg) {
		
		System.out.println("create gnAgent");
		DuplexerAgent agent = new DuplexerAgent(commandMsg);
		this.getAgents().add(agent);
		agent.setAgentListener(this);
		agent.initializeAgent();
		Thread runtime = new Thread(agent);
		runtime.start();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "any"));
		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "same as input"));
		outputs.add(new Output("out_2", "same as input"));
		JSONArray fields = null;
		String longDescription = "This filter fills both output pipes with exact copies of the input data.";
		String shortDescription = "This filter copies the input into the outputs.";
		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		Filter filter = new Filter("Duplicator", "Tools", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

}
