package eu.sisob.components.sl;

import java.util.Stack;
import java.util.Vector;

import eu.sisob.api.visualization.format.graph.fields.Node;

public class SwimLaneNode {

	private Node node;
	private boolean visited;	
	private boolean rootNode;
	private Stack<SwimLaneNode> longestPath;
	private Vector<SwimLaneNode> neighbors;
	
	
	public SwimLaneNode (Node node){
		this.node = node;
		this.visited = false;
		this.rootNode = false;
		this.longestPath = new Stack<SwimLaneNode>();
		this.neighbors = new Vector<SwimLaneNode>();		
		
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	@Override
	public String toString(){
		
		return new String(this.node.getId()+ "-" +this.node.getLabel()+ ":" +this.getLongestPath().size());
	}

	public Stack<SwimLaneNode> getLongestPath() {
		return longestPath;
	}

	public void setLongestPath(Stack<SwimLaneNode> longestPath) {
		this.longestPath = longestPath;
	}

	public Vector<SwimLaneNode> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(Vector<SwimLaneNode> neighbors) {
		this.neighbors = neighbors;
	}

	public boolean isRootNode() {
		return rootNode;
	}

	public void setRootNode(boolean rootNode) {
		this.rootNode = rootNode;
	}
}
