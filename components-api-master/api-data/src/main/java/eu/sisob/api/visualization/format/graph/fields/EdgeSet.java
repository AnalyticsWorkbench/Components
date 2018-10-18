package eu.sisob.api.visualization.format.graph.fields;

import java.util.Vector;

import org.json.simple.JSONArray;


public class EdgeSet extends JSONArray {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5935896912250574900L;

	public EdgeSet(){
		super();		
	}	
	
	@SuppressWarnings("unchecked")
	public void addEdge(Edge e){
		super.add(e);			
	}
	
	public void removeEdge(Edge e){
		super.remove(e);				
	}
	
	
	public void clearEdgeSet(){
		super.clear();
	}

	@SuppressWarnings("unchecked")
	public JSONArray getEdgeSetAsJSON() {
		JSONArray instance = new JSONArray();
		for(int i=0;i<super.size();i++){
			instance.add(super.get(i));
		}
		return instance;
	}
	
	public Vector<Edge> getValues() {
		Vector<Edge> instance = new Vector<Edge>();
		 for(int i=0;i<super.size();i++){
			 instance.add((Edge)super.get(i));
		 }
		 return instance;
	}
	
	@SuppressWarnings("unchecked")
	public void setValues(Vector<Edge> edgeSet) {
		super.clear();
		for(Edge edge:edgeSet){
			super.add(edge);
		}
	}	
	
}
