package eu.sisob.components.findnodesubset;

import com.google.gson.JsonObject;

import info.collide.util.ClassLoaderUtility;

import java.io.IOException;
import java.util.ArrayList;

import rcaller.RCaller;
import rcaller.RCode;
import rcaller.exception.RCallerExecutionException;

import eu.sisob.components.framework.AgentProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.rwrapper.RWrapperAgent;

public class FindNodeSubsetAgent extends RWrapperAgent {
	
	protected String rawOutlet = ".out_1";
	protected String decoratedOutlet = ".out_2";
    private ComponentSelector componentSelector;
    private ThetaSelector thetaSelector;
    private String edgeWeightName;
    private int minSize;
    private int numberRec;    
	
    public enum ComponentSelector {
    	NumberOfNodes,
    	SumOfEdgeWeights
    }

    public enum ThetaSelector {
    	Intersection,
    	SeedIsLost,
    	FixedNumber
    }
  
    
	// =========================================================================
	public FindNodeSubsetAgent(JsonObject commandMsg) {
		super(commandMsg);
		
		parseParameters();


        this.scriptStream = ClassLoaderUtility
                .getClassLoader().getResourceAsStream("findNodeSubset.R");
        
        System.out.println("Find Node Subset Agent created!!!");      
	}
	
	protected void parseParameters() {
		String componentSelectorString = this.getFilterParameters().get(FindNodeSubsetManager.LARGEST_DETERMINER).toString();
		if(componentSelectorString.equals("Number of Nodes")){
			componentSelector = ComponentSelector.NumberOfNodes;
		}
		else if(componentSelectorString.equals("Sum of Edge Weights")){
			componentSelector = ComponentSelector.SumOfEdgeWeights;
		}
		else {
			this.indicateError("Unknown component selector: " + componentSelectorString);
		}
		
		String thetaSelectorString = this.getFilterParameters().get(FindNodeSubsetManager.THRESHOLD).toString();
		if(thetaSelectorString.equals("Intersection of avg. edge weight and sum of weights")){
			thetaSelector = ThetaSelector.Intersection;
		}
		else if(thetaSelectorString.equals("Just before seed node is lost")){
			thetaSelector = ThetaSelector.SeedIsLost;
		}
		else if(thetaSelectorString.equals("Given number of recommendations")){
			thetaSelector = ThetaSelector.FixedNumber;
		}
		else {
			this.indicateError("Unknown theta selector: " + thetaSelectorString);
		}
		
		edgeWeightName = this.getFilterParameters().get(FindNodeSubsetManager.EDGE_WEIGHT_NAME).toString();
		
		try {
			minSize = Integer.parseInt(this.getFilterParameters().get(FindNodeSubsetManager.MIN_SIZE).toString());
		}
		catch(NumberFormatException e){
			this.indicateError("The given minSize is not a number!");
		}
		
		try {
			numberRec = Integer.parseInt(this.getFilterParameters().get(FindNodeSubsetManager.IDEAL_NUM).toString());
		}
		catch(NumberFormatException e){
			this.indicateError("The given numberRec is not a number!");
		}
	}

	
	// =========================================================================
   @Override
    protected boolean executeRScript(String dataUrl) {
		boolean success = true;
		System.out.println("Find Node Subset");

		RCaller caller = new RCaller();
		caller.setRscriptExecutable(RScriptExecutable);
		caller.redirectROutputToConsole();
		String graphFile = "";
		String label = "";
		String resUrls = null;
		String decUrls = null;

		RCode code = new RCode();
		code.clear();
		// Set R working directory
		code.addRCode("library('igraph')");
		code.addRCode("setwd('"
				+ workspaceUrl.replace(AgentProperties.SEPARATOR, "/") + "')");
		// the following line doesn't work
		// code.addRCode("options(scipen=5)"); // forces R to not use scientific
		// notation for floating points because it confuses the GML parser
		code.addRCode("graphs <- list()");
		code.addRCode("labels <- c()");

		for (int i = 0; i < filenames.size(); i = i + 1) {

			graphFile = filenames.get(i);
			System.out.println("filename: " + graphFile);

			if (this.getType(graphFile).equals("gml")) {
				label = graphFile.substring(0, graphFile.indexOf(".gml"));
				code.addRCode("g <- read.graph('"
						+ this.workspaceUrl.replace(AgentProperties.SEPARATOR,
								"/") + "/" + graphFile + "', 'gml')");

				code.addRCode("graphs[['" + label + "']] <- g");
				code.addRCode("labels[" + (i + 1) + "] <- '" + label + "'");

				code.addRCode("componentSelector <- '"
						+ componentSelector.name() + "'");
				code.addRCode("thetaSelector <- '" + thetaSelector.name() + "'");
				code.addRCode("edgeWeightName <- '" + edgeWeightName + "'");
				code.addRCode("minSize <- " + minSize);
				code.addRCode("numberRec <- " + numberRec);
			} else {
				this.indicateError(this.agentID + ": " + "The input does not seem to be a graph.");
			}
		}

		System.out.println("Read script...");
        this.scriptStream = ClassLoaderUtility
                .getClassLoader().getResourceAsStream("findNodeSubset.R");
		code.addRCode(this.readScript(this.scriptStream));

		// Execute Script
		System.out.println("Execute script...");

		try {
			caller.setRCode(code);
			caller.runAndReturnResult("resultData");

			resUrls = caller.getParser().getAsStringArray("dataUrl")[0];

			decUrls = caller.getParser().getAsStringArray("decoratedUrl")[0];

			System.out.println("results: " + resUrls);
			System.out.println("decorated input: " + decUrls);

			if (resUrls != null) {
				try {
					this.resultFiles = this.retrieveResults(resUrls);					
				} catch (IllegalContentTypeException ex) {
					this.indicateError("no results." + ex.getMessage());
				}
			}
			if (decUrls != null) {
				try {
					this.decoratedFiles = this.retrieveResults(decUrls);
				} catch (IllegalContentTypeException ex) {
					this.indicateError(this.agentID + ": " + "no results." + ex.getMessage());
				}
			}

		} catch (RCallerExecutionException ex) {
			System.out
					.println("Failure in R script! Please verify the name of the edge weight. Also make sure that a node attribute 'seed' exists.");
			this.indicateError(this.agentID + ": " + "Failure in R script! Please verify the name of the edge weight. Also make sure that a node attribute 'seed' exists.");
		} catch (IOException ex) {
			System.out.println("No results.");
			this.indicateError(this.agentID + ": " + "No results.");
		}

		return success;
    }

}
