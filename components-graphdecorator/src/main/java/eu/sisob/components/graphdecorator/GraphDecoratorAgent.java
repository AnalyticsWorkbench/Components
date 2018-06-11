package eu.sisob.components.graphdecorator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Iterator;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import eu.sisob.api.parser.sisob.SDTParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.datatable.Dataset;
import eu.sisob.api.visualization.format.datatable.fields.DataField;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.EdgeProperties;
import eu.sisob.api.visualization.format.metadata.fields.NodeProperties;
import eu.sisob.api.visualization.format.metadata.fields.Properties;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.graph.parser.FormatFactory;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphDecoratorAgent extends Agent {

	private String strategy;
	private Vector<JSONFile> datafiles;

	public GraphDecoratorAgent(JsonObject commandMsg) {
		super(commandMsg);
		this.strategy = this.getFilterParameters().get(GraphDecoratorManager.MERGE_STRATEGY).toString().split("-")[1].toUpperCase().trim();
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {

		datafiles = new Vector<JSONFile>();
		try {
			if (dataMessages.size() < 2) {
				super.indicateError("Unable to decorate graph because one of the required inputs is missing.");
			} else {

				JsonObject dataMessage1 = dataMessages.get(0);
				JsonObject dataMessage2 = dataMessages.get(1);

				Vector<JSONFile> dataFiles1;
				Vector<JSONFile> dataFiles2;

				dataFiles1 = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage1.get("payload")));
				dataFiles2 = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage2.get("payload")));

				SGFParser graphParser = new SGFParser();
				SDTParser dataTableParser = new SDTParser();
				String mergeStrategy = this.strategy;

				if (isInputAGraph(dataFiles1.get(0))) {
					graphParser.setNetwork(dataFiles1.get(0));
					dataTableParser.setTabledata(dataFiles2.get(0));

				} else {
					graphParser.setNetwork(dataFiles2.get(0));
					dataTableParser.setTabledata(dataFiles1.get(0));
				}

				JSONFile decoratedGraph = null;

				if (isAValidNodeDecorativeProcedure(mergeStrategy, dataTableParser)) {
					decoratedGraph = decorateNodeSet(graphParser, dataTableParser);
					this.datafiles.add(decoratedGraph);
					uploadResults();
					indicateDone();
				} else if (isAValidEdgeDecorativeProcedure(mergeStrategy, dataTableParser)) {
					decoratedGraph = decorateEdgeSet(graphParser, dataTableParser);
					this.datafiles.add(decoratedGraph);
					uploadResults();
					indicateDone();
				} else {
					throw new IllegalContentTypeException("Unable to decorate graph with the current input. Please check the decoration method in the filter.");
				}
			}
		} catch (Exception g) {
			g.printStackTrace();
			super.indicateError(g.getMessage());
		}
	}

	private boolean isAValidNodeDecorativeProcedure(String mergeStrategy, SDTParser dataTableParser) throws IllegalContentTypeException {
		if (mergeStrategy.equals(FormatFactory.NODE_TRANSFORMATION_STRATEGY)) {
			dataTableParser.parse();
			Dataset dataset = dataTableParser.getParsedDataSet();
			DataField datafield = dataset.getDataSet().get(0);
			if (datafield.getProperty("source") == null && datafield.getProperty("target") == null) {
				return true;
			}
		}
		return false;
	}

	private boolean isAValidEdgeDecorativeProcedure(String mergeStrategy, SDTParser dataTableParser) throws IllegalContentTypeException {
		if (mergeStrategy.equals(FormatFactory.EDGE_TRANSFORMATION_STRATEGY)) {
			dataTableParser.parse();
			Dataset dataset = dataTableParser.getParsedDataSet();
			DataField datafield = dataset.getDataSet().get(0);

			if (datafield.getProperty("source") != null && datafield.getProperty("target") != null) {
				return true;
			}

		}
		return false;
	}

	private JSONFile decorateNodeSet(SGFParser graphParser, SDTParser dataTableParser) throws IllegalContentTypeException {

		//extract the graph	
		graphParser.parse();
		Metadata graphMetadata = graphParser.getParsedMetadata();
		NodeSet graphNodeset = graphParser.getParsedNodeSet();
		EdgeSet graphEdgeset = graphParser.getParsedEdgeSet();

		//extract the data table data
		dataTableParser.parse();
		Dataset dataset = dataTableParser.getParsedDataSet();
		Metadata dataTableMetadata = validateMetaData(dataTableParser.getParsedMetadata(), dataset);

		//decorate graph metadata and add properties of the table as nodeproperties to the graph
		Properties dataTableProperties = dataTableMetadata.getProperties();
		NodeProperties nodeProperties = graphMetadata.getNodeproperties();

		if (nodeProperties == null) {
			nodeProperties = new NodeProperties();
		}

		for (int i = 0; i < dataTableProperties.size(); i++) {
			Property p = (Property) dataTableProperties.get(i);
			if (!nodeProperties.contains(p)) {
				nodeProperties.add(p);
			}
		}

		//decorate original node set
		for (int i = 0; i < dataset.size(); i++) {
			DataField field = (DataField) dataset.get(i);
			for (int k = 0; k < graphNodeset.size(); k++) {
				Node node = (Node) graphNodeset.get(k);
				if (node.getLabel().equalsIgnoreCase(field.getLabel())) {
					Iterator keyIterator = field.keySet().iterator();
					while (keyIterator.hasNext()) {
						String key = keyIterator.next().toString();
						String value = field.get(key).toString();
						if (!node.containsKey(key)) {
							node.addProperty(key, value);
						}
					}
				}
			}
		}

		String title = graphMetadata.getTitle();

		// update graph data
		graphMetadata.setNodeproperties(nodeProperties);
		graphParser.updateMetadata(graphMetadata);
		graphParser.updateNodeSet(graphNodeset);
		graphParser.updateEdgeSet(graphEdgeset);

		String data = graphParser.encode();
		JSONFile graph = new JSONFile(title, ".sgf", data, JSONFile.TEXT);

		return graph;

	}

	private Metadata validateMetaData(Metadata dataTableMetadata, Dataset dataset) {

		//validate the measures
		Properties properties = dataTableMetadata.getProperties();
		DataField datafield = (DataField) dataset.get(0);
		if (properties != null) {
			// check if the current measure is contained in the data field					
			for (int i = 0; i < properties.size(); i++) {
				Property property = (Property) properties.get(i);
				String key = property.getPropertyKey();
				if (!datafield.containsKey(key)) {
					properties.remove(i);
					i = i - 1;
				}
			}
			dataTableMetadata.setProperties(properties);
		}

		//validate the node properties
		NodeProperties nodeProperties = dataTableMetadata.getNodeproperties();
		if (nodeProperties != null) {
			for (int i = 0; i < nodeProperties.size(); i++) {
				Property nodeproperty = (Property) nodeProperties.get(i);
				String key = nodeproperty.getPropertyKey();
				if (!datafield.containsKey(key)) {
					nodeProperties.remove(i);
					i = i - 1;
				}
			}
			dataTableMetadata.setNodeproperties(nodeProperties);
		}

		//validate the edge properties
		EdgeProperties edgeProperties = dataTableMetadata.getEdgeproperties();
		if (edgeProperties != null) {
			for (int i = 0; i < edgeProperties.size(); i++) {
				Property edgeproperty = (Property) nodeProperties.get(i);
				String key = edgeproperty.getPropertyKey();
				if (!datafield.containsKey(key)) {
					edgeProperties.remove(i);
					i = i - 1;
				}
			}
			dataTableMetadata.setEdgeproperties(edgeProperties);
		}

		return dataTableMetadata;
	}

	private JSONFile decorateEdgeSet(SGFParser graphParser, SDTParser dataTableParser) throws IllegalContentTypeException {

		//extract the graph	
		graphParser.parse();
		Metadata graphMetadata = graphParser.getParsedMetadata();
		NodeSet graphNodeset = graphParser.getParsedNodeSet();
		EdgeSet graphEdgeset = graphParser.getParsedEdgeSet();

		//extract the data table data
		dataTableParser.parse();
		Dataset dataset = dataTableParser.getParsedDataSet();
		Metadata dataTableMetadata = validateMetaData(dataTableParser.getParsedMetadata(), dataset);

		//decorate graph metadata and add properties of the table as edgeproperties
		Properties dataTableProperties = dataTableMetadata.getProperties();
		EdgeProperties edgeProperties = graphMetadata.getEdgeproperties();

		if (edgeProperties == null) {
			edgeProperties = new EdgeProperties();
		}

		for (int i = 0; i < dataTableProperties.size(); i++) {
			Property p = (Property) dataTableProperties.get(i);
			if (!edgeProperties.contains(p)) {
				edgeProperties.add(p);
			}
		}

		//decorate original edgeset set
		for (int i = 0; i < dataset.size(); i++) {
			DataField field = (DataField) dataset.get(i);

			String dataFieldSource = field.getProperty("source").toString();
			String dataFieldTarget = field.getProperty("target").toString();

			for (int k = 0; k < graphEdgeset.size(); k++) {
				Edge edge = (Edge) graphEdgeset.get(k);

				if (edge.getSource().equalsIgnoreCase(dataFieldSource) && edge.getTarget().equalsIgnoreCase(dataFieldTarget)) {
					Iterator keyIterator = field.keySet().iterator();
					while (keyIterator.hasNext()) {
						String key = keyIterator.next().toString();
						String value = field.get(key).toString();
						if (!edge.containsKey(key)) {
							edge.addProperty(key, value);
						}
					}
				}
			}
		}

		String title = graphMetadata.getTitle();

		// update graph data
		graphMetadata.setEdgeproperties(edgeProperties);
		graphParser.updateMetadata(graphMetadata);
		graphParser.updateNodeSet(graphNodeset);
		graphParser.updateEdgeSet(graphEdgeset);

		String data = graphParser.encode();
		JSONFile graph = new JSONFile(title, ".sgf", data, JSONFile.TEXT);

		return graph;

	}

	public boolean isInputAGraph(JSONFile someData) {
		JSONObject something = null;
		try {
			something = (JSONObject) JSONValue.parse(someData.getStringContent());
		} catch (IllegalContentTypeException ex) {
			Logger.getLogger(GraphDecoratorAgent.class.getName()).log(Level.SEVERE, null, ex);
		}
		boolean format = something.get("data") instanceof JSONArray ? false : true;
		return format;
	}


	@SuppressWarnings("unchecked")
	@Override
	protected void uploadResults() {
		try {
			System.out.println("Uploading Results...");
			Thread.sleep(2000);

			JSONArray fileSet = new JSONArray();
			for (int i = 0; i < datafiles.size(); i++) {
				JSONFile file = datafiles.get(i);
				fileSet.add(file);
			}

			storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", fileSet.toJSONString());

		} catch (Exception e) {
			indicateError(e.getMessage());
			e.printStackTrace();
		}
		System.out.println("Task Complete!");
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
