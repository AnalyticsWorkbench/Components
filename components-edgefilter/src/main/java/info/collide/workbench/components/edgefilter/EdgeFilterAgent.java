package info.collide.workbench.components.edgefilter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

public class EdgeFilterAgent extends Agent {

	private List<JSONFile> results;
	
	private String sourceAttribute;
	private String sourceValue;
	
	private String targetAttribute;
	private String targetValue;
	private String operator;

	public EdgeFilterAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);

		results = new LinkedList<JSONFile>();
		
		sourceAttribute = getFilterParameters().get(EdgeFilterManager.PARAMETER_SOURCE_ATTRIBUTE).toString();
		sourceValue = getFilterParameters().get(EdgeFilterManager.PARAMETER_SOURCE_VALUE).toString();
		
		targetAttribute = getFilterParameters().get(EdgeFilterManager.PARAMETER_TARGET_ATTRIBUTE).toString();
		targetValue = getFilterParameters().get(EdgeFilterManager.PARAMETER_TARGET_VALUE).toString();
		operator = getFilterParameters().get(EdgeFilterManager.PARAMETER_OPERATOR).toString();
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		Vector<JSONFile> files  = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
		try {
			for (JSONFile file : files) {
				results.add(filterNet(file));
			}
			this.uploadResults();
			this.indicateDone();
		} catch (IllegalContentTypeException ex) {
			Logger.getLogger(EdgeFilterAgent.class.getName()).log(Level.SEVERE, null, ex);
			this.indicateError(ex.getMessage());
		}
	}
	
	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		if (dataMessages.size()==1) {
			// if there is only one JsonObject but it is inside a List
			executeAgent(dataMessages.get(0));
		} else {
			throw new UnsupportedOperationException("Not supported.");
		}
		
	}
	
	@Override
	protected void uploadResults() {
		// convert the data into a String
		String uploadData = JSONFile.collectionToString(results);

		// just call storeData from the superclass {@link Agente} with the following parameters and everything is just fine
		// 1. Runid, which can be accessed via getWorkflowID()
		// 2. The pipe in which the data "is put". It is constructed from the agent instance id and the output identifier.
		// 3. The data, in this case it is our String uploadData
		storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", uploadData);
	}
	
	
	/**
	 * Collects edges that match the attribute type and value for
	 * the source and target nodes of an edge. The matched edges
	 * are deleted from the graph if the operator is != leaving only
	 * the edges that did not match. If the operator is == the matched
	 * edges are used as the new edge set leaving only the matched edges.
	 * @param file
	 * @return
	 * @throws IllegalContentTypeException
	 */
	@SuppressWarnings("unchecked")
	private JSONFile filterNet(JSONFile file)
			throws IllegalContentTypeException {
		SGFParser parser = new SGFParser();
		parser.setNetwork(file);
		parser.parse();

		NodeSet nodes = parser.getParsedNodeSet();
		EdgeSet edges = parser.getParsedEdgeSet();
		
		LinkedList<Edge> matchingEdges = new LinkedList<>();
		HashMap<String, Node> nodeMap = new HashMap<>();
		for(int i = 0; i<nodes.size();i++){
			Node node = (Node) nodes.get(i);
			nodeMap.put(node.getId(), node);
		}
		
		
		for(int i = 0; i<edges.size();i++){
			Edge edge = (Edge) edges.get(i);
			Node sourceNode = nodeMap.get(edge.getSource());
			Node targetNode = nodeMap.get(edge.getTarget());
			String sourceNodeValue = sourceNode.getProperty(sourceAttribute);
			String targetNodeValue = targetNode.getProperty(targetAttribute);
			if(sourceNodeValue.equals(sourceValue)&&targetNodeValue.equals(targetValue)){
				matchingEdges.add(edge);
			}
		}
		if(operator.equals(EdgeFilterManager.operatorNotEqual)){
			edges.removeAll(matchingEdges);
		}else if (operator.equals(EdgeFilterManager.operatorEqual)){
			edges = new EdgeSet();
			edges.addAll(matchingEdges);
		}

		parser.updateEdgeSet(edges);
		JSONFile dataFile = new JSONFile("graph" + System.currentTimeMillis()
				+ ".sgf", "sgf", parser.encode(), JSONFile.TEXT);

		return dataFile;
	}

}
