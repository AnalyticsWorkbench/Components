package eu.sisob.api.visualization.technique.graph.fgl.util;

import java.util.Vector;


public class ForesightedGraph {

	private Vector<NodePartition> nodePartitions;
	private Vector<EdgePartition> edgePartitions;	
	
	public ForesightedGraph(Vector<NodePartition> nodePartitions, Vector<EdgePartition> edgePartitions){
		this.nodePartitions = nodePartitions;
		this.edgePartitions = edgePartitions;
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
