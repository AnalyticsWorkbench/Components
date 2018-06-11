/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.components.datatablemerge;


import com.google.gson.JsonObject;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.util.ConnectionType;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;



/**
 * 
 * @author walter
 */
public class DataTableMergeManager extends AgentManager {
	
	public static final String COMPONENT_NAME = "Data Table Merge";
	public static final String FIELD_ID = "field identifier";

	public DataTableMergeManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		this.createAgent(new DataTableMergeAgent(coordinationMessage));
	}

	@Override
	protected List<Filter> getFilterDescriptions() {

		String shortDescription = "This filter merges two SISOB Data Tables.";
		String longDescription = "This filter merges two SISOB Data Tables.";

		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "SISOB Data Table"));
		inputs.add(new Input("in_2", "SISOB Data Table"));

		JSONArray outputs = new JSONArray();
		outputs.add(new Output("out_1", "SISOB Data Table"));

		JSONArray fields = new JSONArray();

		String[] fieldIdentifier = { "id", "label", };
		fields.add(new SelectField("fieldIdentifier", FIELD_ID, true,
				fieldIdentifier));

		Container container = new Container(shortDescription, longDescription,
				inputs, outputs, fields);

		Filter filter = new Filter(COMPONENT_NAME, "Tools", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);

		return filters;
	}

}
