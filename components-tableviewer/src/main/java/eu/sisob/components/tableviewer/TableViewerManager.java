package eu.sisob.components.tableviewer;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.util.ConnectionType;

public class TableViewerManager extends AgentManager {

	public TableViewerManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject commandMsg) {
		System.out.println("create TableView Agent");
		TableViewerAgent agent = new TableViewerAgent(commandMsg);
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
		String shortDescription = "This filter creates a web-view for data tables.";
		String longDescription = "The created table view is sortable by any of the available attributes.";
		Container container = new Container(shortDescription, longDescription, inputs, null, null);
		Filter filter = new Filter("Table Viewer", "Output", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

}
