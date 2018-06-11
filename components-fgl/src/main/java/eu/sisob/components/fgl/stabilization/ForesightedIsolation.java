package eu.sisob.components.fgl.stabilization;

import java.util.Collections;
import java.util.Vector;

import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.technique.graph.fgl.util.NodePartition;

public class ForesightedIsolation extends DefaultStabilization{	
	
	private Vector<Integer> lifetimeMap;
	private Vector<Integer> flickeringMap;	
	
	public ForesightedIsolation(){
		super();		
		this.lifetimeMap = new Vector<Integer>();
		this.flickeringMap = new Vector<Integer>();		
		
	}
	
	@Override
	public void buildTimeMatrix() throws Exception {
		// TODO Auto-generated method stub
		super.setTimeMatrix(new int [super.getEntitiesOverTime().size()][super.getNetworks().size()]);	
		for(int row=0;row<super.getEntitiesOverTime().size();row++){
			for(int column=0;column<super.getNetworks().size();column++){
				super.getTimeMatrix()[row][column]=-1;
			}	
		}		
		
		for(int column=0;column<super.getNetworks().size();column++){
			SGFParser parser = new SGFParser();
			parser.setNetwork(super.getNetworks().get(column));
			parser.parse();				
			
			NodeSet nodeset = parser.getParsedNodeSet();	
			EdgeSet edgeset = parser.getParsedEdgeSet();
			
			for(Node node:nodeset.getValues()){
				int degreeCentrality = 0;
				int row = super.getEntitiesOverTime().indexOf(node.getLabel());
				for(Edge edge:edgeset.getValues()){
					if(edge.getSource().trim().equalsIgnoreCase(node.getId().trim()) || edge.getTarget().trim().equalsIgnoreCase(node.getId().trim())){
						degreeCentrality++;
					}
				}
				super.getTimeMatrix()[row][column]=degreeCentrality;
			}						
		}		
	}
	
	private void buildLifeTimeMap() {
		// TODO Auto-generated method stub		
		for(int row = 0; row<super.getEntitiesOverTime().size();row++){
			int lifetime = 0;
			for(int column = 0; column<super.getNetworks().size();column++){
				if(super.getTimeMatrix()[row][column]!=-1){
					lifetime++;
				}
			}
			if(lifetimeMap.indexOf(lifetime)==-1){
				lifetimeMap.add(lifetime);
			}
		}		
		Collections.sort(lifetimeMap);		
	}
	
	private void buildFlickeringMap(){
		
		for(int i=0;i<super.getNodePartitions().size();i++){
			int partitionFlickering = 0;
			NodePartition partition = super.getNodePartitions().get(i);
			if(flickeringMap.indexOf(calculatePartitionFlickering(partition))==-1){
				flickeringMap.add(partitionFlickering);
			}			
		}		
		Collections.sort(flickeringMap, Collections.reverseOrder());
	}
	
	
	private int calculatePartitionFlickering(NodePartition partition){
		int partitionFlickering = 0;
		Vector<Integer> timeSpecter = buildTimeSpecter(partition);
		for(int j=0;j<timeSpecter.size();j++){
			if(j+1<timeSpecter.size() && timeSpecter.get(j)!=timeSpecter.get(j+1)){
				partitionFlickering++;
			}
		}		
		return partitionFlickering;
	}
	
	private Vector<Integer> buildTimeSpecter(NodePartition partition) {
		// TODO Auto-generated method stub
		Vector<Integer> specter = new Vector<Integer>();
		for(int i=0;i<partition.getPartitionValues().size();i++){
			specter.add( (partition.getPartitionValues().get(i) != null) ? 1 : 0);
		}		
		return specter;
	}

	@Override
	public void fillNodePartitions(){
		buildLifeTimeMap();
		for(int i=0;i<lifetimeMap.size();i++){
			NodeSet currentEntities = loadCurrentEntities(lifetimeMap.get(i));
			buildPartitions(currentEntities);
		}
		
		buildFlickeringMap();
		for(int i=0;i<flickeringMap.size();i++){
			stabilizePartitions(flickeringMap.get(i));	
		}		
	}	
	
	private void stabilizePartitions(int flickeringPriority) {
		// TODO Auto-generated method stub
		for(int i=0;i<super.getNodePartitions().size();i++){
			NodePartition partition = super.getNodePartitions().get(i);
			if(partition!=null){
				int partitionFlickering = calculatePartitionFlickering(partition);
				if(partitionFlickering==flickeringPriority){
					stabilize(partition);					
				}	
			}					
		}
		
	}

	private void stabilize(NodePartition partition) {
		// TODO Auto-generated method stub
		for(int i=0;i<super.getNodePartitions().size();i++){
			NodePartition candidatePartition = super.getNodePartitions().get(i);
			if(candidatePartition!=null && !collision(partition, candidatePartition)){
				mergePartitions(partition, candidatePartition);
				super.getNodePartitions().remove(i);
				i=i-1;
			}
		}		
	}

	private NodeSet loadCurrentEntities(Integer lifeTimeID) {
		// TODO Auto-generated method stub
		NodeSet currentEntities = new NodeSet();
		
		for(int row = 0; row<super.getEntitiesOverTime().size();row++){
			int lifetime = 0;
			for(int column = 0; column<super.getNetworks().size();column++){
				if(super.getTimeMatrix()[row][column]!=-1){
					lifetime++;
				}
			}
			if(lifetime==lifeTimeID){
				Node currentCandidate = new Node(""+row,super.getEntitiesOverTime().get(row));
				currentEntities.addNode(currentCandidate);
			}
		}		
		
		return currentEntities;
	}
		
	private void buildPartitions(NodeSet currentEntities) {
		// TODO Auto-generated method stub
		while(currentEntities.size()>0){
			Node currentEntity = (Node)currentEntities.remove(0);			
			NodePartition partition = completePartition(requestUnstablePartition(currentEntity),currentEntities);			
			super.getNodePartitions().add(partition);
		}	
	}
	
	private NodePartition requestUnstablePartition(Node node) {
		// TODO Auto-generated method stub
		int row = super.getEntitiesOverTime().indexOf(node.getLabel());
		Vector<Node> partitionValues = new Vector<Node>();
		for(int column=0;column<super.getNetworks().size();column++){
			partitionValues.add((super.getTimeMatrix()[row][column]!=-1) ? new Node("", node.getLabel()) : null);
		}			
		NodePartition partition = new NodePartition(""+super.getNodePartitions().size(), partitionValues);		
		return partition;
	}	

	private NodePartition completePartition(NodePartition partition, NodeSet currentEntities) {
		// TODO Auto-generated method stub
		int partitionAverageDegree = partitionAverageDegree(partition);	
		int partitionAverageGradient = partitionAverageGradient(partition);
		
		for(int i=0;i<currentEntities.size();i++){
			Node currentEntity = (Node) currentEntities.get(i);
			int currentAverageDegree = averageDegree(currentEntity);
			NodePartition candidatePartition = requestUnstablePartition(currentEntity);
			if(!collision(partition,candidatePartition) && isToleranceInRange(currentAverageDegree,partitionAverageDegree,partitionAverageGradient)){
				mergePartitions(partition, candidatePartition);				
				currentEntities.remove(i);
				i=i-1;				
			}			
			
		}
		return partition;
	}
	
	private void mergePartitions(NodePartition partition, NodePartition candidatePartition) {
		// TODO Auto-generated method stub		
		for(int i=0;i<super.getNetworks().size();i++){
			if(partition.getPartitionValues().get(i)==null){
				partition.getPartitionValues().setElementAt(candidatePartition.getPartitionValues().get(i), i);				
			}
		}
	}
	
	private boolean isToleranceInRange(int currentAverageDegree, int partitionAverageDegree, int partitionAverageGradient) {
		// TODO Auto-generated method stub
		if( (currentAverageDegree-partitionAverageDegree) <= partitionAverageGradient)
			return true;
		
		return false;
	}

	private boolean collision(NodePartition partition, NodePartition candidatePartition) {
		// TODO Auto-generated method stub
		for(int i=0;i<super.getNetworks().size();i++){
			if(partition.getPartitionValues().get(i)!=null && candidatePartition.getPartitionValues().get(i)!=null){
				return true;
			}
		}
		
		return false;
	}	
	
	private int averageDegree(Node node){
		int averageDegree = 0;
		int appearance = 0;
		int row = super.getEntitiesOverTime().indexOf(node.getLabel());		
		for(int column=0;column<super.getNetworks().size();column++){
			if(super.getTimeMatrix()[row][column]!=-1){
				appearance++;
				averageDegree+=super.getTimeMatrix()[row][column];
			}
			
		}			
		return averageDegree/appearance;
	}
	
	private int partitionAverageDegree(NodePartition partition){
		int activeValues = 0;
		for(int i=0;i<partition.getPartitionValues().size();i++){
			Node partitionValue = partition.getPartitionValues().get(i);
			if(partitionValue!=null){
				activeValues++;
			}
		}
		
		int degreeSum = 0; 
		Vector<String> processedNodes = new Vector<String>();
		for(int i=0;i<partition.getPartitionValues().size();i++){
			Node partitionValue = partition.getPartitionValues().get(i);
			if(partitionValue!=null && processedNodes.indexOf(partitionValue.getLabel())==-1){
				degreeSum += averageDegree(partitionValue);
			}
		}		
		return degreeSum/activeValues;
	}
	
	private int partitionAverageGradient(NodePartition partition){
		Vector<Integer> gradientVector = new Vector<Integer>();
		Vector<String> processedNodes = new Vector<String>();
		
		for(int i=0;i<partition.getPartitionValues().size();i++){
			Node partitionValue = partition.getPartitionValues().get(i);
			if(partitionValue!=null && processedNodes.indexOf(partitionValue.getLabel())==-1){
				int nodeIndex = super.getEntitiesOverTime().indexOf(partitionValue.getLabel());
				for(int j=0;j<super.getNetworks().size();j++){
					if(super.getTimeMatrix()[nodeIndex][j]!=-1)
					gradientVector.add(super.getTimeMatrix()[nodeIndex][j]);
				}	
			}
		}
		
		int sum = 0;
		for(int i=0;i<gradientVector.size();i++){
			if(i+1<gradientVector.size()){
				sum+=Math.abs(gradientVector.get(i) - gradientVector.get(i+1));
			}
		}
		
		return sum/gradientVector.size();
	}
	
	
}
