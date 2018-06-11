package eu.sisob.components.directed2undirected;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.collide.sqlspaces.commons.Tuple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.JSONFile;
import org.json.simple.JSONArray;

public class Directed2UndirectedAgent extends Agent {
	private Vector<JSONFile> data;
//	private HashMap<String, String> keySet;
	private String method;
	private boolean removeEdgeWeight;
	private String nameWeight;
	private boolean weightsExist;
	
	public Directed2UndirectedAgent(JsonObject coordinationMessage) {
		
	super(coordinationMessage);

	this.method = (String) this.getFilterParameters().get(Directed2UndirectedManager.METHOD);
	this.removeEdgeWeight = Boolean.parseBoolean(this.getFilterParameters().get(Directed2UndirectedManager.REMOVE_WEIGHTS).toString());
	this.nameWeight = (String) this.getFilterParameters().get(Directed2UndirectedManager.WEIGHT_NAME);
	this.weightsExist = Boolean.parseBoolean((String) this.getFilterParameters().get(Directed2UndirectedManager.WEIGHT_EXISTS).toString());
	}

//	private void prepareFilteringKeys() {
//		this.keySet.put("Edge Betweenness Centrality", "ebc");
//		this.keySet.put("Weight", "weight");
//	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		try {
			this.data = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
			filterEdges(this.data);
			uploadResults();
			indicateDone();
		} catch (Exception g) {
			super.indicateError(g.getMessage());
			g.printStackTrace();
		}
	}

	private void filterEdges(Vector<JSONFile> data) throws Exception {

		for (JSONFile dataFile : data) {
			SGFParser parser = new SGFParser();
			parser.setNetwork(dataFile);
			parser.parse();

			Vector<Edge> edgeVector = parser.getParsedEdgeSet().getValues();
			Vector<Edge> filteredEdgeVector = new Vector<Edge>();

			List<Directed2UndirectedAgent.StringPair> handledEdges = new LinkedList<Directed2UndirectedAgent.StringPair>();

			int edgeCounter = 1;

			for (int i = 0; i < edgeVector.size(); i++) {
				Edge edge = edgeVector.get(i);
				String source = edge.getSource();
				String target = edge.getTarget();

				StringPair edgePair = new StringPair(source, target);

				if (!handledEdges.contains(edgePair)) {

					handledEdges.add(edgePair);

					Edge edge2 = null;
					for (int j = (i + 1); j < edgeVector.size(); j++) {
						Edge innerEdge = edgeVector.get(j);
						if (edge.getSource().equals(innerEdge.getTarget())
								&& edge.getTarget().equals(
										innerEdge.getSource())) {
							edge2 = innerEdge;
						}
					}

					double weight = 1.0;
					double weight2 = 0.0;
					double newWeight = 0.0;
					if (weightsExist){
						String rawWeight = edge.getProperty(this.nameWeight);
						try {
							if (!rawWeight.trim().isEmpty()) {
								weight = Double.parseDouble(rawWeight);
							}
						} catch (NumberFormatException ex) {
							weight = 1.0;
						} catch(NullPointerException e){
							throw new Exception("The edge weight " + this.nameWeight + " does not exist!");
						}
						if (edge2 != null) {
							String rawWeight2 = edge2.getProperty(this.nameWeight);
							try {
								if (!rawWeight2.trim().isEmpty()) {
									weight2 = Double.parseDouble(edge2
											.getProperty(this.nameWeight));
								} 
							} catch (NumberFormatException ex) {
									weight2 = 1.0;
							} catch(NullPointerException e){
								throw new Exception("The edge weight " + this.nameWeight + " does not exist!");
							}
						}
						
						if (method.equals("maximum weight")) {
							newWeight = Math.max(weight, weight2);
						} else if (method.equals("minimal weight")) {
							newWeight = Math.max(weight, weight2);
						} else if (method.equals("mean weight")) {
							newWeight = (weight + weight2) / 2.0;
						} else if (method.equals("sum of weights")) {
							newWeight = weight + weight2;
						}
					}
					if(!weightsExist){
						Edge newEdge = new Edge(Integer.toString(edgeCounter),
							source, target);
						filteredEdgeVector.add(newEdge);
						edgeCounter++;
					}
					else if (newWeight != 0.0) {
						Edge newEdge = new Edge(Integer.toString(edgeCounter),
								source, target);
						if (!removeEdgeWeight) {
							newEdge.addProperty(this.nameWeight,
									Double.toString(newWeight));
						}
						filteredEdgeVector.add(newEdge);
						edgeCounter++;
					}
				}
			}

			EdgeSet filteredEdges = new EdgeSet();
			filteredEdges.setValues(filteredEdgeVector);
			parser.updateEdgeSet(filteredEdges);

			Metadata metaData = parser.getParsedMetadata();
			metaData.setDirectedNetwork("false");
			parser.updateMetadata(metaData);

			dataFile.setTextContent(parser.encode());
			dataFile.setFileName(dataFile.getFileName() + "_undirected");
		}
	}

	@Override
	protected void uploadResults() {
		 try {
			 System.out.println("Uploading Results...");
	            Thread.sleep(2000);
	            
	            JSONArray fileSet = new JSONArray();
	            for(int i=0; i<data.size();i++){
	            	JSONFile file = data.get(i);
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
	public void executeAgent(List<JsonObject> dataMessages) {
		if (dataMessages.size() == 1) {
			executeAgent(dataMessages.get(0));
		} else {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
		
	}

	private class StringPair {

		private String firstString;
		private String secondString;

		private StringPair(String first, String second) {
			firstString = first;
			secondString = second;
		}

		public boolean equals(Object compare) {
			if (compare instanceof StringPair
					&& (firstString.equals(((StringPair) compare).getFirst())
							&& secondString.equals(((StringPair) compare)
									.getSecond()) || firstString
							.equals(((StringPair) compare).getSecond())
							&& secondString.equals(((StringPair) compare)
									.getFirst()))) {
				return true;
			} else {
				return false;
			}
		}

		public String getFirst() {
			return firstString;
		}

		public String getSecond() {
			return secondString;
		}
	}
}
