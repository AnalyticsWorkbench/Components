package eu.sisob.api.visualization.format.metadata;

import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import eu.sisob.api.visualization.format.metadata.fields.DataLinks;
import eu.sisob.api.visualization.format.metadata.fields.EdgeProperties;
import eu.sisob.api.visualization.format.metadata.fields.NodeProperties;
import eu.sisob.api.visualization.format.metadata.fields.Properties;
import eu.sisob.api.visualization.format.metadata.fields.Property;

public class Metadata extends JSONObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3543694675895389378L;
	
	public Metadata() {
		super();
	}

	@SuppressWarnings("unchecked")
	public Metadata(String title){
		super();
		super.put("title", title);
	}
	
	@SuppressWarnings("unchecked")
	public Metadata(String title, String description){
		this(title);
		super.put("description", description);
	}
	
	@SuppressWarnings("unchecked")
	public Metadata(String title, String directed,	int dataIndex) {
		this(title);
		super.put("directed", directed);
		super.put("dataindex", String.valueOf(dataIndex));
	}

	/**
	 * 
	 * @return the index in the datalinks field or -1 if not set
	 */
	public int getDataIndex(){
		String sValue = (String) super.get("dataindex");
		if (sValue != null && !sValue.isEmpty()){
			return Integer.parseInt(sValue);
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public void setDataIndex(int index){
		super.put("dataindex", String.valueOf(index));
	}

	public String getTitle() {
		Object value = super.get("title");
		if (value != null)
			return value.toString();

		return null;
	}

	@SuppressWarnings("unchecked")
	public void setTitle(String title) {
		super.put("title", title);
	}


	public String getNetworkType() {
		Object value = super.get("type");
		if (value != null)
			return value.toString();

		return null;
	}

	@SuppressWarnings("unchecked")
	public void setNetworkType(String nt) {
		super.put("type", nt);
	}
	

	
	public String isDirectedNetwork() {
		Object value = super.get("directed");
		if (value != null)
			return value.toString();

		return null;
	}

	@SuppressWarnings("unchecked")
	public void setDirectedNetwork(String directed) {
		super.put("directed", directed);
	}

	public DataLinks getDatalinks() {
		Object value = super.get("datalinks");
		if (value != null) {
			DataLinks dl = (DataLinks) value;
			return dl;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public void setDatalinks(DataLinks datalinks) {
		super.put("datalinks", datalinks);
	}
	
	public Properties getProperties() {
		Object value = super.get("properties");
		if (value != null) {
			try {
				Properties p = (Properties) value;
				return p;
			} catch (ClassCastException cce) {
				Properties props = new Properties();
				JSONArray op = (JSONArray) value;
				for (int i = 0; i < op.size(); i++) {
					Property p = new Property((JSONObject) op.get(i));
					props.addProperty(p);
				}
				return props;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public void setProperties(Properties properties) {
		super.put("properties", properties);
	}

	public NodeProperties getNodeproperties() {
		Object value = super.get("nodeproperties");
		if (value != null) {
			NodeProperties np = (NodeProperties) value;
			return np;
		}

		return null;

	}

	@SuppressWarnings("unchecked")
	public void setNodeproperties(NodeProperties nodeproperties) {
		super.put("nodeproperties", nodeproperties);
	}

	public EdgeProperties getEdgeproperties() {
		Object value = super.get("edgeproperties");
		if (value != null) {
			EdgeProperties ep = (EdgeProperties) value;
			return ep;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public void setEdgeproperties(EdgeProperties edgeproperties) {
		super.put("edgeproperties", edgeproperties);
	}
	

	public String getNetworkDescription() {
		Object value = super.get("description");
		if (value != null) {
			return value.toString();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public void setNetworkDescription(String networkDescription) {
		super.put("description", networkDescription);
	}
	
	@SuppressWarnings("unchecked")
	public void addNetworkInfo(String key, Object value) {
		super.put(key, value);
	}

	public void removeNetworkInfo(String key) {
		super.remove(key);
	}

	// kind of clone?
	@SuppressWarnings("unchecked")
	public JSONObject getMetadata() {
		JSONObject instance = new JSONObject();
		Set<String> keys = super.keySet();
		Iterator<String> keyIterator = keys.iterator();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			Object value = super.get(key);
			instance.put(key, value);
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public void setMetadata(JSONObject metadata) {
		Set<String> keys = metadata.keySet();
		Iterator<String> keyIterator = keys.iterator();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			Object value = metadata.get(key);
			super.put(key, value);
		}
	}
	
}
