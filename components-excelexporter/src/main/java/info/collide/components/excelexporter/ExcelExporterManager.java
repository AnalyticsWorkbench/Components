package info.collide.components.excelexporter;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.util.ConnectionType;

public class ExcelExporterManager extends AgentManager {

	/**
	 * Component Name to be used in Starter and in the component description
	 */
	public static final String COMPONENT_NAME = "Excel Exporter";
	
	/**
	 * Constructor for the manager. First it calls the constructor of the superclass.
	 * @param coordinationMessageTemplate template to check if the incoming coordination message is for this manager
	 * @param managerName name of the manager
	 * @param connectionType the connection type, can be SINGLE (single connection for manager and agents), MANAGER (one
	 *                       connection for manager and agents) or AGENTBUNDLE (shared connection with other analysis components.
     */

	public ExcelExporterManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	/**
	 * This method is called if a coordination message receives our manager
	 * @param coordinationMessage coordination message for our manager
     */
	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		
		// now simplified:
		this.createAgent(new ExcelExporterAgent(coordinationMessage));

	}

	/**
	 * This is for the analysis component ui in the web workbench
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		
		ArrayList<Filter> filters = new ArrayList<>();
		
		/*
		 *  Description to be displayed as short description in the ui
		 */
		String shortDescription = "This filter converts SiSOB Data Table to Excel";
		
		// this is the detailed description ...
		String longDescription = null;
		
		// here the input pipes are represented
		JSONArray inputs = new JSONArray();
		
		/*
		 *  1. Input. Parameters: (name of the input-pipe, data type to be accepted)
		 */
		inputs.add(new Input("in_1", "data"));
		
		// here the output pipes are represented
		JSONArray outputs = new JSONArray();
		/**
		 * 1. Ouput. Parameters(name of the output-pipe, data type)
		 */
		outputs.add(new Output("out_1", "data"));
		
		// here the parameter files can be used
		JSONArray fields = null;

		// here the create a container for the ui which contains the short as well as the long description
		// input and output pipes and our fields
		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		/*
		 *  Here we define the filter
		 *  Parameters: 1. Filtername, 2. Filter Category, 3. out newly created container
		 */
     	Filter filter = new Filter(COMPONENT_NAME, "Data Converters", container);
     	
     	// add the filter description to the returned filter list
     	filters.add(filter);
		
		return filters;
	}

}
