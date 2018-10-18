package eu.sisob.api.visualization.format.metadata.fields;

import org.json.simple.JSONArray;

public class NodeProperties extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1033346961966280793L;
	
	public static final String CLUSTER = "clusters";
	
	public NodeProperties() {
		super();
	}

	public JSONArray getNodePropertySet() {
		return super.getPropertySet();
	}

	public void setNodePropertySet(JSONArray nodePropertySet) {
		super.setPropertySet(nodePropertySet);
	}
}
