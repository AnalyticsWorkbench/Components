package eu.sisob.components.datauploader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ConnectionType;
import org.json.simple.JSONArray;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;

/**
 * @author  Alfredo Ramos, Per Verheyen
 */

public class DataUploaderManager extends AgentManager {
	
	public static final String FILE_PARAMETER = "selected_file";

	public DataUploaderManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
		super(commandMsg, mngId, connectionType);
	}

	@Override
	protected void createAgent(JsonObject coordinationMessage) {
		DataUploaderAgent fileLoaderAgent = new DataUploaderAgent(coordinationMessage);
		this.getAgents().add(fileLoaderAgent);
		fileLoaderAgent.setAgentListener(this);
		fileLoaderAgent.initializeAgent();
		Thread runtime = new Thread(fileLoaderAgent);
		runtime.start();

	}

	@SuppressWarnings("unchecked")
	@Override
    protected List<Filter> getFilterDescriptions() {
    	String shortDescription = "This filter reads data from a repository on the server. Please use the Format Transformation Filter for converting in the SISOB graph format.";
    	String longDescription = "This filter reads data from a repository on the server and feeds it into the workflow. The data format depends on the selected data set.";
    	JSONArray inputs = null;
    	JSONArray outputs = new JSONArray();
    	outputs.add(new Output("out_1", "uploaded data"));
    	
    	JSONArray fields = new JSONArray();
    	fields.add(getFileSelections());
    	
    	Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
    	
    	Filter filter = new Filter("Data Repository", "Input", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
    	return filters;
    }
    
    protected SelectField getFileSelections() {
    	
    	String fileLocation = System.getProperty("user.dir");
    	String tmp = SISOBProperties.getProperty("upload.directory");
    	if (tmp != null && !tmp.trim().isEmpty()) {
    		fileLocation = tmp;
    	}

    	// FIXME
    	LinkedList<String> fileList = new LinkedList<String>();
    	File fileDirectory = new File(fileLocation);
    	if (fileDirectory.exists() && fileDirectory.isDirectory()) {
    		for (File file : fileDirectory.listFiles()) {
    			if (!file.isHidden()) {
    				fileList.add(file.getName());
    			}
    		}
    	} else {
    		logger.warning("Property 'upload.directory' is set but seems to be wrong!");
    	}
    	
    	Collections.sort(fileList);
    	
    	SelectField selectField = new SelectField("File", FILE_PARAMETER, true, fileList.toArray(new String[fileList.size()]));
    	return selectField;
    }

}
