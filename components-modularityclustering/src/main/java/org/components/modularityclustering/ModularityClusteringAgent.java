package org.components.modularityclustering;

import com.github.rcaller.rStuff.RCode;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ClassLoaderUtility;

import eu.sisob.components.rwrapper.RWrapperAgent;

/**
 * 
 * @author Laura Steinert 
 * based on the MpaAgent by Tobias Hecking
 * 
 */
public class ModularityClusteringAgent extends RWrapperAgent {

	private static final String SCRIPT = "modularity_clustering.R";
	private String weightName;
	boolean useWeights;




	
    public ModularityClusteringAgent(JsonObject commandMsg){
    	super(commandMsg);
    	
		
		this.scriptStream = ClassLoaderUtility.getClassLoader().getResourceAsStream(SCRIPT);

    	
    	// retrieve and parse the filter-parameters for the agent
    	this.useWeights = Boolean.valueOf(this.getFilterParameters().get(ModularityClusteringManager.USEEDGEWEIGHT).toString());
    	this.weightName = this.getFilterParameters().get(ModularityClusteringManager.NAMEOFEDGEWEIGHT).toString();
        
        System.out.println("ModularityClustering agent created!!!");
        System.out.println(this.input);
    }
    
        @Override
	public void addScriptSpecificVariables(RCode code){
		if (useWeights) {
			code.addRCode("weightName <- " + "\"" + this.weightName + "\"");
		} else {
			code.addRCode("weightName <- " + "\"\"");
		}
	}

    
    
    
    



}