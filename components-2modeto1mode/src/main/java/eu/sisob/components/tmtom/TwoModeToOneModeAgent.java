/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.components.tmtom;

import java.io.IOException;
import java.util.LinkedList;

import org.json.simple.JSONArray;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.rwrapper.RWrapperAgent;
import info.collide.util.ClassLoaderUtility;

/**
 *
 * @author hecking
 */
public class TwoModeToOneModeAgent extends RWrapperAgent {
 
    private static final String BY_ROW = "2ModeToOneModeA.R";
    private static final String BY_COLUMN = "2ModeToOneModeB.R";
    
    public TwoModeToOneModeAgent(JsonObject commandMsg) {
    	this(commandMsg, (new Gson().fromJson(commandMsg.get("parameters").getAsString(), JsonObject.class)).get("value1").getAsString(), new String[] {"1st mode", "2nd mode"});
    	
//        super(commandMsg);
//
//        String modeobject = commandMsg.get("parameters").getAsString();
//        JsonObject jsonObject = new Gson().fromJson(modeobject, JsonObject.class);
//        String mode = jsonObject.get("value1").getAsString();
//        
//        if  (mode.equals("1st mode")) {
//            
//            this.scriptStream = ClassLoaderUtility.getClassLoader()
//				.getResourceAsStream(BY_ROW);
//        } else {
//            
//            this.scriptStream = ClassLoaderUtility.getClassLoader()
//				.getResourceAsStream(BY_COLUMN);
//        }
    }
    
    protected TwoModeToOneModeAgent(JsonObject commandMsg, String parameter, String[] options) {
    	super(commandMsg);
    	
//    	String modeobject = commandMsg.get("parameters").getAsString();
//    	JsonObject jsonObject = new Gson().fromJson(modeobject, JsonObject.class);
//    	String mode = jsonObject.get("value1").getAsString();
    	
    	if  (options[0].equals(parameter)) {
    		
    		this.scriptStream = ClassLoaderUtility.getClassLoader()
    				.getResourceAsStream(BY_ROW);
    	} else if (options[1].equals(parameter)) {
    		
    		this.scriptStream = ClassLoaderUtility.getClassLoader()
    				.getResourceAsStream(BY_COLUMN);
    	} else {
    		String optionString = "[";
    		boolean first = true;
    		for (String o : options) {
    			if (!first) {
    				optionString += ",";
    			}
    			optionString += o;
    			if (first) {
    				first = false;
    			}
    		}
    		optionString += "]";
    		throw new IllegalArgumentException("could not handle parameter " + parameter + " with options " + optionString);
    	}
    }
    
    @Override
    public JSONArray retrieveResults(String urls, boolean restoreOldIds) throws IOException, IllegalContentTypeException {
    	// remove stored edge property information
    	this.edgePropertyMetadata = new LinkedList<Property>();
    	
    	// add default edge properties
    	edgePropertyMetadata.add(new Property("id", "string"));
    	edgePropertyMetadata.add(new Property("source", "string"));
    	edgePropertyMetadata.add(new Property("target", "string"));

    	// add long and detailed description for the property we create here 
    	edgePropertyMetadata.add(new Property("weight", "double", "Edge Weight", null, null));
    	
    	// call super method
    	return super.retrieveResults(urls, restoreOldIds);
    }
    
    
    
}
