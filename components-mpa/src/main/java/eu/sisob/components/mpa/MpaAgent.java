package eu.sisob.components.mpa;

import com.github.rcaller.exception.ExecutionException;
import com.github.rcaller.rStuff.RCaller;
import com.github.rcaller.rStuff.RCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import eu.sisob.components.framework.AgentProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.rwrapper.RWrapperAgent;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ClassLoaderUtility;

public class MpaAgent extends RWrapperAgent {

    private Map<String, String> methodMapping;

    private String method;
    private String mpdetection;
    private double threshold;
    private String nameOfWeight;
    private InputStream scriptStream1;
    private InputStream scriptStream2;
    
    private static final String SCRIPT1 = "edge_weightening_schemes.R";
    private static final String SCRIPT2 = "mpa.R";
    
    

    public MpaAgent(JsonObject coordinationMessage) {
        super(coordinationMessage);

        methodMapping = new HashMap<String, String>();
        methodMapping.put("SPC", "spc");
        methodMapping.put("SPC with existing weights - pre", "spcpre");
        methodMapping.put("SPC with existing weights - post", "spcpost");
        methodMapping.put("SPC (norm.) with existing weights - post", "spcnormpost");        
        methodMapping.put("Existing weights only", "existingWeights");
        methodMapping.put("SPLC", "splc");
        methodMapping.put("SPNP", "spnp");
        methodMapping.put("NPCC", "npcc");
        methodMapping.put("Edge Betweenness Centrality",
                        "edgeBetweennessCentrality");
        

        this.method = (String) getFilterParameters().get(MpaManager.METHOD_VALUE);
        
        this.nameOfWeight = (String) getFilterParameters().get(MpaManager.WEIGHT_VALUE);
        
        this.threshold =  Double.parseDouble((String) getFilterParameters().get(MpaManager.THRESHOLD_VALUE));

        this.scriptStream1 = ClassLoaderUtility
                    .getClassLoader().getResourceAsStream(SCRIPT1);
        this.scriptStream2 = ClassLoaderUtility
                    .getClassLoader().getResourceAsStream(SCRIPT2);
        
        
        this.mpdetection = (String) getFilterParameters().get(MpaManager.RANGE_VALUE);
        
        System.out.println("agent created!!!");
    }

    
    @Override
    protected boolean executeRScript(String dataUrl) {

        boolean success = true;
        RCaller caller = new RCaller();
        caller.setRscriptExecutable(this.RScriptExecutable);

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
            System.out.println("filename: " + graphFile);
            
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
        code.addRCode(this.readScript(this.scriptStream1));

        // set edge weightening scheme
       	code.addRCode("ews <- " + this.methodMapping.get(this.method));
        
        code.addRCode("threshold <- " + this.threshold);

        code.addRCode("nameofweight <- " + "\"" + this.nameOfWeight + "\"");

        code.addRCode("mpdetection <- " + "\"" + this.mpdetection + "\"");

        code.addRCode(this.readScript(this.scriptStream2));

        // Execute Script
        System.out.println("Execute mpa script...");

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
            
        } catch (ExecutionException ex) {

            System.out.println(agentName + ": Failure in R script!!!");
            this.indicateError(agentName + ": Main path analysis does not work with the given input. "
                    + "Please check graphs if the graphs are directed and acyclic. Furthermore node " +
                    "and edge properties must consist of a single word without white spaces!");
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
    
    public void testAgent(String workspace) {
        
        System.out.println("begin test.");
        this.workspaceUrl = workspace;
        this.executeRScript(workspace);
        System.out.println("end test.");
    }
}