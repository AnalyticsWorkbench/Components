package info.collide.workbench.components.logstatisticsovertime;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.util.ConnectionType;

public class LogStatisticsOverTimeManager extends AgentManager {

	public LogStatisticsOverTimeManager(JsonObject coordinationMessageTemplate, String managerName,
			ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		/* Creation of a new agent */
		Agent agent = new LogStatisticsOverTimeAgent(coordinationMessage);
		getAgents().add(agent);
		/* Adding the listeners */
		agent.setAgentListener(this);
		/* Initializing the new agent */
		agent.initializeAgent();
		/* Creating the agent runtime */
		Thread runTime = new Thread(agent);
		/* Executing the agent */
		runTime.start();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "This filter creates time based statistics.";
		String longDescription = "This filter creates statistics per given time interval.";

		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "log file"));

		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "data table"));

		JSONArray fields = new JSONArray();
		fields.add(new StringField("verb", "value1", false));
		fields.add(new StringField("object objectType", "value2", false));
		fields.add(new StringField("target objectType", "value3", false));

		String[] groupBy = { "no grouping", "actor", "verb", "object", "target", "generator" };
		fields.add(new SelectField("group by", "value4", true, groupBy));
		fields.add(new StringField("group by property", "value5", false));
		String[] timeUnit = { "day", "week", "month" };
		fields.add(new SelectField("time interval", "value6", true, timeUnit));

		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

		Filter filter = new Filter("Log Statistics Over Time", "Analysis", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

}
