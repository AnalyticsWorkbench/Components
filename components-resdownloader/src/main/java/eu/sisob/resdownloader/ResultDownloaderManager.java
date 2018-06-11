package eu.sisob.resdownloader;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.BooleanField;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.util.ConnectionType;
import org.json.simple.JSONObject;

public class ResultDownloaderManager extends AgentManager {
	
	public static final String PARAMETER_JSON = "tojson";

    public ResultDownloaderManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
        super(commandMsg, mngId, connectionType);
    }

    @Override
    protected void createAgent(JsonObject commandMsg) {
        ResultDownloaderAgent rda = new ResultDownloaderAgent(commandMsg);
        this.getAgents().add(rda);
        rda.setAgentListener(this);
        rda.initializeAgent();

        Thread runtime = new Thread(rda);
        runtime.start();

        System.out.println("Result Downloader Agent created");
    }


    @SuppressWarnings("unchecked")
    @Override
    protected List<Filter> getFilterDescriptions() {
        JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "any"));
        JSONArray fields = new JSONArray();
        fields.add(new BooleanField("Add .json file extension to internal formats.", PARAMETER_JSON, true, true));
        
        JSONObject form = this.readFormJSON("resultdownloader-form.json");
    	
    	Container container = new Container("Creates a link for retrieving data.", 
                "This filter creates a web-accesible "
                        + "folder containing individual links for all input data and "
                        + "a zip-file containing all input data at once.", 
                inputs, null, fields, form);
        
        Filter filter = new Filter("Result Downloader", "Output", container);
        List<Filter> filters = new ArrayList<Filter>(1);
        filters.add(filter);
        
        return filters;
    }

}
