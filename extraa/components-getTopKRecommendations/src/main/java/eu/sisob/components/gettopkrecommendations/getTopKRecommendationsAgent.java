package eu.sisob.components.gettopkrecommendations;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.json.simple.JSONArray;

import rcaller.RCaller;
import rcaller.RCode;
import rcaller.exception.RCallerExecutionException;

import info.collide.util.ClassLoaderUtility;
import eu.sisob.api.parser.csv.CSVParser;
import eu.sisob.api.parser.gml.GMLNetworkParser;
import eu.sisob.api.parser.sisob.SDTParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.metadata.Metadata;

import eu.sisob.components.framework.AgentProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import eu.sisob.components.rwrapper.RWrapperAgent;

public class getTopKRecommendationsAgent extends RWrapperAgent {
	
	int k;
	String nameRecommendationFlag;
//	String weightName;

	public getTopKRecommendationsAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
		
		try {
			this.k = Integer.parseInt(this.getFilterParameters().get(getTopKRecommendationsManager.K).toString());
		} catch (NumberFormatException e) {
			this.indicateError("The given k is not a number!");
		}
		this.nameRecommendationFlag = this.getFilterParameters().get(getTopKRecommendationsManager.REC_ATTRIBUTE).toString();

		this.scriptStream = ClassLoaderUtility.getClassLoader()
				.getResourceAsStream("getTopKRecommendations.R");

		System.out.println("Get Top k Recommendations Agent created!!!");
	}

	@Override
	public void addScriptspecificVariables(RCode code) {
		
	code.addRCode("k <- " + this.k);	
	code.addRCode("nameRecommendationFlag <- '" + this.nameRecommendationFlag + "'");
	
	}



	/*
	// =========================================================================
	@Override
	protected boolean executeRScript(String dataUrl) {
		boolean success = true;
		System.out.println("Get Top K Recommendations");

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

				code.addRCode("k <- " + this.k);
				code.addRCode("nameRecommendationFlag <- '" + this.nameRecommendationFlag + "'");
//				code.addRCode("weightName <- '" + this.weightName + "'");

			} else {
				this.indicateError(this.agentID + ": "
						+ "The input does not seem to be a graph.");
			}
		}

		System.out.println("Read script...");
		this.scriptStream = ClassLoaderUtility.getClassLoader()
				.getResourceAsStream("getTopKRecommendations.R");
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
					this.indicateError(this.agentID + ": " + "no results."
							+ ex.getMessage());
				}
			}

		} catch (RCallerExecutionException ex) {
			System.out
					.println("Failure in R script!");
			this.indicateError(this.agentID
					+ ": "
					+ "Failure in R script!");
		} catch (IOException ex) {
			System.out.println("No results.");
			this.indicateError(this.agentID + ": " + "No results.");
		}

		return success;
	}
	*/

	/*
	// =========================================================================
	protected JSONArray retrieveResults(String urls) throws IOException,
	IllegalContentTypeException {

		StringTokenizer tokenizer;
		String filename;
		String suffix;
		JSONArray resFileSet = new JSONArray();
		GMLNetworkParser graphParser;
		SGFParser outputParser;

		Vector<JSONFile> graphs;

		resFileSet = new JSONArray();
		graphs = new Vector<JSONFile>();

		tokenizer = new StringTokenizer(urls, ",");

		while (tokenizer.hasMoreTokens()) {
			filename = tokenizer.nextToken();
			suffix = this.getType(filename);

			String filenameWithPath = this.workspaceUrl
					+ AgentProperties.SEPARATOR + filename;

			if (suffix.equals("gml")) {
				graphs.add(new JSONFile(new File(filenameWithPath), true));
			}
			else if (suffix.equals("txt")) {
				BufferedReader br = new BufferedReader(new FileReader(filenameWithPath));
				String data = "";
				try {
				    StringBuilder sb = new StringBuilder();
				    String line = br.readLine();

				    while (line != null) {
				        sb.append(line);
				        sb.append(System.lineSeparator());
				        line = br.readLine();
				    }
				    data = sb.toString();
				} finally {
				    br.close();
				}

				System.out.println("Data: " + data);

				resFileSet.add(new JSONFile(filename, ".txt", data, JSONFile.TEXT));
			}
		}

		if (graphs.size() > 0) {
			graphParser = new GMLNetworkParser();
			outputParser = new SGFParser();
			for (JSONFile graph : graphs) {

				graphParser.setNetwork(graph);
				graphParser.parse();

				if (graphParser.getParsedMetadata() == null) {
					graphParser.updateMetadata(new Metadata());
				}
				if (this.measureMetadata != null) {

					if (graphParser.getParsedMetadata().getNetworkMeasures() == null) {

						graphParser.getParsedMetadata().setNetworkMeasures(
								new Measures());
					}
					for (Measure m : this.measureMetadata) {

						graphParser.getParsedMetadata().getNetworkMeasures()
								.addMeasure(m);
					}
				}

				if (this.networkType != null) {
					graphParser.getParsedMetadata().setNetworkType(
							this.networkType);
				}

				outputParser.updateMetadata(graphParser.getParsedMetadata());
				outputParser.updateNodeSet(graphParser.getParsedNodeSet());
				outputParser.updateEdgeSet(graphParser.getParsedEdgeSet());

				String newFileName = graph.getFileName().substring(0,
						graph.getFileName().lastIndexOf("."));
				resFileSet.add(new JSONFile((newFileName + outputParser
						.requestFileTypeExtension()), outputParser
						.requestFileType(), outputParser.encode(),
						JSONFile.TEXT));
			}

		}

		return resFileSet;
	}
*/
	
	// =========================================================================
	protected String getType(String dataUrl) {
		String type = "";
		if (dataUrl.endsWith(".gml")) {
			type = "gml";
		} else if (dataUrl.endsWith(".txt")) {
			type = "txt";
		}

		return type;
	}
}
