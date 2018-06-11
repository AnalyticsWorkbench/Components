package eu.sisob.api.visualization.technique.graph.fgl.util;

import java.util.Vector;

import eu.sisob.api.visualization.format.graph.fields.Edge;

public class EdgePartition {

	private String partitionID;
	private Vector <Edge> partitionValues;
	
	public EdgePartition(String partitionID){		
		this.partitionID = partitionID;
		partitionValues = new Vector<Edge>();
	}
	
	public void addValue(Edge Edge){
	  this.partitionValues.add(Edge)	;
	}	
	
	public void removeValue(Edge Edge){
		this.partitionValues.remove(Edge);
	}

	public Vector <Edge> getPartitionValues() {
		return partitionValues;
	}

	public void setPartitionValues(Vector <Edge> partitionValues) {
		this.partitionValues = partitionValues;
	}

	public String getPartitionID() {
		return partitionID;
	}

	public void setPartitionID(String partitionID) {
		this.partitionID = partitionID;
	}
	
}
