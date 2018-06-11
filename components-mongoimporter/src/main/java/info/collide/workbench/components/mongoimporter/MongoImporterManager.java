package info.collide.workbench.components.mongoimporter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.BooleanField;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.IntField;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.util.ConnectionType;

/**
 * @author verheyen
 * 
 */
public class MongoImporterManager extends AgentManager {
	
	public static final String COMPONENT_NAME = "MongoImporter";

	public MongoImporterManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		createAgent(new MongoImporterAgent(coordinationMessage));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "Import a set of events from the KOLEGEA events mongoDB.";
		String longDescription = null;
		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "imported events"));
		JSONArray fields = new JSONArray();
		fields.add(new SelectField("From year:", "value1", true, getYearSelections()));
		fields.add(new SelectField("From month:", "value2", true, getMonthSelections()));
		fields.add(new SelectField("From day:", "value3", true, getDaySelections()));
		fields.add(new SelectField("From hour:", "value4", true, getHourSelections()));
		fields.add(new SelectField("From minute:", "value5", true, getMinuteSelections()));
		fields.add(new SelectField("From second:", "value6", true, getMinuteSelections()));
		fields.add(new SelectField("To year:", "value7", true, getYearSelections()));
		fields.add(new SelectField("To month:", "value8", true, getMonthSelections()));
		fields.add(new SelectField("To day:", "value9", true, getDaySelections()));
		fields.add(new SelectField("To hour:", "value10", true, getHourSelections()));
		fields.add(new SelectField("To minute:", "value11", true, getMinuteSelections()));
		fields.add(new SelectField("To second:", "value12", true, getMinuteSelections()));
		fields.add(new BooleanField("Last month:", "value13", true));
		fields.add(new IntField("Last X days:", "value14", false));
		fields.add(new BooleanField("Until now:", "value15", true));

		Container container = new Container(shortDescription, longDescription, null, outputs,
				fields);
		Filter filter = new Filter(COMPONENT_NAME, "Input", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

	private String[] getYearSelections() {
		int start = 2013;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int diff = year - start;
		String[] years = new String[diff + 1];
		for (int i = 0; i <= diff; i++) {
			years[i] = "" + (2013 + i);
		}
		return years;
	}

	private String[] getMonthSelections() {
		String[] months = new String[12];
		for (int i = 12; i > 0; i--) {
			months[i - 1] = "" + i;
		}
		return months;
	}

	private String[] getDaySelections() {
		String[] days = new String[31];
		for (int i = 31; i > 0; i--) {
			days[i - 1] = "" + i;
		}
		return days;
	}

	private String[] getHourSelections() {
		String[] hours = new String[24];
		for (int i = 24; i > 0; i--) {
			hours[i - 1] = "" + (i - 1);
		}
		return hours;
	}

	private String[] getMinuteSelections() {
		String[] minutes = new String[60];
		for (int i = 60; i > 0; i--) {
			minutes[i - 1] = "" + (i - 1);
		}
		return minutes;
	}

}
