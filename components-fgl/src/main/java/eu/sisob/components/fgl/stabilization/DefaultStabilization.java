package eu.sisob.components.fgl.stabilization;

import java.util.Vector;

import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.technique.graph.fgl.util.EdgePartition;
import eu.sisob.api.visualization.technique.graph.fgl.util.ForesightedGraph;
import eu.sisob.api.visualization.technique.graph.fgl.util.NodePartition;
import eu.sisob.components.framework.json.util.JSONFile;



public class DefaultStabilization {

	private int timeMatrix[][];	
	private Vector<JSONFile> networks;
	
	private Vector<String> entitiesOverTime;	
	
	private Vector<NodePartition> nodePartitions;
	private Vector<EdgePartition> edgePartitions;	
		
	
	public DefaultStabilization(){
		entitiesOverTime = new Vector<String>();		
		nodePartitions = new Vector<NodePartition>();
		edgePartitions = new Vector<EdgePartition>();
	}	
	
	public ForesightedGraph stabilize(Vector<JSONFile> networks) throws Exception{		
		this.networks = networks;
		generateNodePartitions();
		generateEdgePartitions();
		
		ForesightedGraph fgl = new ForesightedGraph(nodePartitions, edgePartitions);
		
		return fgl;		
	}

	public void generateNodePartitions() throws Exception {
		// TODO Auto-generated method stub
		scanEntitiesOverTime();
		buildTimeMatrix();
		fillNodePartitions();		
	}
	
	public void generateEdgePartitions() throws Exception {
		// TODO Auto-generated method stub
		for(JSONFile network:networks){
			SGFParser parser = new SGFParser();
			parser.setNetwork(network);
			parser.parse();
			NodeSet nodeset = parser.getParsedNodeSet();
			EdgeSet edgeset = parser.getParsedEdgeSet();
			for(Edge edge:edgeset.getValues()){
				Node source = translateNode(nodeset, edge.getSource());
				Node target = translateNode(nodeset, edge.getTarget());	
				String sourcePartition = translateToNodePartition(source);
				String targetPartition = translateToNodePartition(target);
				
				String edgePartitionID = sourcePartition+"-"+targetPartition;		
				
				EdgePartition edgePartition = locateEdgePartition(edgePartitionID);
				
				if(edgePartition!=null){
					edgePartition.addValue(edge);
				}else{
					EdgePartition edgepartition = new EdgePartition(edgePartitionID);
					edgepartition.addValue(edge);					
					edgePartitions.add(edgepartition);
				}				
			}
		}		
	}
	
	public EdgePartition locateEdgePartition(String edgePartitionID) {
		// TODO Auto-generated method stub
		for(EdgePartition partition:edgePartitions){
			if(partition.getPartitionID().equals(edgePartitionID))
				return partition;
		}
		return null;
	}

	public String translateToNodePartition(Node node) {
		// TODO Auto-generated method stub
		for(NodePartition partition:nodePartitions){
			for(Node partitionValues:partition.getPartitionValues()){
				if(partitionValues!=null && partitionValues.getLabel().trim().equalsIgnoreCase(node.getLabel().trim()))
					return partition.getPartitionID();
			}
		}		
		return null;
	}

	public Node translateNode(NodeSet nodeset, String id) {
		// TODO Auto-generated method stub
		for(Node node:nodeset.getValues()){
			if(node.getId().equalsIgnoreCase(id))
				return node;
		}		
		return null;
	}
	
	public void buildTimeMatrix() throws Exception {
		// TODO Auto-generated method stub
		timeMatrix = new int [entitiesOverTime.size()][networks.size()];		
		for(int column=0;column<networks.size();column++){
			SGFParser parser = new SGFParser();
			parser.setNetwork(networks.get(column));
			parser.parse();				
			
			NodeSet nodeset = parser.getParsedNodeSet();			
			for(Node node:nodeset.getValues()){
				int row = entitiesOverTime.indexOf(node.getLabel().trim());
				if(row != -1){
					timeMatrix[row][column]=1;
				}
			}						
		}	
	}
	
	
	public NodeSet lookForSharablePositions(NodeSet currentEntities,int currentTime) {
		// TODO Auto-generated method stub
		for(int i=0;i<currentEntities.getValues().size();i++){
			Node currentEntity = currentEntities.getValues().get(i);
			for(int j=0;j<nodePartitions.size();j++){
				Vector<Node> partitionValues = nodePartitions.get(j).getPartitionValues();
				if(canShareThePartition(currentEntity,partitionValues)){
					partitionValues.add(currentEntity);
					currentEntities.remove(i);
					i=i-1;
					break;
				}				
			}		
		}		
		return currentEntities;
	}
	
	
	
	public boolean canShareThePartition(Node entity,Vector<Node> partitionValues) {
		// TODO Auto-generated method stub
		for(Node r:partitionValues){
			int a = entitiesOverTime.indexOf(entity.getLabel().trim());
			int b = entitiesOverTime.indexOf(r.getLabel().trim());
			
			if(canShare(a,b)!=true)
				return false;
		}

		return true;
	}
	
	public boolean canShare(int node, int nodeOnPartition) {
		// TODO Auto-generated method stub
		for(int z=0;z<networks.size();z++){
		      if((timeMatrix[nodeOnPartition][z]==1) && (timeMatrix[node][z]==1))
		    	  return false;
		}			
		return true;
	}	

	
	public void scanEntitiesOverTime() throws Exception{	
		for(JSONFile network:this.networks){
			SGFParser parser = new SGFParser();
			parser.setNetwork(network);
			parser.parse();
			
			NodeSet nodeset = parser.getParsedNodeSet();			
			for(Node node:nodeset.getValues()){
				if(entitiesOverTime.indexOf(node.getLabel().trim())==-1){
					entitiesOverTime.add(node.getLabel().trim());
				}
			}				
						
		}		
	}	
	
	public void fillNodePartitions() throws Exception {
		// TODO Auto-generated method stub
		for(int i=0;i<networks.size();i++){
			SGFParser parser = new SGFParser();
			parser.setNetwork(networks.get(i));
			parser.parse();
			
			NodeSet currentEntities = parser.getParsedNodeSet();
				currentEntities = lookForPreviouslyInsertedEntities(currentEntities);				
			if(currentEntities.size()>0)
				currentEntities = lookForSharablePositions(currentEntities,i);
			if(currentEntities.size()>0)
				currentEntities = increaseNumberOfPartitions(currentEntities);
		}		
	}
	
	public NodeSet lookForPreviouslyInsertedEntities(NodeSet currentEntities) {
		// TODO Auto-generated method stub
		for(int i=0;i<currentEntities.getValues().size();i++){
			Node currentEntity = currentEntities.getValues().get(i);
			boolean found = false;
			for(int j=0;j<nodePartitions.size();j++){
				Vector<Node> partitionValues = nodePartitions.get(j).getPartitionValues();
				for(int k=0; k<partitionValues.size();k++){
					Node partitionValue = partitionValues.get(k);
					if(currentEntity.getLabel().equalsIgnoreCase(partitionValue.getLabel())){
						partitionValues.add(currentEntity);
						currentEntities.remove(i);
						i=i-1;
						found = true;
						break;
					}
				}
				if(found)
					break;
			}		
		}		
		return currentEntities;
	}
	
	public NodeSet increaseNumberOfPartitions(NodeSet currentEntities) {
		// TODO Auto-generated method stub
		for(int i=0;i<currentEntities.getValues().size();i++){			
			Node currentEntity = currentEntities.getValues().get(i);
			NodePartition partition = new NodePartition(""+nodePartitions.size());
			partition.addValue(currentEntity);
			nodePartitions.add(partition);
			currentEntities.remove(i);
			i=i-1;			
		}		
		return currentEntities;
	}
	
	public int[][] getTimeMatrix() {
		return timeMatrix;
	}

	public void setTimeMatrix(int[][] timeMatrix) {
		this.timeMatrix = timeMatrix;
	}

	public Vector<JSONFile> getNetworks() {
		return networks;
	}

	public void setNetworks(Vector<JSONFile> networks) {
		this.networks = networks;
	}

	public Vector<String> getEntitiesOverTime() {
		return entitiesOverTime;
	}

	public void setEntitiesOverTime(Vector<String> entitiesOverTime) {
		this.entitiesOverTime = entitiesOverTime;
	}

	public Vector<NodePartition> getNodePartitions() {
		return nodePartitions;
	}

	public void setNodePartitions(Vector<NodePartition> nodePartitions) {
		this.nodePartitions = nodePartitions;
	}

	public Vector<EdgePartition> getEdgePartitions() {
		return edgePartitions;
	}

	public void setEdgePartitions(Vector<EdgePartition> edgePartitions) {
		this.edgePartitions = edgePartitions;
	}
	
	
}
