package info.collide.workbench.components.mongoexporter;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.util.ConnectionType;

/**
 * @author verheyen
 * 
 */
public class MongoExporterManager extends AgentManager {

	public MongoExporterManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}
	
	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		createAgent(new MongoExporterAgent(coordinationMessage));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "any"));
		Container container = new Container("Exports JSON data into MongoDB.",
				"This filter exports JSON data into a MongoDB.", inputs, null, null);
		Filter filter = new Filter("MongoDB Exporter", "Output", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

}
