package eu.sisob.api.visualization.format.graph;

import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONObject;

import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;


public class Dataset extends JSONObject{

	private static final long serialVersionUID = 6589675955048745089L;

	public Dataset (){		
		super();

	}

	@SuppressWarnings("unchecked")
	public Dataset(NodeSet nodeSet, EdgeSet edgeSet){
		super();
		super.put("nodes",nodeSet);
		super.put("edges",edgeSet);
		
	}	
	
	
	public NodeSet getNodeSet() {
		Object value = super.get("nodes");
		if(value!=null)
			return (NodeSet)value;
		return null;
	}

	@SuppressWarnings("unchecked")
	public void setNodeSet(NodeSet nodeSet) {
		super.put("nodes",nodeSet);		
	}

	public EdgeSet getEdgeSet() {
		Object value = super.get("edges");
		if(value!=null)
			return (EdgeSet)value;
		return null;
	}

	@SuppressWarnings("unchecked")
	public void setEdgeSet(EdgeSet edgeSet) {
		super.put("edges", edgeSet);
	}

	@SuppressWarnings("unchecked")
	public JSONObject getDataSetAsJSON() {
		JSONObject instance = new JSONObject();
		Set <String> keys = super.keySet();		
		Iterator <String> keyIterator = keys.iterator();
		while(keyIterator.hasNext()){
			String key = keyIterator.next();
			Object value = super.get(key);
			instance.put(key, value);
		}		
		return instance;
	}

	@SuppressWarnings("unchecked")
	public void setData(JSONObject data) {
		Set<String> keys = data.keySet();
		Iterator<String> keyIterator = keys.iterator();		
		while(keyIterator.hasNext()){
		     String key = keyIterator.next();
		     Object value = data.get(key);
		     super.put(key, value);
		}
	}	
	
}
