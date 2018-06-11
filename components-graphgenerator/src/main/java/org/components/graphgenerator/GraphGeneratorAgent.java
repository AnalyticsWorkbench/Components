/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.components.graphgenerator;

import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.components.framework.AgentProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.util.AgentStatus;
import eu.sisob.components.rwrapper.RWrapperAgent;
import info.collide.sqlspaces.commons.Tuple;
import info.collide.util.ClassLoaderUtility;
import java.io.File;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import rcaller.RCaller;
import rcaller.RCode;
import rcaller.exception.RCallerExecutionException;

/**
 *
 * @author doberstein
 */
public class GraphGeneratorAgent extends RWrapperAgent {

	private static final String SCRIPT1 = "preferential_attachment.R";
	private static final String SCRIPT2 = "forest_fire.R";
	private static final String SCRIPT3 = "random_graph.R";

	private JsonObject commandMsg;
	private String method;
	private int n;
	private String directed;
	private String edgeProbability;
	private String loops;
	private String FWBurning;
	private String BWBurning;
	private String PAPower;
	private String EdgesAdded;

	public GraphGeneratorAgent(JsonObject commandMsg) {
		super(commandMsg);
		
		this.commandMsg = commandMsg;

		this.n = Integer.parseInt(this.getFilterParameters().get(GraphGeneratorManager.NODES).toString());
		this.method = this.getFilterParameters().get(GraphGeneratorManager.METHOD).toString();
		this.directed = this.getFilterParameters().get(GraphGeneratorManager.DIRECTED).toString();
		this.edgeProbability = this.getFilterParameters().get(GraphGeneratorManager.EDGE_PROB).toString();
		this.loops = this.getFilterParameters().get(GraphGeneratorManager.LOOPS).toString();
		this.FWBurning = this.getFilterParameters().get(GraphGeneratorManager.FWBURN).toString();
		this.BWBurning = this.getFilterParameters().get(GraphGeneratorManager.BWBURN).toString();
		this.PAPower = this.getFilterParameters().get(GraphGeneratorManager.PAPOWER).toString();
		this.EdgesAdded = this.getFilterParameters().get(GraphGeneratorManager.EDGES_ADDED).toString();
		

		
		if (this.method.equals("Random")) {
			this.method = SCRIPT3;
		} else if (this.method.equals("Preferential attachment")) {
			this.method = SCRIPT1;
		} else if (this.method.equals("Forest-fire")) {
			this.method = SCRIPT2;
		}

		this.scriptStream = ClassLoaderUtility.getClassLoader().getResourceAsStream(this.method);
	}

	public void addScriptVariablesForRandom (RCode code) {
		code.addRCode("edge_prob <- " + this.edgeProbability);
		code.addRCode("loops_bool <- " + this.loops);
	}
	public void addScriptVariablesForPrefAtt (RCode code) {
		code.addRCode("pa_power <- " + this.PAPower);
		code.addRCode("edges_added <- " + EdgesAdded);
	}
	public void addScriptVariablesForForestFire (RCode code) {
		code.addRCode("fwburn <- " + FWBurning);
		code.addRCode("bwburn <- " + BWBurning);
	}
	
        @Override
	public void addScriptSpecificVariables(RCode code){
            System.out.println("ADD SCRIPT SCPECIFIC VARIABLES");
		code.addRCode("n <- " + this.n);
		code.addRCode("dir <- " + this.directed);
		if (this.method.equals(SCRIPT1)) {
			// Preferential attachment
			addScriptVariablesForPrefAtt(code);
		} else if (this.method.equals(SCRIPT2)) {
			// Forest-fire
			addScriptVariablesForForestFire(code);
		} else if (this.method.equals(SCRIPT3)){
			// Random
			addScriptVariablesForRandom(code);
		}
	}
	
	public void execAgentManually() {

        setAgentWorkingStatus(AgentStatus.RUNNING);
        setAlive(true);
        getAgentListener().notifyManager(this);
        executeAgent(commandMsg);
    }
	
	@Override
    public void run() {
        execAgentManually();
        super.run();
    }
	
	@Override
	public Metadata mergeMetadata(String filename, Metadata newMetadata) {
		
		Metadata new_Metadata = new Metadata();
		
		if (this.directed.equals("TRUE"))  {
			new_Metadata.setDirectedNetwork("true");
		} else {
			new_Metadata.setDirectedNetwork("false");
		}
		return new_Metadata;
	}
	
	@Override
	protected void uploadResults() {
		logger.fine(agentName + ": uploading results");

		if (this.resultFiles.size() == 0) {

			// this.deleteWorkspace(new File(this.workspaceUrl));
			this.indicateError("No result data available.");
		} else {

			this.uploadData(this.resultFiles, this.rawOutlet);

//			if ((this.decoratedFiles == null) || (this.decoratedFiles.size() == 0)) {
//
//				this.uploadData((JSONArray) JSONValue.parse(this.input), this.decoratedOutlet);
//				this.uploadData((JSONArray) JSONValue.parse(this.workspaceUrl), this.decoratedOutlet);
//			} else {
//
//				this.uploadData(this.decoratedFiles, this.decoratedOutlet);
//			}
			this.deleteWorkspace(new File(this.workspaceUrl));
			logger.fine(agentName + ": kill agent");

			this.indicateDone();
		}

	}
	
	@Override
	public void executeAgent(JsonObject dataMessage) {
		logger.fine(agentName + ": agent");

		if (this.executeRScript(null)) {
			this.uploadResults();
		}
	}

}
