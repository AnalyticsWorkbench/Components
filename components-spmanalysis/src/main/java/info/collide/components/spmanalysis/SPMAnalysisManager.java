package info.collide.components.spmanalysis;

import java.lang.ClassLoader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import eu.sisob.components.framework.util.ConnectionType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.BooleanField;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.componentdescription.StringField;

public class SPMAnalysisManager extends AgentManager {

	public SPMAnalysisManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
            this.createAgent(new SPMAnalysisAgent(coordinationMessage));
	}

	@Override
	protected List<Filter> getFilterDescriptions() {
		String shortDescription = "This filter performs sequential pattern mining on activity streams";
		String longDescription = "The input needs to be an activity stream. This filter performs a sequential pattern mining analysis with the CM-SPAM algorithm."
				+ "You can chose in which format the activities are mapped to strings, what the minimum support should be and if activities in the sequence have"
				+ "to be continuous are gaps are allowed.";
		
		JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "log file"));
    	
        JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "data table"));

    	JSONArray fields = new JSONArray();
    	fields.add(new SelectField("Input for analysis", "input_encoding", true, new String[]{"\u003C verb \u003E _ \u003C object:objectType \u003E", "\u003C verb \u003E _ \u003C object:objectType \u003E _ \u003C object:name \u003E"}));
    	fields.add(new StringField("Select min_sup (relative to the number of input sequences) ", "support", true, "0.2"));
    	fields.add(new BooleanField("Only allow continuous sequences?", "continuous", true, false));
    	fields.add(new SelectField("Select output type", "output", true, "SiSOB table", "Excel file"));
    	
    	JSONObject form = this.readFormJSON("spmanalysis-form.json");
    	String jsUpdateForm = this.readFile("spmanalysis-update-form.js");
    	String jsTransformMeta = this.readFile("spmanalysis-transform-meta.js");
    	
    	Container container = new Container(shortDescription, longDescription, inputs, outputs, fields, form, jsUpdateForm, jsTransformMeta);

 	    Filter filter = new Filter("Sequential Pattern Mining", "Analysis", container);
  		List<Filter> filters = new ArrayList<Filter>(1);
  		filters.add(filter);
  		return filters;
	}

}
