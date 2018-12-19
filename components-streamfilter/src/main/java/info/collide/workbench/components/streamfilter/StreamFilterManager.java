/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.workbench.components.streamfilter;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ConnectionType;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;
/**
 *
 * @author walter
 */
public class StreamFilterManager extends AgentManager {

    public StreamFilterManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
        super(commandMsg, mngId, connectionType);
    }
    @Override
    protected void createAgent(JsonObject coordinationMessage) {
        StreamFilterAgent agent = new StreamFilterAgent(coordinationMessage);
        this.getAgents().add(agent);
        agent.setAgentListener(this);
        agent.initializeAgent();
        Thread runtime = new Thread(agent);
        runtime.start();
    }
    @Override
    protected List<Filter> getFilterDescriptions() {
        
        String shortDescription = "This filter returns a filtered activity stream.";
    	String longDescription = "The input needs to be an activity stream. You can apply multiple filters at once as well as define a date range.";
    	JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "activity stream"));
    	JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "activity stream"));
    	JSONArray fields = new JSONArray();            
    	fields.add(new SelectField("include/exclude", "value1", true, new String[]{"include", "exclude"}));
    	fields.add(new StringField("verb", "value2", false));
    	fields.add(new StringField("actor:id", "value3", false));
    	fields.add(new StringField("actor:role", "value4", false));
    	fields.add(new StringField("actor:qualification", "value5", false));
    	fields.add(new StringField("object:objectType", "value6", false));
    	fields.add(new StringField("target:objectType", "value7", false));
    	fields.add(new StringField("generator:id", "value8", false));
        JSONObject form = this.readFormJSON("streamfilter-form.json");
    	String jsUpdateForm = this.readFile("streamfilter-update-form.js");
    	String jsTransformMeta = this.readFile("streamfilter-transform-meta.js");
    	Container container = new Container(shortDescription, longDescription, inputs, outputs, fields, form, jsUpdateForm, jsTransformMeta);
    	Filter filter = new Filter("Stream Filter", "Tools", container);
        List<Filter> filters = new ArrayList<Filter>(1);
        filters.add(filter);
    	return filters;
    }
}
