package eu.sisob.components.fgl.stabilization;

import java.util.Vector;

import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.technique.graph.fgl.util.NodePartition;

public class DegreeStabilization extends DefaultStabilization{
	
	public DegreeStabilization(){
		super();		
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

	@Override
	public void fillNodePartitions() throws Exception {
		// TODO Auto-generated method stub
		for(int i=0;i<super.getNetworks().size();i++){
			SGFParser parser = new SGFParser();
			parser.setNetwork(super.getNetworks().get(i));
			parser.parse();
			
			NodeSet currentEntities = parser.getParsedNodeSet();
				currentEntities = lookForPreviouslyInsertedEntities(currentEntities);				
			if(currentEntities.size()>0)
				currentEntities = lookForSharablePositions(currentEntities,i);
			if(currentEntities.size()>0)
				currentEntities = increaseNumberOfPartitions(currentEntities);
		}	
		
		attachAverageDegreeToPartitions();		
	}
	
	@Override
	public NodeSet lookForSharablePositions(NodeSet currentEntities,int currentTime)  {
		// TODO Auto-generated method stub
		for(int i=0;i<currentEntities.getValues().size();i++){
			Node currentEntity = currentEntities.getValues().get(i);				
			int currentAverageDegree = averageDegree(currentEntity);
			for(int j=0;j<super.getNodePartitions().size();j++){
				NodePartition partition = super.getNodePartitions().get(j);
				int partitionAverageDegree = partitionAverageDegree(partition);
				int partitionAverageGradient = partitionAverageGradient(partition);
				if(canShareThePartition(currentEntity, partition.getPartitionValues()) && isToleranceInRange(currentAverageDegree,partitionAverageDegree,partitionAverageGradient)){
					partition.getPartitionValues().add(currentEntity);
					currentEntities.remove(i);
					i=i-1;
					break;
				}					
			}		
		}		
		return currentEntities;
	}

	@Override
	public boolean canShareThePartition(Node entity,Vector<Node> partitionValues) {
		// TODO Auto-generated method stub
		for(Node r:partitionValues){
			int a = super.getEntitiesOverTime().indexOf(entity.getLabel().trim());
			int b = super.getEntitiesOverTime().indexOf(r.getLabel().trim());
			
			if(canShare(a,b)!=true)
				return false;
		}

		return true;
	}
	
	@Override
	public boolean canShare(int node, int nodeOnPartition) {
		// TODO Auto-generated method stub
		for(int z=0;z<super.getNetworks().size();z++){
		      if((super.getTimeMatrix()[nodeOnPartition][z] !=-1) && (super.getTimeMatrix()[node][z]!=-1))
		    	  return false;
		}			
		return true;
	}

	private boolean isToleranceInRange(int currentAverageDegree, int partitionAverageDegree, int partitionAverageGradient) {
		// TODO Auto-generated method stub
		if((Math.abs(currentAverageDegree-partitionAverageDegree)) <= partitionAverageGradient)
			return true;
		
		return false;
	}

	private int averageDegree(Node node){
		int averageDegree = 0;
		int appearance = 0;
		int nodeIndex = super.getEntitiesOverTime().indexOf(node.getLabel());		
		for(int i=0;i<super.getNetworks().size();i++){
			if(super.getTimeMatrix()[nodeIndex][i]!=-1){
				appearance++;
				averageDegree+=super.getTimeMatrix()[nodeIndex][i];
			}
			
		}			
		return averageDegree/appearance;
	}	
	
	private int partitionAverageDegree(NodePartition partition){		
		int averageDegree = 0;	
		Vector<String> processedNodes = new Vector<String>();
		for(int i=0;i<partition.getPartitionValues().size();i++){
			Node node = partition.getPartitionValues().get(i);
			if(processedNodes.indexOf(node.getLabel())==-1){
				averageDegree += averageDegree(node);
				processedNodes.add(node.getLabel());
			}									
		}
		return averageDegree/partition.getPartitionValues().size();				
	}
	
	private int partitionAverageGradient(NodePartition partition){
		Vector<Integer> gradientVector = new Vector<Integer>();		
		Vector<String> processedNodes = new Vector<String>();
		for(int i=0;i<partition.getPartitionValues().size();i++){
			Node node = partition.getPartitionValues().get(i);
			if(processedNodes.indexOf(node.getLabel())==-1){
				int nodeIndex = super.getEntitiesOverTime().indexOf(node.getLabel());
				for(int j=0;j<super.getNetworks().size();j++){
					if(super.getTimeMatrix()[nodeIndex][j]!=-1){
						gradientVector.add(super.getTimeMatrix()[nodeIndex][j]);
						processedNodes.add(node.getLabel());
					}					
				}	
			}						
		}	
		
		int gradient = 0;
		for(int i=0;i<gradientVector.size();i++){
			if(i+1<gradientVector.size()){
				gradient+=Math.abs(gradientVector.get(i) - gradientVector.get(i+1));
			}
		}
		
		return gradient/gradientVector.size();
	}
	
	private void attachAverageDegreeToPartitions(){
		for(int i=0;i<super.getNodePartitions().size();i++){
			NodePartition partition = super.getNodePartitions().get(i);
			partition.setAverageActiveDegree(partitionAverageDegree(partition));
		}
	}
}
