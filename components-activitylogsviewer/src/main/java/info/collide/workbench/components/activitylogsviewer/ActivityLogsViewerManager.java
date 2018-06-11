package info.collide.workbench.components.activitylogsviewer;

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
 * 
 * @author Emmanuel Meinike
 *
 */
public class ActivityLogsViewerManager extends AgentManager {
	
	public static final String COMPONENT_NAME = "Activity Logs Viewer";

	public ActivityLogsViewerManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		this.createAgent(new ActivityLogsViewerAgent(coordinationMessage));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "any"));
		
		String shortDescription = "This filter creates a web-view for Activity Logs.";
		String longDescription = "This filter creates a web-view for Activity Logs.";
		Container container = new Container(shortDescription, longDescription, inputs, null, null);
		Filter filter = new Filter(COMPONENT_NAME, "Output", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
    	return filters;	}

}
