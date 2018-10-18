package eu.sisob.api.visualization.format.graph.fields;

import java.util.Vector;

import org.json.simple.JSONArray;


public class NodeSet extends JSONArray{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -576081104011871364L;

	public NodeSet(){		
		super();
	}
	
	@SuppressWarnings("unchecked")
	public void addNode(Node n){
		super.add(n);				
	}
	
	public void removeNode(Node n){		
		super.remove(n);
	}	
	
	public void clearNodeSet(){
		super.clear();		
	}

	@SuppressWarnings("unchecked")
	public JSONArray getNodeSetAsJSON(){
		 JSONArray instance = new JSONArray();
		 for(int i=0;i<super.size();i++){
			 instance.add(super.get(i));
		 }
		 return instance;
	}
	
	public  Vector <Node> getValues(){
		 Vector<Node> instance = new Vector<Node>();
		 for(int i=0;i<super.size();i++){
			 instance.add((Node)super.get(i));
		 }
		 return instance;
	}
	
	@SuppressWarnings("unchecked")
	public void setValues(Vector<Node> nodeSet) {
		super.clear();
		for(Node node:nodeSet){
			super.add(node);
		}
	}

}
