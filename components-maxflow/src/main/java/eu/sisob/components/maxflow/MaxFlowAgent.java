package eu.sisob.components.maxflow;

import com.google.gson.JsonObject;
import info.collide.sqlspaces.commons.Tuple;
import info.collide.util.ClassLoaderUtility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import rcaller.RCaller;
import rcaller.RCode;
import rcaller.exception.RCallerExecutionException;

import eu.sisob.components.framework.AgentProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.rwrapper.RWrapperAgent;


/**
 * 
 * @author Laura Steinert 
 * based on the MpaAgent by Tobias Hecking
 * 
 */
public class MaxFlowAgent extends RWrapperAgent {

    private InputStream scriptStream;

    public MaxFlowAgent(JsonObject commandMsg) {
		super(commandMsg);

        // load the R script
        this.scriptStream = ClassLoaderUtility
                    .getClassLoader().getResourceAsStream("maxflow.R");
        System.out.println("agent created!!!");
    }

    
    @Override
    protected boolean executeRScript(String dataUrl) {
        String suffix = "";
        boolean success = true;
        RCaller caller = new RCaller();
        caller.setRscriptExecutable(this.RScriptExecutable);
        File graphFolder = new File(this.workspaceUrl);
        String graphFile = "";
        String label = "";
        String resUrls = null;
        String decUrls = null;

        RCode code = new RCode();
        code.clear();
        // Set R working directory
        code.addRCode("library('igraph')");
        code.addRCode("setwd('" + this.workspaceUrl.replace(AgentProperties.SEPARATOR, "/") + "')");

        code.addRCode("graphs <- list()");
        code.addRCode("labels <- c()");
        
        for (int i = 0; i < this.filenames.size(); i = i + 1) {

            graphFile = this.filenames.get(i);
            
            if (this.getType(graphFile).equals("gml")) {
                label = graphFile.substring(0, graphFile.indexOf(".gml"));
                code.addRCode("g <- read.graph('" + this.workspaceUrl.replace(AgentProperties.SEPARATOR, "/") + "/" + graphFile + "', 'gml')");

                code.addRCode("graphs[['" + label + "']] <- g");
                code.addRCode("labels[" + (i + 1) + "] <- '" + label + "'");
            } else {
                
                this.indicateError(this.agentInstanceID + "The input does not seem to be a graph.");
                
                return false;
            }
        }

        System.out.println("Read script...");
        code.addRCode(this.readScript(this.scriptStream));

         // Execute Script
        System.out.println("Execute script...");

        try {
            caller.setRCode(code);
            caller.runAndReturnResult("resultData");

            resUrls = caller.getParser().getAsStringArray(
                                "dataUrl")[0];

            decUrls = caller.getParser().getAsStringArray(
                                    "decoratedUrl")[0];

            if (resUrls != null) {
                try {
                    this.resultFiles = this.retrieveResults(resUrls);
                } catch (IllegalContentTypeException ex) {
                    this.indicateError("no results.", ex);
                }
            }
            if (decUrls != null) {
                try {
                    this.decoratedFiles = this.retrieveResults(decUrls);
                } catch (IllegalContentTypeException ex) {
                    this.indicateError("no results.", ex);
                }
            }

        } catch (RCallerExecutionException ex) {

            System.out.println(agentName + ": Failure in R script!!!");
            this.indicateError(agentName + ": Max flow analysis does not work with the given input."
                    + "Please check if the graphs have 'capacity' as an edge attribute.");
            success = false;
            Logger.getLogger(RWrapperAgent.class.getName()).log(Level.SEVERE,
                            null, ex.getMessage());
        } catch (IOException ex) {

            System.out.println(agentName + ": No results.");
            this.indicateError(agentName + ": No results.");
            Logger.getLogger(RWrapperAgent.class.getName()).log(Level.SEVERE,
                                null, ex.getMessage());
        }

        return success;
    }
}