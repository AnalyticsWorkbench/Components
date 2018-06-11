package eu.sisob.components.fgl.stabilization;

import java.util.Vector;

import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.technique.graph.fgl.util.NodePartition;



public class GapReduction extends DefaultStabilization{
	
	private Vector<Integer> gapMap;
	
	public GapReduction(){
		super();
		gapMap = new Vector<Integer>();
	}
	
	@Override
	public NodeSet lookForPreviouslyInsertedEntities(NodeSet currentEntities) {
		// TODO Auto-generated method stub
		for(int i=0;i<currentEntities.getValues().size();i++){
			Node currentEntity = currentEntities.getValues().get(i);
			boolean found = false;
			for(int j=0;j<super.getNodePartitions().size();j++){
				Vector<Node> partitionValues = super.getNodePartitions().get(j).getPartitionValues();
				for(int k=0; k<partitionValues.size();k++){
					Node partitionValue = partitionValues.get(k);
					if(partitionValue!=null && currentEntity.getLabel().equalsIgnoreCase(partitionValue.getLabel())){						
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
	
	public NodeSet lookForSharablePositions(NodeSet currentEntities,int currentTime) {
		// TODO Auto-generated method stub	
		
		buildPriorityMap(currentEntities);
		
		while(currentEntities.size()>0){
			Node currentEntity = requestFlickeringNode(currentEntities);
			int currentEntityIndex = super.getEntitiesOverTime().indexOf(currentEntity.getLabel());
			NodePartition partition = stabilizePartition(requestUnstablePartition(currentEntity));
			markEntityAsStable(currentEntityIndex);
			super.getNodePartitions().add(partition);				
		}
			
		return currentEntities;
	}
	
	private Node requestFlickeringNode(NodeSet currentEntities) {
		// TODO Auto-generated method stub
		int maxFlickering = Integer.MIN_VALUE;
		int foundIndex = -1;
		Node priorityNode = null;		
		for(int i=0;i<gapMap.size();i++){
			if(gapMap.get(i)>maxFlickering){
				foundIndex = i;
				priorityNode = (Node)currentEntities.get(i);						
			}
		}
		
		gapMap.remove(foundIndex);
		currentEntities.remove(foundIndex);		
		return priorityNode;
	}

	private void buildPriorityMap(NodeSet currentEntities) {
		// TODO Auto-generated method stub		
		for(int i=0;i<currentEntities.size();i++){
			int flicker = calculateGap((Node)currentEntities.get(i));
			gapMap.add(flicker);
		}
		
	}

	private int calculateGap(Node currentEntity) {
		// TODO Auto-generated method stub		
		int row = super.getEntitiesOverTime().indexOf(currentEntity.getLabel());
		int gap = 0;
		for(int column=0;column<super.getNetworks().size();column++ ){
			if(super.getTimeMatrix()[row][column]!=1){
				gap++;
				for(int t=column;t<super.getNetworks().size();t++){
					if(super.getTimeMatrix()[row][column]!=0){
						column = t;
						break;
					}
				}
			}
		}	
		return gap;
	}	


	private NodePartition requestUnstablePartition(Node node) {
	// TODO Auto-generated method stub
		int row = super.getEntitiesOverTime().indexOf(node.getLabel());
		Vector<Node> partitionValues = new Vector<Node>();
		for(int column=0;column<super.getNetworks().size();column++){
			partitionValues.add((super.getTimeMatrix()[row][column]!=0) ? new Node("", node.getLabel()) : null);
		}			
		NodePartition partition = new NodePartition(""+super.getNodePartitions().size(), partitionValues);		
		return partition;
	}


	private NodePartition stabilizePartition(NodePartition partition) {
		// TODO Auto-generated method stub
		for (int i=0;i<super.getEntitiesOverTime().size();i++){		
			if(super.getEntitiesOverTime().get(i)!=null){
				NodePartition candidatePartition = requestUnstablePartition(new Node(null,super.getEntitiesOverTime().get(i)));
				if(!collision(partition,candidatePartition) && !stable(partition)){
					insertIntoPartition(partition,candidatePartition);
					markEntityAsStable(i);
				}	
			}			
		}	
		return partition;		
	}



	private void insertIntoPartition(NodePartition partition, NodePartition candidatePartition) {
		// TODO Auto-generated method stub		
		for(int i=0;i<super.getNetworks().size();i++){
			if(partition.getPartitionValues().get(i)==null){
				partition.getPartitionValues().setElementAt(candidatePartition.getPartitionValues().get(i), i);				
			}
		}
	}


	private boolean stable(NodePartition partition) {
		// TODO Auto-generated method stub
		for(int i=0;i<super.getNetworks().size();i++){
			if(partition.getPartitionValues().get(i)==null)
				return false;
		}		
		return true;
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
	
	private void markEntityAsStable(int entityIndex){
		super.getEntitiesOverTime().setElementAt(null, entityIndex);
	}
}
