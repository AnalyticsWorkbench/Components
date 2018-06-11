package info.collide.workbench.components.graphpropertyfilter;

import java.util.HashSet;
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

public class GraphPropertyFilterAgent extends Agent {

	private List<JSONFile> results;
	private boolean filterNodes;
	private String property;
	private String operator;
	private String value;

	public GraphPropertyFilterAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);

		results = new LinkedList<JSONFile>();

		filterNodes = getFilterParameters().get(GraphPropertyFilterManager.parameterFilterType).equals("nodes");
		property = getFilterParameters().get(GraphPropertyFilterManager.parameterProperty).toString();
		operator = getFilterParameters().get(GraphPropertyFilterManager.parameterOperator).toString();
		value = getFilterParameters().get(GraphPropertyFilterManager.parameterValue).toString();

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
			Logger.getLogger(GraphPropertyFilterAgent.class.getName()).log(Level.SEVERE, null, ex);
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
	
	private boolean isNumeric(String str) {
		try {
			Float.parseFloat(str);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	private JSONFile filterNet(JSONFile file)
			throws IllegalContentTypeException {
		SGFParser parser = new SGFParser();
		parser.setNetwork(file);
		parser.parse();

		NodeSet nodes = parser.getParsedNodeSet();
		EdgeSet edges = parser.getParsedEdgeSet();

		if (filterNodes) {
			// removes nodes
			LinkedList<Node> delList = new LinkedList<Node>();
			HashSet<String> delIds = new HashSet<>();
			for (int i = 0; i < nodes.size(); i++) {
				Node n = (Node) nodes.get(i);
				String val = n.getProperty(property);
				if (val != null) { // node has the given property
					int cv;
					if (isNumeric(val) && isNumeric(value)) {
						cv = Float.parseFloat(val) < Float.parseFloat(value) ? -1
								: (Float.parseFloat(val) > Float
										.parseFloat(value) ? 1 : 0);
					} else {
						cv = val.compareToIgnoreCase(value);
					}

					if ((operator.equals("=") && cv == 0)
							|| (operator.equals(">=") && cv >= 0)
							|| (operator.equals("<=") && cv <= 0)
							|| ((operator.equals(">") || operator.equals("!=")) && cv > 0)
							|| ((operator.equals("<") || operator.equals("!=")) && cv < 0)) {
						delList.add(n);
						delIds.add(n.getId());
					}

				}
			}
			nodes.removeAll(delList);

			// remove edges linked with the deleted nodes
			LinkedList<Edge> delList2 = new LinkedList<Edge>();
			for (int i = 0; i < edges.size(); i++) {
				Edge e = (Edge) edges.get(i);
				if (delIds.contains(e.getSource())
						|| delIds.contains(e.getTarget())) {
					delList2.add(e);
				}
			}
			edges.removeAll(delList2);
		} else {
			LinkedList<Edge> delList = new LinkedList<Edge>();
			for (int i = 0; i < edges.size(); i++) {
				Edge e = (Edge) edges.get(i);
				String val = e.getProperty(property);
				if (val != null) {
					int cv;
					if (isNumeric(val) && isNumeric(value)) {
						cv = Float.parseFloat(val) < Float.parseFloat(value) ? -1
								: (Float.parseFloat(val) > Float
										.parseFloat(value) ? 1 : 0);
					} else {
						cv = val.compareToIgnoreCase(value);
					}
					if ((operator.equals("=") && cv == 0)
							|| (operator.equals(">=") && cv >= 0)
							|| (operator.equals("<=") && cv <= 0)
							|| ((operator.equals(">") || operator.equals("!=")) && cv > 0)
							|| ((operator.equals("<") || operator.equals("!=")) && cv < 0)) {
						delList.add(e);
					}
				}
			}
			edges.removeAll(delList);
		}

		parser.updateEdgeSet(edges);
		parser.updateNodeSet(nodes);
		JSONFile dataFile = new JSONFile("graph" + System.currentTimeMillis()
				+ ".sgf", "sgf", parser.encode(), JSONFile.TEXT);

		return dataFile;
	}

}
