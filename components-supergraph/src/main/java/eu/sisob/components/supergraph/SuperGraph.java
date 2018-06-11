package eu.sisob.components.supergraph;

import java.awt.Dimension;
import java.util.Iterator;
import java.util.Vector;

import org.json.simple.JSONArray;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.VisualizationTechnique;
import eu.sisob.api.visualization.technique.interfaces.NetworkUpdate;
import eu.sisob.api.visualization.technique.interfaces.ServerSideCalculation;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;



public class SuperGraph extends VisualizationTechnique implements ServerSideCalculation, NetworkUpdate{
		
	private Vector<Node> superNodeSet;
	private Vector<Edge> superEdgeSet;
	private Graph<Node,Edge> superGraph;
	
	
	public SuperGraph(Vector<JSONFile> networks,String layout) {
		super(networks,layout);
		// TODO Auto-generated constructor stub
		superNodeSet = new Vector<Node>();
		superEdgeSet = new Vector<Edge>();	
		superGraph = new SparseGraph<Node, Edge>();
	}

	@Override
	public void applyLayout(int w, int h) throws Exception {
		// TODO Auto-generated method stub		
		 createSuperNodeSet();
		 createSuperEdgeSet();		 
		 createSuperGraph();
		 
		 if(super.getLayout().equals("Circular"))
			    this.superGraph = applayCircularLayout(w,h,superGraph);
			else if (super.getLayout().equals("Kamada-Kawai"))
				this.superGraph = applyKKLayout(w,h,superGraph);
			else if (super.getLayout().equals("Fruchterman-Rheingold"))
				this.superGraph= applyFRLayout(w,h,superGraph);		 
	}
	
	public void analyzeVisualStability() throws Exception{
		System.out.println("-----Super Graph Layout Visual Stability-----");			
		System.out.println("Node Set Structural Stability:"+calculateNodeSetStructuralStability());
		System.out.println("Edge Set Structural Stability:"+calculateEdgeSetStructuralStability());
		System.out.println("Layout Degree Change:"+calculateLayoutDegreeChange());
		System.out.println("Layout Spatial Movement:"+calculateLayoutSpatialMovement());	
	}
	
	private Vector<Double> calculateLayoutDegreeChange() throws Exception{
		// TODO Auto-generated method stub
		Vector<Double> degreeMovement = new Vector<Double>();
		
		for(int i=0;i<super.getNetworks().size();i++){
			if(i+1<super.getNetworks().size()){		
				
				SGFParser parser = new SGFParser();
				parser.setNetwork(super.getNetworks().get(i));
				parser.parse();
				
				SGFParser futureParser = new SGFParser();
				futureParser.setNetwork(super.getNetworks().get(i+1));
				futureParser.parse();
				
				NodeSet currentNodeSet = parser.getParsedNodeSet();
				EdgeSet currentEdgeSet = parser.getParsedEdgeSet();
				
				NodeSet futureNodeSet = futureParser.getParsedNodeSet();
				EdgeSet futureEdgeSet = futureParser.getParsedEdgeSet();				

	
				double variations = 0;
				//intersection
				for(Node currentNode:currentNodeSet.getValues()){					
					for(Node futureNode:futureNodeSet.getValues()){						
						if(currentNode.getLabel().trim().equals(currentNode.getLabel().trim())){
							double currentNodeDegreeCentrality  = 0;
							for(Edge currentEdge:currentEdgeSet.getValues()){
								if(currentEdge.getSource().equals(currentNode.getId()) || currentEdge.getTarget().equals(currentNode.getId())){
									currentNodeDegreeCentrality++;
								}
							}
							double futureNodeDegreeCentrality  = 0;
							for(Edge futureEdge:futureEdgeSet.getValues()){
								if(futureEdge.getSource().equals(futureNode.getId()) || futureEdge.getTarget().equals(futureNode.getId())){
									futureNodeDegreeCentrality++;
								}
							}
							
							variations += Math.abs(currentNodeDegreeCentrality-futureNodeDegreeCentrality);
						}
					}
				}
				degreeMovement.add(variations);
			}			
		}		
		
		return degreeMovement;
	}

	private Vector<Double> calculateLayoutSpatialMovement() throws Exception{
		Vector<Double> spatialMovement = new Vector<Double>();
	
		for(int i=0;i<super.getNetworks().size();i++){
			if(i+1<super.getNetworks().size()){		
				
				SGFParser parser = new SGFParser();
				parser.setNetwork(super.getNetworks().get(i));
				parser.parse();
				
				SGFParser futureParser = new SGFParser();
				futureParser.setNetwork(super.getNetworks().get(i+1));
				futureParser.parse();
				
				NodeSet currentNodeSet = parser.getParsedNodeSet();								
				NodeSet futureNodeSet = futureParser.getParsedNodeSet();
	
				double variations = 0;

				for(Node currentNode:currentNodeSet.getValues()){					
					for(Node futureNode:futureNodeSet.getValues()){						
						if(currentNode.getLabel().trim().equals(futureNode.getLabel().trim())){
							 JSONArray currentCoordinates = requestNodeCoordinates(currentNode);							 
							 JSONArray futureCoordinates = requestNodeCoordinates(futureNode);							 
							if(currentCoordinates!=null && futureCoordinates!=null){
								double x1 = Double.parseDouble(""+currentCoordinates.get(0));
								double y1 = Double.parseDouble(""+currentCoordinates.get(1));
								
								double x2 = Double.parseDouble(""+futureCoordinates.get(0));
								double y2 = Double.parseDouble(""+futureCoordinates.get(1));
								
								double xDiference = x2-x1;
								double yDiference = y2-y1;
								
								variations+= Math.sqrt(Math.pow(xDiference, 2) + Math.pow(yDiference, 2));	
							}
							
						}
					}
				}
				spatialMovement.add(variations);
			}			
		}		
	
		return spatialMovement;
	}
	
	private Vector<Double> calculateEdgeSetStructuralStability() throws Exception {
		// TODO Auto-generated method stub
		Vector<Double> edgesMaintained = new Vector<Double>();
		
		for(int i=0;i<super.getNetworks().size();i++){
			if(i+1<super.getNetworks().size()){		
				
				SGFParser parser = new SGFParser();
				parser.setNetwork(super.getNetworks().get(i));
				parser.parse();
				
				SGFParser futureParser = new SGFParser();
				futureParser.setNetwork(super.getNetworks().get(i+1));
				futureParser.parse();
				
				NodeSet currentNodeSet = parser.getParsedNodeSet();
				EdgeSet currentEdgeSet = parser.getParsedEdgeSet();
				
				NodeSet futureNodeSet = futureParser.getParsedNodeSet();
				EdgeSet futureEdgeSet = futureParser.getParsedEdgeSet();
				
				Vector<String> union = new Vector<String>();
				Vector<String> intersection = new Vector<String>();

				//union
				for(Edge edge:currentEdgeSet.getValues()){					
					
					Node source = retrieveCurrentNode(edge.getSource(), currentNodeSet);
					Node target = retrieveCurrentNode(edge.getTarget(), currentNodeSet);				
					
					String edgeID = source.getLabel().trim()+"-"+target.getLabel().trim();
					if(union.indexOf(edgeID)==-1){
						union.add(edgeID);
					}
					
				}
				
				for(Edge edge:futureEdgeSet.getValues()){
					
					Node source = retrieveCurrentNode(edge.getSource(), futureNodeSet);
					Node target = retrieveCurrentNode(edge.getTarget(), futureNodeSet);				
					
					String edgeID = source.getLabel().trim()+"-"+target.getLabel().trim();
					if(union.indexOf(edgeID)==-1){
						union.add(edgeID);
					}
					
				}				
				
				//intersection
				for(Edge currentEdge:currentEdgeSet.getValues()){
					Node currentSource = retrieveCurrentNode(currentEdge.getSource(), currentNodeSet);
					Node currentTarget = retrieveCurrentNode(currentEdge.getTarget(), currentNodeSet);		
					
					
					String currentEdgeID = currentSource.getLabel().trim()+"-"+currentTarget.getLabel().trim();
					
					for(Edge futureEdge:futureEdgeSet.getValues()){
						Node futureSource = retrieveCurrentNode(futureEdge.getSource(), futureNodeSet);
						Node futureTarget = retrieveCurrentNode(futureEdge.getTarget(), futureNodeSet);						
						
						String futureEdgeID = futureSource.getLabel().trim()+"-"+futureTarget.getLabel().trim();
						
						if(currentEdgeID.equals(futureEdgeID))
							intersection.add(currentEdgeID);
					}					
				}				

				Double intersectionSize = (double) intersection.size();
				Double unionSize = (double) union.size();				
				
				edgesMaintained.add(intersectionSize/unionSize);
			}			
		}		
		
		return edgesMaintained;
	}

	private Vector<Double> calculateNodeSetStructuralStability() throws Exception{
		Vector<Double> nodesMaintained = new Vector<Double>();
		
		for(int i=0;i<super.getNetworks().size();i++){
			if(i+1<super.getNetworks().size()){		
				
				SGFParser parser = new SGFParser();
				parser.setNetwork(super.getNetworks().get(i));
				parser.parse();
				
				SGFParser futureParser = new SGFParser();
				futureParser.setNetwork(super.getNetworks().get(i+1));
				futureParser.parse();
				
				NodeSet currentNodeSet = parser.getParsedNodeSet();
				NodeSet futureNodeSet = futureParser.getParsedNodeSet();
				
				Vector<String> union = new Vector<String>();
				Vector<String> intersection = new Vector<String>();
				
				//union
				for(Node node:currentNodeSet.getValues()){					
					if(union.indexOf(node.getLabel().trim())==-1){
						union.add(node.getLabel().trim());
					}
				}
				
				for(Node node:futureNodeSet.getValues()){					
					if(union.indexOf(node.getLabel().trim())==-1){
						union.add(node.getLabel().trim());
					}
				}
				
				//intersection
				for(Node currentNode:currentNodeSet.getValues()){					
					for(Node futureNode:futureNodeSet.getValues()){						
						if(currentNode.getLabel().trim().equals(futureNode.getLabel().trim())){
							intersection.add(currentNode.getLabel().trim());
						}
					}
				}				
				Double intersectionSize = (double) intersection.size();
				Double unionSize = (double) union.size();				
				
				nodesMaintained.add(intersectionSize/unionSize);
			}			
		}		

		return nodesMaintained;
	}
	
	private Graph <Node,Edge> applayCircularLayout(int w, int h, Graph <Node,Edge> graph) {
		// TODO Auto-generated method stub
			
			CircleLayout<Node, Edge> circular = new CircleLayout<Node, Edge>(graph);		
			circular.setSize(new Dimension(w,h));
				    
		    final VisualizationModel<Node,Edge> visualizationModel =  new DefaultVisualizationModel<Node,Edge>(circular, new Dimension(w,h));
		    VisualizationViewer<Node,Edge> vv =  new VisualizationViewer<Node,Edge>(visualizationModel, new Dimension(w,h));	    

		    Iterator <Node> vertexIterator = circular.getGraph().getVertices().iterator();
		    while(vertexIterator.hasNext()){			
		    	Node n = vertexIterator.next();
		    	n.setCoordinates(circular.transform(n).getX()+"", circular.transform(n).getY()+"");			
			}		
		    
		    return graph;
		}
	
	private Graph <Node,Edge>applyFRLayout(int w, int h, Graph <Node,Edge> graph) {
		// TODO Auto-generated method stub
		
		FRLayout<Node, Edge> fr = new FRLayout <Node, Edge> (graph);
		fr.setSize(new Dimension(w,h));
		fr.setMaxIterations(Integer.MAX_VALUE);		
		fr.setRepulsionMultiplier(0.50);
		fr.setAttractionMultiplier(0.50);
	    
	    final VisualizationModel<Node,Edge> visualizationModel =  new DefaultVisualizationModel<Node,Edge>(fr, new Dimension(w,h));
	    VisualizationViewer<Node,Edge> vv =  new VisualizationViewer<Node,Edge>(visualizationModel, new Dimension(w,h));	    

	    Iterator <Node> vertexIterator = fr.getGraph().getVertices().iterator();
	    while(vertexIterator.hasNext()){			
	    	Node n = vertexIterator.next();
	    	n.setCoordinates(fr.transform(n).getX()+"", fr.transform(n).getY()+"");			
		}
	    
	    return graph;
		
	}

	private Graph <Node,Edge> applyKKLayout(int w, int h,Graph <Node,Edge>graph) {
		// TODO Auto-generated method stub
		
		KKLayout<Node, Edge> kk = new KKLayout <Node, Edge> (graph);
		kk.setSize(new Dimension(w,h));		
		kk.setMaxIterations(Integer.MAX_VALUE);
		kk.setDisconnectedDistanceMultiplier(2.5);
		kk.setLengthFactor(0.2);	
		
		
	    final VisualizationModel<Node,Edge> visualizationModel =  new DefaultVisualizationModel<Node,Edge>(kk, new Dimension(w,h));
	    VisualizationViewer<Node,Edge> vv =  new VisualizationViewer<Node,Edge>(visualizationModel, new Dimension(w,h));	    
	    
	    Iterator <Node> vertexIterator = kk.getGraph().getVertices().iterator();
	    while(vertexIterator.hasNext()){			
	    	Node n = vertexIterator.next();
	    	n.setCoordinates(kk.transform(n).getX()+"", kk.transform(n).getY()+"");			
		}	

		return graph;
	}
	
	private void createSuperGraph() {
		// TODO Auto-generated method stub		
		for(Node node:superNodeSet){
			superGraph.addVertex(node);
		}
		
		for(Edge edge:superEdgeSet){
			superGraph.addEdge(edge, superNodeSet.get(Integer.parseInt(edge.getSource())), superNodeSet.get(Integer.parseInt(edge.getTarget())));			
		}		
	}	

	private void createSuperEdgeSet()throws IllegalContentTypeException{		
		
	    int superEdgeID = 0;
		for(JSONFile network:super.getNetworks()){
			SGFParser parser = new SGFParser();
			parser.setNetwork(network);
			parser.parse();		
			EdgeSet currentEdgeSet = parser.getParsedEdgeSet();
			NodeSet currentNodeSet = parser.getParsedNodeSet();
			
			for(Edge edge:currentEdgeSet.getValues()){
				Node sourceNode = retrieveCurrentNode(edge.getSource(), currentNodeSet);
				Node targetNode = retrieveCurrentNode(edge.getTarget(), currentNodeSet);
				
				int source = retrieveSuperNodeID(sourceNode);
				int target = retrieveSuperNodeID(targetNode);				
				Edge superEdge = new Edge(""+superEdgeID,""+source,""+target);
				superEdgeSet.add(superEdge);
				superEdgeID++;
			}			
		}		
	}	
	
	private void createSuperNodeSet() throws IllegalContentTypeException{
		
		int superNodeID=0;
		for(JSONFile network:super.getNetworks()){
			SGFParser parser = new SGFParser();
			parser.setNetwork(network);
			parser.parse();			
			NodeSet parsingnodeset = parser.getParsedNodeSet();
			
			for(int i=0;i<parsingnodeset.size();i++){
				Node node = (Node) parsingnodeset.get(i);
				if(!isNodeInSuperNodeSet(node)){
					node.setId(""+superNodeID);
					superNodeSet.add(node);		
					superNodeID++;
				}				
			}			
		}		
	}
	
	
	private Node retrieveCurrentNode(String nodeID,NodeSet nodeset){		
		for(Node currentNode:nodeset.getValues()){
			if(currentNode.getId().equals(nodeID))
				return currentNode;
		}
		return null;
	}
	
	private int retrieveSuperNodeID(Node node){		
		for(int i=0;i<superNodeSet.size();i++){
			Node superNode = (Node) superNodeSet.get(i);
			if(superNode.getLabel().trim().equalsIgnoreCase(node.getLabel().trim()))
				return i;
		}		
		return -1;
	}	
	
	private Node translateToSuperNode(Node node){
		for(Node supernode:superNodeSet){
			if(supernode.getLabel().trim().equalsIgnoreCase(node.getLabel().trim()))
				return supernode;
		}				
		return null;
	}		

	private boolean isNodeInSuperNodeSet(Node node) {
		// TODO Auto-generated method stub		
		for(Node supernode:superNodeSet){
			if(supernode.getLabel().trim().equalsIgnoreCase(node.getLabel().trim()))
				return true;
		}				
		return false;
	}

	private JSONArray requestNodeCoordinates(Node normalNode) {
		Iterator<Node> i = superGraph.getVertices().iterator();
		while(i.hasNext()){
			Node superGraphNode = i.next();
			if(superGraphNode.getLabel().trim().equals(normalNode.getLabel().trim()))
				return superGraphNode.getCoordinates();			
		}
		return null;
	}		

	@Override
	public Vector<JSONFile> updatedNetworks() throws Exception {
		// TODO Auto-generated method stub
	Vector<JSONFile> networks = super.getNetworks();
		
		for(JSONFile network:networks){
			 String originalNetwork = network.getStringContent();
			 
			 SGFParser parser = new SGFParser();
			 parser.setNetwork(network);
			 parser.parse();
			 
			 Metadata m = parser.getParsedMetadata();
			 NodeSet ns = parser.getParsedNodeSet();
			 EdgeSet es = parser.getParsedEdgeSet();			
			 
			 for(Node node: ns.getValues()){				 
				 Node superNode = translateToSuperNode(node);				 
				 JSONArray coordinates = requestNodeCoordinates(superNode);
				 if(coordinates!=null)
					 node.setCoordinates(coordinates.get(0).toString(), coordinates.get(1).toString());
			 }			 
			 parser.updateNodeSet(ns);
			 parser.updateEdgeSet(es);
			 parser.updateMetadata(m);
			 network.setTextContent(parser.encode());
			 
		}
		
		return networks;	
	}
	
	
}
