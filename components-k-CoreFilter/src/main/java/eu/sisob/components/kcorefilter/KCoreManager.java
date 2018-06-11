package eu.sisob.components.kcorefilter;

import com.google.gson.JsonObject;
import info.collide.sqlspaces.commons.Tuple;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.FilterCategories;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.IntField;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.util.ConnectionType;

/**
 * 
 * @author hecking 
 */
public class KCoreManager extends AgentManager {
	
	public static final String COMPONENT_NAME = "k-core Filter";
	public static final String K = "k";

	public KCoreManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject commandMsg) {
		System.out.println("create KCore agent");
		KCoreAgent agent = new KCoreAgent(commandMsg);
		this.createAgent(agent);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {
		ArrayList<Filter> filterList = new ArrayList<Filter>(1);
		String shortDescription = "Filters the incoming data based on k-core values";
		String longDescription = "This filter calculates the k-core values of all nodes in the graph. All nodes with a k-core value below the given threshold will be removed. The resulting network will probably be smaller!";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "network data"));
    	JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "network data"));
    	JSONArray fields = new JSONArray();
    	fields.add(new IntField("k-value", K, true, 1));
    	Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
    	Filter filter = new Filter(COMPONENT_NAME, FilterCategories.FILTER, container);
    	filterList.add(filter);
		return filterList;
	}	
}
