package eu.sisob.components.kcorefilter;

import com.google.gson.JsonObject;
import info.collide.sqlspaces.commons.Tuple;
import info.collide.util.ClassLoaderUtility;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import rcaller.RCaller;
import rcaller.RCode;
import eu.sisob.components.framework.AgentProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.rwrapper.RWrapperAgent;
import java.io.IOException;
import rcaller.exception.RCallerExecutionException;

/**
 * 
 * @author hecking 
 */
public class KCoreAgent extends RWrapperAgent {

    private int k;
	private static final String SCRIPT = "extract_k_core.R";

    public KCoreAgent(JsonObject commandMsg) {
		super(commandMsg);

//		this.clustCount = this.getFilterParameters().get(BlockmodelManager.CLUSTER_COUNT).toString();
//            String[] params = this.getParamsList(commandTuple.getField(6).getValue().toString());
        
		this.k = Integer.parseInt(this.getFilterParameters().get(KCoreManager.K).toString());
		this.scriptStream = ClassLoaderUtility.getClassLoader().getResourceAsStream(SCRIPT);
    }

//    @Override
//    protected boolean executeRScript(String dataURL) {
//
//        boolean success = true;
//        if (this.k > 0) {
//            String resUrls = null;
//            
//            String suffix = "";
//            RCaller caller = new RCaller();
//            caller.setRscriptExecutable(RScriptExecutable);
//
//            RCode code = new RCode();
//            code.clear();
//        //Set R working directory
//            code.addRCode("setwd('" + this.workspaceUrl.replace(AgentProperties.SEPARATOR, "/") + "')");
//            
//            //execute multiple graph script
//            code.addRCode("library('igraph')");
//            File graphFolder = new File(this.workspaceUrl);
//            suffix = "";
//            String graphFile = "";
//            String label = "";
//
//            code.addRCode("graphs <- list()");
//            code.addRCode("labels <- c()");
//
//            for (int i = 0; i < this.filenames.size(); i = i + 1) {//int i = 0; i < graphFolder.list().length; i = i + 1) {
//                graphFile = this.filenames.get(i);
//                if (this.getType(graphFile).equals("gml")) {
//                    label = graphFile.substring(0, graphFile.indexOf(".gml"));
//                    code.addRCode("g <- read.graph('" + this.workspaceUrl.replace(AgentProperties.SEPARATOR, "/") + "/" + graphFile + "', 'gml')");
//                                //}
//                    code.addRCode("graphs[['" + label + "']] <- g");
//                    code.addRCode("labels[" + (i + 1) + "] <- '" + label + "'");
//                } else {
//                    
//                    this.indicateError(this.agentInstanceID + "Input should be in gml format.");
//                    return false;
//                }
//
//            }
//
//            code.addRCode("k <- " + this.k);
//            code.addRCode(this.readScript(ClassLoaderUtility.getClassLoader().getResourceAsStream("extract_k_core.R")));
//
//            //Execute Script
//            try {
//
//                caller.setRCode(code);
//                caller.runAndReturnResult("resultData");
//
//                resUrls = caller.getParser().getAsStringArray("dataUrl")[0];
//                this.resultFiles = this.retrieveResults(resUrls);
//
//		        } catch (RCallerExecutionException ex) {
//		                this.indicateError(agentName + ": Failure in R script!!!");
//		                success = false;
//		                logger.log(Level.SEVERE, agentName + ": Failure in R script!!!", ex);
//		        } catch (IOException ex) {
//		            success = false;
//		            logger.log(Level.SEVERE, agentName + ": no results!!!",ex);
//		        } catch (IllegalContentTypeException ex) {
//		        	success = false;
//		        	logger.log(Level.SEVERE, agentName + ": problems parsing!!!",ex);
//		        }
//        } else {
//            
//            this.indicateError("Please provide an integer > 0 as value for k.");
//            success = false;
//        }
//        return success;
//    }
    
    public void addScriptSpecificVariables(RCode code){
		code.addRCode("k <- " + this.k);
	}
}