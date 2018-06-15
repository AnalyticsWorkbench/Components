/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.components.blockmodel;

import eu.sisob.components.framework.AgentProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import eu.sisob.components.rwrapper.RWrapperAgent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ClassLoaderUtility;

import rcaller.RCaller;
import rcaller.RCode;
import rcaller.exception.RCallerExecutionException;

/**
 * @author doberstein
 */
public class BlockmodelAgent extends RWrapperAgent {

	private static final String SCRIPT1 = "blockmodel.R";
	private String clustCount;
	//private String blockRelations;
	
	private boolean NULL = false;
	private boolean REG = false;
	private boolean COM = false;
	private boolean RDO = false;
	private boolean CDO = false;
	private boolean RRE = false;
	private boolean CRE = false;
	private boolean DNC = false;


	public BlockmodelAgent(JsonObject commandMsg) {
		super(commandMsg);
		
		this.clustCount = this.getFilterParameters().get(BlockmodelManager.CLUSTER_COUNT).toString();
		
		this.NULL = Boolean.parseBoolean(this.getFilterParameters().get(BlockmodelManager.NULL).toString());
		this.REG = Boolean.parseBoolean(this.getFilterParameters().get(BlockmodelManager.REG).toString());
		this.COM = Boolean.parseBoolean(this.getFilterParameters().get(BlockmodelManager.COM).toString());
		this.RDO = Boolean.parseBoolean(this.getFilterParameters().get(BlockmodelManager.RDO).toString());
		this.CDO = Boolean.parseBoolean(this.getFilterParameters().get(BlockmodelManager.CDO).toString());
		this.RRE = Boolean.parseBoolean(this.getFilterParameters().get(BlockmodelManager.RRE).toString());
		this.CRE = Boolean.parseBoolean(this.getFilterParameters().get(BlockmodelManager.CRE).toString());
		this.DNC = Boolean.parseBoolean(this.getFilterParameters().get(BlockmodelManager.DNC).toString());
		
		String selectedScript = SCRIPT1;
		
		//this.blockRelations = "\"null\",\"reg\", \"com\", \"rdo\", \"cdo\", \"rre\", \"cre\", \"rfn\", \"cfn\", \"dnc\"";

		//this.method = this.getFilterParameters().get(BlockmodelManager.METHOD).toString();

		
		if (selectedScript != null) {
			this.scriptStream = ClassLoaderUtility.getClassLoader().getResourceAsStream(selectedScript);
		} else {
			indicateError("could not find the selected script");
		}
		
	}
	
	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		
		Vector<JSONFile> files = new Vector<JSONFile>();
		logger.fine(agentName + ": agent");
		//this.input = new Gson().toJson(dataMessages.get(1).get("payload"));
		for (JsonObject dataMessage : dataMessages) {
			Vector<JSONFile> help = new Vector<JSONFile>();
			help = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
			
			files.add(help.get(0));
				//this.input = dataMessage.get("payload").toString();
				//files = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
				//files.add(dataMessage.get("payload"));
				
				//files.add
				//this.input = this.input + dataMessage.get("payload").toString();
			
		}
		this.input = files.toString();
		if (this.prepareAgentWorkspace(this.input)) {

			if (this.executeRScript(null)) {
				this.uploadResults();
			}
		}	
	}
	
	@Override
	protected boolean executeRScript(String dataUrl, boolean restoreOldIds) {

		String resDataUrls = null;
		String decDataUrls = null;
		String metadata = null;

		boolean success = true;
		String suffix = "";
		RCaller caller = new RCaller();
		caller.setRscriptExecutable(RScriptExecutable);

		RCode code = new RCode();
		code.clear();
		// Set R working directory

		code.addRCode("setwd('" + this.workspaceUrl.replace(AgentProperties.SEPARATOR, "/") + "')");
		String label = "";

		// execute multiple graph script
		code.addRCode("library('igraph')");
		// File folder = new File(this.workspaceUrl);
		suffix = "";
		String file = "";

		code.addRCode("graphs <- list()");
		code.addRCode("tables <- list()");
		code.addRCode("labels <- c()");

		for (int i = 0; i < this.filenames.size(); i = i + 1) {
			file = this.filenames.get(i);
			suffix = this.getType(file);

			label = file.substring(0, file.indexOf("." + suffix));

			code.addRCode("labels[" + (i + 1) + "] <- '" + label + "'");
			
			if (consistentFormat) {
				if (this.isGraphData) {
					code.addRCode("g <- read.graph('"
							+ (this.workspaceUrl + AgentProperties.SEPARATOR + file).replace(AgentProperties.SEPARATOR, "/")
							+ "', 'gml')");

					code.addRCode("graphs[['" + label + "']] <- g");

				} else {

					if (suffix.equals("csv")) {

						code.addRCode("data <- read.csv('" + (this.workspaceUrl + AgentProperties.SEPARATOR + file)
								.replace(AgentProperties.SEPARATOR, "/") + "', header=TRUE)");// "',
																								// row.names=1)");
						code.addRCode("tables[['" + label + "']] <- data");
					} else {

						code.addRCode("data <- read.table('" + (this.workspaceUrl + AgentProperties.SEPARATOR + file)
								.replace(AgentProperties.SEPARATOR, "/") + "')");
						code.addRCode("tables[['" + label + "']] <- data");
					}
				}
			} else {
				if (suffix.equals("gml")) {
					code.addRCode("g <- read.graph('"
							+ (this.workspaceUrl + AgentProperties.SEPARATOR + file).replace(AgentProperties.SEPARATOR, "/")
							+ "', 'gml')");

					code.addRCode("graphs[['" + label + "']] <- g");

				} else {

					if (suffix.equals("csv")) {

						code.addRCode("data <- read.csv('" + (this.workspaceUrl + AgentProperties.SEPARATOR + file)
								.replace(AgentProperties.SEPARATOR, "/") + "', header=TRUE)");// "',
																								// row.names=1)");
						code.addRCode("tables[['" + label + "']] <- data");
					} else {

						code.addRCode("data <- read.table('" + (this.workspaceUrl + AgentProperties.SEPARATOR + file)
								.replace(AgentProperties.SEPARATOR, "/") + "')");
						code.addRCode("tables[['" + label + "']] <- data");
					}
				}
			}

			
		}
		//Add extra variables for Single script Agents
		addScriptSpecificVariables(code);

		logger.fine(agentName + ": Read script...");
		code.addRCode(this.readScript(this.scriptStream));

		// Execute Script
		logger.fine(agentName + ": Execute script...");
		try {
			// System.out.println(code);
			caller.setRCode(code);
			caller.runAndReturnResult("resultData");

			resDataUrls = caller.getParser().getAsStringArray("dataUrl")[0];
			// System.out.println(this.resultDataUrl);
			decDataUrls = caller.getParser().getAsStringArray("decoratedUrl")[0];

			this.resultFiles = new JSONArray();
			this.decoratedFiles = new JSONArray();
			// if (resDataUrls != null) {
			// try {
			// this.resultFiles.addAll(this.retrieveResults(resDataUrls));
			// } catch (IllegalContentTypeException ex) {
			// this.indicateError("Cannot read results.", ex);
			// }
			// }
			
			// for first output
			if (resDataUrls != null) {

				try {
					metadata = caller.getParser().getAsStringArray("metadata")[0];
				} catch (Exception e) {
					System.out.println("no metadata.");
				}
				if (metadata != null) {

					// this.measureMetadata.addAll(this.extractMeasureMetadata(metadata));
					// this.propertyMetadata.addAll(this.extractPropertiesMetadata(metadata));
					// TODO this is only about node meta data, what about edge
					// meta data??
					this.nodePropertyMetadata.addAll(this.extractPropertiesMetadata(metadata));
				}
				try {
					this.resultFiles.addAll(this.retrieveResults(resDataUrls, restoreOldIds));
				} catch (IllegalContentTypeException ex) {
					success = false;
					this.indicateError("Cannot read results.", ex);
				}
			}
			
			// for second output
			if (decDataUrls != null) {

				try {
					metadata = caller.getParser().getAsStringArray("metadata")[0];
				} catch (Exception e) {
					System.out.println("no metadata.");
				}
				if (metadata != null) {

					// this.measureMetadata.addAll(this.extractMeasureMetadata(metadata));
					// this.propertyMetadata.addAll(this.extractPropertiesMetadata(metadata));
					// TODO this is only about node meta data, what about edge
					// meta data??
					this.nodePropertyMetadata.addAll(this.extractPropertiesMetadata(metadata));
				}
				try {
					this.decoratedFiles.addAll(this.retrieveResults(decDataUrls, restoreOldIds));
				} catch (IllegalContentTypeException ex) {
					success = false;
					this.indicateError("Cannot read results.", ex);
				}
			}

		} catch (RCallerExecutionException ex) {

			this.indicateError(agentName + ": Failure in R script!!!", ex);
			success = false;
		} catch (IOException ex) {

			this.indicateError(agentName + ": No results.", ex);
			success = false; // FIXME: Is this the right behavior
		}

		return success;
	}
	
	/*
	 * private boolean NULL = false;
	private boolean REG = false;
	private boolean COM = false;
	private boolean RDO = false;
	private boolean CDO = false;
	private boolean RRE = false;
	private boolean CRE = false;
	private boolean DNC = false;
	 */
	
	// generate the list of choosen block relations that will be used in the crit.fun function in the R script
	public String generateBlockRelations() {
		String out = "";
		
		ArrayList<String> relations = new ArrayList<String>();
		if(NULL){relations.add("\"null\"");}
		if(REG){relations.add("\"reg\"");}
		if(COM){relations.add("\"com\"");}
		if(RDO){relations.add("\"rdo\"");}
		if(CDO){relations.add("\"cdo\"");}
		if(RRE){relations.add("\"rre\"");}
		if(CRE){relations.add("\"cre\"");}
		if(DNC){relations.add("\"dnc\"");}
		
		for (int i = 0; i < relations.size()-1; i++) {
			out += relations.get(i) + ",";
		}
		out += relations.get(relations.size()-1);
		
		System.out.println("***\nString out: " + out);
		
		return out;
	}
	
	
	public void addScriptSpecificVariables(RCode code){
		code.addRCode("clustCount <- " + this.clustCount);
		code.addRCode("blockRelations <- c(" + generateBlockRelations() + ")");
	}

}
