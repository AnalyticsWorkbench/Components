/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.components.tmtom;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.util.ConnectionType;

/**
 *
 * @author hecking
 */
public class TwoModeToOneModeManager extends AgentManager {

    public TwoModeToOneModeManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
        super(commandMsg, mngId, connectionType);
    }

    @Override
    protected void createAgent(JsonObject commandMsg) {
    	createAgent(new TwoModeToOneModeAgent(commandMsg));
    }
    
    protected List<Filter> getFilterDescriptions() {
        
        ArrayList<Filter> filterList = new ArrayList<Filter>(1);
        String shortDescription = "This filter converts bipartite networks into unipartite networks.";
        String longDescription = "If the input is not recogisable as 2-mode network, the output is the same as the input";
        JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "network data"));
    	JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "network data"));
    	JSONArray fields = new JSONArray();
    	fields.add(new SelectField("script", "value1", true, new String[] {"1st mode", "2nd mode"}));
    	Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
    	Filter filter = new Filter("2-Mode to 1-Mode", "Tools", container);
    	filterList.add(filter);
		return filterList;
	}
}
