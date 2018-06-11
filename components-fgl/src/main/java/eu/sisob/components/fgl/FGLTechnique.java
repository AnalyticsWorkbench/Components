package eu.sisob.components.fgl;

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

import eu.sisob.api.visualization.technique.graph.fgl.util.EdgePartition;
import eu.sisob.api.visualization.technique.graph.fgl.util.ForesightedGraph;
import eu.sisob.api.visualization.technique.graph.fgl.util.NodePartition;
import eu.sisob.api.visualization.technique.interfaces.NetworkUpdate;
import eu.sisob.api.visualization.technique.interfaces.ServerSideCalculation;
import eu.sisob.components.fgl.stabilization.DefaultStabilization;
import eu.sisob.components.fgl.stabilization.DegreeStabilization;
import eu.sisob.components.fgl.stabilization.FlickeringReduction;
import eu.sisob.components.fgl.stabilization.ForesightedIsolation;
import eu.sisob.components.fgl.stabilization.GapReduction;
import eu.sisob.components.fgl.stabilization.GradientStabilization;
import eu.sisob.components.framework.json.util.JSONFile;

public class FGLTechnique extends VisualizationTechnique implements ServerSideCalculation, NetworkUpdate{

	public static final String DEFAULT_STABILIZATION = "Default";
	public static final String DEGREE_STABILIZATION = "Degree";
	public static final String GRADIENT_STABILIZATION = "Gradient";
	public static final String FLICKER_REDUCTION = "Flicker";
	public static final String GAP_REDUCTION = "Gap";
	public static final String FORESIGHTED_ISOLATION = "Isolation";	
	
	private DefaultStabilization stabilization;
	
	private String strategy;	
	private ForesightedGraph fg;
	private Graph  <Node, Edge> graph;	
	
	public FGLTechnique(Vector<JSONFile> networks,String layout,String strategy){
		super(networks,layout);
		this.strategy = strategy;
	}	

	@Override
	public void applyLayout(int w, int h) throws Exception {
		// TODO Auto-generated method stub
		if(this.strategy.equals(FGLTechnique.DEFAULT_STABILIZATION)){
			this.stabilization = new DefaultStabilization();			
		}else if(this.strategy.equals(FGLTechnique.DEGREE_STABILIZATION)){
			this.stabilization = new DegreeStabilization();
		}else if(this.strategy.equals(FGLTechnique.GRADIENT_STABILIZATION)){
			this.stabilization = new GradientStabilization();
		}else if(this.strategy.equals(FGLTechnique.FLICKER_REDUCTION)){
			this.stabilization = new FlickeringReduction();
		}else if(this.strategy.equals(FGLTechnique.GAP_REDUCTION)){
			this.stabilization = new GapReduction();
		}else if(this.strategy.equals(FGLTechnique.FORESIGHTED_ISOLATION)){
			this.stabilization = new ForesightedIsolation();
		}
		
		this.fg = this.stabilization.stabilize(super.getNetworks());
	
		if(super.getLayout().equals("Circular"))
		    this.graph = applayCircularLayout(w,h,transformGraph());
		else if (super.getLayout().equals("Kamada-Kawai"))
			this.graph= applyKKLayout(w,h,transformGraph());
		else if (super.getLayout().equals("Fruchterman-Rheingold"))
			this.graph= applyFRLayout(w,h,transformGraph());		
	}	
	
	public void analyzeVisualStability() throws Exception{
		System.out.println("-----Foresighted Graph Layout Visual Stability:"+this.strategy+"-----");
		System.out.println("Node Set Structural Stability:"+calculateNodeSetStructuralStability());
		System.out.println("Edge Set Structural Stability:"+calculateEdgeSetStructuralStability());
		System.out.println("Layout Degree Change:"+calculateLayoutDegreeChange());
		System.out.println("Layout Spatial Movement:"+calculateLayoutSpatialMovement());
//		System.out.println("Node Partitions:"+this.fg.getNodePartitions().size());
//		System.out.println("Edge Partitions:"+this.fg.getEdgePartitions().size());
				
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
					String currentPartitionID = translateToNodePartition(currentNode);
					for(Node futureNode:futureNodeSet.getValues()){
						String futurePartitionID = translateToNodePartition(futureNode);
						if(currentPartitionID.equals(futurePartitionID)){
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
					String currentPartitionID = translateToNodePartition(currentNode);
					for(Node futureNode:futureNodeSet.getValues()){
						String futurePartitionID = translateToNodePartition(futureNode);
						if(currentPartitionID.equals(futurePartitionID)){
							 JSONArray currentCoordinates = requestNodeCoordinates(translateToNodePartition(currentNode));							 
							 JSONArray futureCoordinates = requestNodeCoordinates(translateToNodePartition(futureNode));							 
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
					Node source = translateNode(currentNodeSet, edge.getSource());
					Node target = translateNode(currentNodeSet, edge.getTarget());
					
					String sourcePartition = translateToNodePartition(source);
					String targetPartition = translateToNodePartition(target);
					
					String edgeID = sourcePartition+"-"+targetPartition;
					if(union.indexOf(edgeID)==-1){
						union.add(edgeID);
					}
					
				}
				
				for(Edge edge:futureEdgeSet.getValues()){
					Node source = translateNode(futureNodeSet, edge.getSource());
					Node target = translateNode(futureNodeSet, edge.getTarget());
					
					String sourcePartition = translateToNodePartition(source);
					String targetPartition = translateToNodePartition(target);
					
					String edgeID = sourcePartition+"-"+targetPartition;
					if(union.indexOf(edgeID)==-1){
						union.add(edgeID);
					}
					
				}				
				
				//intersection
				for(Edge currentEdge:currentEdgeSet.getValues()){
					Node currentSource = translateNode(currentNodeSet, currentEdge.getSource());
					Node currentTarget = translateNode(currentNodeSet, currentEdge.getTarget());
					
					String currentSourcePartition = translateToNodePartition(currentSource);
					String currentTargetPartition = translateToNodePartition(currentTarget);
					
					String currentEdgeID = currentSourcePartition+"-"+currentTargetPartition;
					
					for(Edge futureEdge:futureEdgeSet.getValues()){
						Node futureSource = translateNode(futureNodeSet, futureEdge.getSource());
						Node futureTarget = translateNode(futureNodeSet, futureEdge.getTarget());
						
						String futureSourcePartition = translateToNodePartition(futureSource);
						String futureTargetPartition = translateToNodePartition(futureTarget);
						
						String futureEdgeID = futureSourcePartition+"-"+futureTargetPartition;
						
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
					String partitionID = translateToNodePartition(node);
					if(union.indexOf(partitionID)==-1){
						union.add(partitionID);
					}
				}
				
				for(Node node:futureNodeSet.getValues()){
					String partitionID = translateToNodePartition(node);
					if(union.indexOf(partitionID)==-1){
						union.add(partitionID);
					}
				}
				
				//intersection
				for(Node currentNode:currentNodeSet.getValues()){
					String currentPartitionID = translateToNodePartition(currentNode);
					for(Node futureNode:futureNodeSet.getValues()){
						String futurePartitionID = translateToNodePartition(futureNode);
						if(currentPartitionID.equals(futurePartitionID)){
							intersection.add(currentPartitionID);
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
	
	//layout methods
	private Graph<Node, Edge> transformGraph() {
		// TODO Auto-generated method stub
		Graph<Node, Edge> graph  = new SparseGraph<Node,Edge>();		
		NodeSet nodeset = new NodeSet();
		EdgeSet edgeset = new EdgeSet();		
		
		 for (int i=0;i<fg.getNodePartitions().size();i++){
			 NodePartition p = fg.getNodePartitions().get(i);			 			 
			 Node n = new Node(p.getPartitionID(), "Partition "+p.getPartitionID());
			 nodeset.addNode(n);
		 }
		 
		 for(int i = 0; i<fg.getEdgePartitions().size();i++){		 
			 EdgePartition p=fg.getEdgePartitions().get(i);
			 String sourcePartition = p.getPartitionID().split("-")[0];
			 String targetPartition = p.getPartitionID().split("-")[1];
			 Edge edge = new Edge(p.getPartitionID(),sourcePartition,targetPartition);
			 edgeset.addEdge(edge);			 
		 }
		 
		 
		for(Node n:nodeset.getValues()){
			graph.addVertex(n);
		}
		
		for(Edge e:edgeset.getValues()){
			graph.addEdge(e, translateNode(nodeset,e.getSource()), translateNode( nodeset,e.getTarget()));
		}
		 
		
		return graph;
	}
	
	private Graph <Node,Edge> applyFRLayout(int w, int h, Graph <Node,Edge> graph) {
		// TODO Auto-generated method stub
		
		FRLayout<Node, Edge> fr = new FRLayout <Node, Edge> (graph);
		fr.setSize(new Dimension(w,h));		
		fr.setMaxIterations(Integer.MAX_VALUE);		
	
	    
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
		
		
	    final VisualizationModel<Node,Edge> visualizationModel =  new DefaultVisualizationModel<Node,Edge>(kk, new Dimension(w,h));
	    VisualizationViewer<Node,Edge> vv =  new VisualizationViewer<Node,Edge>(visualizationModel, new Dimension(w,h));	    
	    
	    Iterator <Node> vertexIterator = kk.getGraph().getVertices().iterator();
	    while(vertexIterator.hasNext()){			
	    	Node n = vertexIterator.next();
	    	n.setCoordinates(kk.transform(n).getX()+"", kk.transform(n).getY()+"");			
		}		
		return graph;
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
		
	private Node translateNode(NodeSet nodeset, String id) {
		// TODO Auto-generated method stub
		for(Node node:nodeset.getValues()){
			if(node.getId().equalsIgnoreCase(id))
				return node;
		}		
		return null;
	}
	
	private String translateToNodePartition(Node node) {
		// TODO Auto-generated method stub
		for(NodePartition partition:fg.getNodePartitions()){
			for(Node partitionValues:partition.getPartitionValues()){
				if(partitionValues!=null && partitionValues.getLabel().trim().equalsIgnoreCase(node.getLabel().trim()))
					return partition.getPartitionID();
			}
		}		
		return null;
	}
	
	private JSONArray requestNodeCoordinates(String nodePartitionID){
			
			Iterator<Node> i = graph.getVertices().iterator();
			while(i.hasNext()){
				Node n = i.next();
				if(n.getId().equals(nodePartitionID))
					return n.getCoordinates();
			}
			return null;
	}
	
	@Override
	public Vector<JSONFile> updatedNetworks() throws Exception {
		// TODO Auto-generated method stub
		Vector<JSONFile> networks = super.getNetworks();
		
		for(int i=0;i<networks.size();i++){
			JSONFile network = networks.get(i);
			
			 SGFParser parser = new SGFParser();
			 parser.setNetwork(network);
			 parser.parse();
			 
			 Metadata m = parser.getParsedMetadata();
			 NodeSet ns = parser.getParsedNodeSet();
			 EdgeSet es = parser.getParsedEdgeSet();
			
			 
			 for(Edge e:es.getValues()){
				 Node source = translateNode(ns, e.getSource());
				 Node target = translateNode(ns, e.getTarget());
				 
				 String sourcePartition = translateToNodePartition(source);
				 String targetPartition = translateToNodePartition(target);
				
				e.setSource(sourcePartition);
				e.setTarget(targetPartition);			 
				 
			 }
			 
			 for(Node n: ns.getValues()){
				 String nodePartition = translateToNodePartition(n);
				 JSONArray coordinates = requestNodeCoordinates(nodePartition);
				 if(coordinates!=null){
					 n.setId(nodePartition);
					 n.setCoordinates(coordinates.get(0).toString(), coordinates.get(1).toString());	 
				 }			 
				 				 
			 }		
			 
			 parser.updateNodeSet(ns);
			 parser.updateEdgeSet(es);
			 parser.updateMetadata(m);
			 network.setTextContent(parser.encode());
			 
		}
		
		return networks;	
	}
}
