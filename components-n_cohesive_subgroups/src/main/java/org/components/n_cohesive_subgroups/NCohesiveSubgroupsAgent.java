/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.components.n_cohesive_subgroups;

import eu.sisob.components.framework.AgentProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.rwrapper.RWrapperAgent;
import info.collide.sqlspaces.commons.Tuple;
import info.collide.util.ClassLoaderUtility;
import java.io.File;
import java.io.IOException;
import org.json.simple.JSONArray;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import rcaller.RCaller;
import rcaller.RCode;
import rcaller.exception.RCallerExecutionException;

/**
 *
 * @author hecking
 */
public class NCohesiveSubgroupsAgent extends RWrapperAgent {

	private static final String SCRIPT = "n_cliques.R";

	private String method;
	private int n;

	public NCohesiveSubgroupsAgent(JsonObject commandMsg) {
		super(commandMsg);

		this.n = Integer.parseInt(this.getFilterParameters().get(NCohesiveSubgroupManager.N).toString());

		this.method = this.getFilterParameters().get(NCohesiveSubgroupManager.METHOD).toString();

		this.scriptStream = ClassLoaderUtility.getClassLoader().getResourceAsStream(SCRIPT);
		System.out.println(scriptStream.toString());
	}


	
        @Override
	public void addScriptSpecificVariables(RCode code){
		code.addRCode("method <- \"" + this.method + "\"");
		code.addRCode("n <- " + this.n);
	}

}
