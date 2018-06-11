package info.collide.workbench.components.dynnetvis;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.util.ConnectionType;

public class DynNetVisManager extends AgentManager{

	public DynNetVisManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
		System.out.println("3DDynNetVis Manager started");
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		// create a new instance of tutorial agent
		DynNetVisAgent agent = new DynNetVisAgent(coordinationMessage);
		
		// add it to the agents list
		getAgents().add(agent);
		// tell the agent to which manager it belongs
		agent.setAgentListener(this);
		// initialize agent
		agent.initializeAgent();

		// start the agent
		Thread runTime = new Thread(agent);
		runTime.start();
		System.out.println("3DDynNetVis Agent created");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "any"));
		String shortDescription = "This filter creates a 3D visualization for dynamic graphs.";
		String longDescription = "This filter is based on the 3D DynNetVis visualization from the Henrik Detjen's Master's Thesis.";
		Container container = new Container(shortDescription, longDescription, inputs, null, null);
		Filter filter = new Filter("3D Dynamic Network Visualization", "Graph Visualizations", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
    	return filters;
	}

}
