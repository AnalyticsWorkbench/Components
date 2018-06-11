package eu.sisob.api.visualization.technique.graph.fgl.util;

import java.util.Vector;

import eu.sisob.api.visualization.format.graph.fields.Node;

public class NodePartition {

	private String partitionID;	
	private Vector <Node> partitionValues;	
	private int averageActiveDegree;
	
	public NodePartition(String partitionID){		
		this.partitionID = partitionID;		
		this.partitionValues = new Vector<Node>();
		
	}
	
	public NodePartition(String partitionID,Vector <Node> partitionValues){		
		this.partitionID = partitionID;		
		this.partitionValues =partitionValues;		
	}
	
	public void addValue(Node node){
	  this.partitionValues.add(node);
	}	
	
	public void removeValue(Node node){
		this.partitionValues.remove(node);
	}	

	public Vector <Node> getPartitionValues() {
		return partitionValues;
	}

	public void setPartitionValues(Vector <Node> partitionValues) {
		this.partitionValues = partitionValues;
	}

	public String getPartitionID() {
		return partitionID;
	}

	public void setPartitionID(String partitionID) {
		this.partitionID = partitionID;
	}

	public int getAverageActiveDegree() {
		return averageActiveDegree;
	}

	public void setAverageActiveDegree(int averageActiveDegree) {
		this.averageActiveDegree = averageActiveDegree;
	}	
	
}
