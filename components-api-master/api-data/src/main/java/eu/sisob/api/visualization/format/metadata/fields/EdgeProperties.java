package eu.sisob.api.visualization.format.metadata.fields;

import org.json.simple.JSONArray;

public class EdgeProperties extends Properties {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4031723631174635716L;

	public static final String BELONGS_TO_MAIN_PATH = "mp";
	
	public EdgeProperties(){
		super();
	}	
	
	public JSONArray getEdgePropertySet() {
		return super.getPropertySet();
	}

	public void setEdgePropertySet(JSONArray edgePropertySet) {
		super.setPropertySet(edgePropertySet);
	}	
	
}
