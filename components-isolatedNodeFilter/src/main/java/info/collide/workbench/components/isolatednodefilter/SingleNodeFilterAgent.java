package info.collide.workbench.components.isolatednodefilter;

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


public class SingleNodeFilterAgent extends Agent {
	
	private List<JSONFile> results;
	
	public SingleNodeFilterAgent(JsonObject commandMessage){
		super(commandMessage);
		results = new LinkedList<JSONFile>();
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		// here we extract the data from a data message
		Vector<JSONFile> files = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
		
		try {
			for (JSONFile file : files) {
				results.add(filterNet(file));
			}
			this.uploadResults();
		} catch (IllegalContentTypeException ex) {
			Logger.getLogger(SingleNodeFilterAgent.class.getName()).log(Level.SEVERE, null, ex);
			this.indicateError(ex.getMessage());
		}
				
		// now the analysis is done, upload results
		this.uploadResults();

		// tell the manager that everyhing is alright and the agent is now ready to be finished
		indicateDone();
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void uploadResults() {
		storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", JSONFile.collectionToString(results));
	}
	
	@SuppressWarnings("unchecked")
	private JSONFile filterNet(JSONFile file) throws IllegalContentTypeException {
		SGFParser parser = new SGFParser();
		parser.setNetwork(file);
		parser.parse();
		
		NodeSet nodes = parser.getParsedNodeSet();
		EdgeSet edges = parser.getParsedEdgeSet();
		HashSet<String> linkedNodes = new HashSet<String>();
		
		for(int i = 0; i <edges.size(); i++){
			Edge e = (Edge) edges.get(i);
			linkedNodes.add(e.getSource());
			linkedNodes.add(e.getTarget());
		}
		
		LinkedList<Node> delList = new LinkedList<Node>();
		for(int i = 0; i <nodes.size(); i++){
			Node n = (Node) nodes.get(i);
			if(!linkedNodes.contains(n.getId())){
				delList.add(n);
			}
		}
		nodes.removeAll(delList);
		
		parser.updateEdgeSet(edges);
		parser.updateNodeSet(nodes);
		JSONFile dataFile = new JSONFile("graph"+System.currentTimeMillis()+".sgf", "sgf", parser.encode(), JSONFile.TEXT);
		
		return dataFile;
	}

}
