package eu.sisob.components.fgl.stabilization;

import java.util.Vector;

import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.technique.graph.fgl.util.NodePartition;

public class GradientStabilization extends DefaultStabilization{

	private Vector<Double> stabilityVector;
	
	public GradientStabilization(){
		super();
		stabilityVector = new Vector<Double>();
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
		
		for(int i=0;i<super.getEntitiesOverTime().size();i++){
			double gradient = 0;
			double lifetime  = 0;
			
			for(int j=0;j<super.getNetworks().size();j++){
				if(super.getTimeMatrix()[i][j]!=-1){
					lifetime++;
				}
				
				if(j+1<super.getNetworks().size()){
					int currentDegree = (super.getTimeMatrix() [i][j]!=-1) ? super.getTimeMatrix() [i][j] : 0;
					int futureDegree =  (super.getTimeMatrix() [i][j+1]!=-1) ? super.getTimeMatrix() [i][j+1] : 0;
					gradient += Math.abs(currentDegree-futureDegree);
				}				
			}
			double stability = gradient / lifetime;
			stabilityVector.add(stability);
		}
		
	}
	
	@Override
	public NodeSet lookForSharablePositions(NodeSet currentEntities,int currentTime)  {
		
		for(int i=0;i<currentEntities.getValues().size();i++){
			Node currentEntity = currentEntities.getValues().get(i);
			for(int j=0;j<super.getNodePartitions().size();j++){
				NodePartition partition = super.getNodePartitions().get(i);
				if(canShareThePartition(currentEntity, partition.getPartitionValues()) && isStabilityImproved(currentEntity,partition.getPartitionValues())){
					partition.getPartitionValues().add(currentEntity);
					currentEntities.remove(i);
					stabilityVector.remove(i);
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

	private boolean isStabilityImproved(Node currentEntity, Vector<Node> partitionValues) {
		// TODO Auto-generated method stub		
		
		double currentPartitionStability = calculatePartitionStability(null, partitionValues);		
		double simulatedStability = calculatePartitionStability(currentEntity, partitionValues);
		
		if(simulatedStability>currentPartitionStability)
			return true;
				
		return false;
	}	
	

	private double calculatePartitionStability(Node currentEntity, Vector<Node> partitionValues) {
		// TODO Auto-generated method stub
		double gradient = 0;
		double lifetime = 0;
		
		for(int i=0;i<partitionValues.size();i++){
			int entityIndex = super.getEntitiesOverTime().indexOf(partitionValues.get(i).getLabel());
			for(int j=0;j<super.getNetworks().size();j++){								
				if(super.getTimeMatrix()[entityIndex][j]!=-1){
					lifetime++;
				}
				
				if(j+1<super.getNetworks().size()){
					int currentDegree = (super.getTimeMatrix() [entityIndex][j]!=-1)   ? super.getTimeMatrix() [entityIndex][j] : 0;
					int futureDegree =  (super.getTimeMatrix() [entityIndex][j+1]!=-1) ? super.getTimeMatrix() [entityIndex][j+1] : 0;
					gradient += Math.abs(currentDegree-futureDegree);
				}
			}	
		}		
		
		if(currentEntity!=null){
			int entityIndex = super.getEntitiesOverTime().indexOf(currentEntity.getLabel());
			for(int j=0;j<super.getNetworks().size();j++){								
				if(super.getTimeMatrix()[entityIndex][j]!=-1){
					lifetime++;
				}
				
				if(j+1<super.getNetworks().size()){
					int currentDegree = (super.getTimeMatrix() [entityIndex][j]!=-1)   ? super.getTimeMatrix() [entityIndex][j] : 0;
					int futureDegree =  (super.getTimeMatrix() [entityIndex][j+1]!=-1) ? super.getTimeMatrix() [entityIndex][j+1] : 0;
					gradient += Math.abs(currentDegree-futureDegree);
				}
			}	
		}
		
		return gradient / lifetime;
	}
	

}
