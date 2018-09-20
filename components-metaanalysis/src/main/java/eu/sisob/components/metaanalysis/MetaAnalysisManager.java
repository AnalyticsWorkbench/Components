package eu.sisob.components.metaanalysis;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ConnectionType;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.BooleanField;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;

/**
 * This is the tutorial to create an agent manager. The manager
 * organizes and coordinates all his agents.
 *
 * @author remberg
 *
 * Like all other AgentManagers our TutorialManager extends the {@link AgentManager} class.
 *
 */
public class MetaAnalysisManager extends AgentManager {

	/**
	 * Component Name to be used in Starter and in the component description
	 */
	public static final String COMPONENT_NAME = "metaanalysis"; // FBA was public

	/**
	 * Constructor for the manager. First it calls the constructor of the superclass.
	 * @param coordinationMessageTemplate template to check if the incoming coordination message is for this manager
	 * @param managerName name of the manager
	 * @param connectionType the connection type, can be SINGLE (single connection for manager and agents), MANAGER (one
	 *                       connection for manager and agents) or AGENTBUNDLE (shared connection with other analysis components.
	 */

	public MetaAnalysisManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
		super(coordinationMessageTemplate, managerName, connectionType);
	}

	/**
	 * This method is called if a coordination message receives our manager
	 * @param coordinationMessage coordination message for our manager
	 */
	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		// now simplified:
		this.createAgent(new MetaAnalysisAgent(coordinationMessage));
		System.out.println("=============MetaAnalysis Agent Created================");

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
		String shortDescription = "Analyses inputs";

		// this is the detailed description ...
		String longDescription = "Analyses inputs";

		// here the input pipes are represented
		JSONArray inputs = new JSONArray();
		/*
		 *  1. Input. Parameters: (name of the input-pipe, data type to be accepted)
		 */
		inputs.add(new Input("in_1", "data"));

		// here the output pipes are represented
		JSONArray outputs = new JSONArray();

		// here the parameter files can be used
		JSONArray fields = new JSONArray();

		// here the create a container for the ui which contains the short as well as the long description
		// input and output pipes and our fields
		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
		/*
		 *  Here we define the filter
		 *  Parameters: 1. Filtername, 2. Filter Category, 3. out newly created container
		 */
		Filter filter = new Filter(COMPONENT_NAME, "Tools", container);

		// add the filter description to the returned filter list
		filters.add(filter);

		return filters;
	}

}
