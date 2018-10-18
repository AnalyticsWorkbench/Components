package eu.sisob.api.visualization.format.graph.fields;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Edge extends JSONObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4212465720568269866L;

	public Edge() {
		super();
	}

	@SuppressWarnings("unchecked")
	public Edge(String id, String source, String target) {
		super();
		super.put("id", id);
		super.put("source", source);
		super.put("target", target);
	}

	// time methods
	@SuppressWarnings("unchecked")
	public void addTimeAppearance(String time) {

		Object object = super.get("timeappearance");
		if (object != null && object instanceof JSONArray) {
			JSONArray timeAppearance = (JSONArray) object;
			timeAppearance.add(time.replaceAll("\"", ""));
			super.put("timeappearance", timeAppearance);
		} else {
			JSONArray timeAppearance = new JSONArray();
			timeAppearance.add(time.replaceAll("\"", ""));
			super.put("timeappearance", timeAppearance);
		}

	}

	/**
	 * 
	 * @return the timeappearance-object, containing an interval
	 *         ({from:...,to:...}), or null if timeappearance is an array or not
	 *         set
	 */
	public JSONObject getTimeappearanceObject() {
		Object object = super.get("timeappearance");
		if (object != null && object instanceof JSONObject) {
			JSONObject timeAppearance = (JSONObject) object;
			return timeAppearance;
		}
		return null;
	}

	/**
	 * 
	 * @return the array of discrete points in time or null, if timeappearance
	 *         is an object or not set
	 */
	public JSONArray getTimeAppearance() {
		Object object = super.get("timeappearance");
		if (object != null && object instanceof JSONArray) {
			JSONArray timeAppearance = (JSONArray) object;
			return timeAppearance;
		}
		return null;
	}

	/**
	 * removes a single discrete point in time out of the timeappearance array.
	 * Works only if timeappearance is an array.
	 * 
	 * @param time
	 */
	@SuppressWarnings("unchecked")
	public void removeTimeAppearance(String time) {
		Object object = super.get("timeappearance");
		if (object != null && object instanceof JSONArray) {
			JSONArray timeAppearance = (JSONArray) object;
			timeAppearance.remove(time);
			super.put("timeappearance", timeAppearance);
		}
	}

	// new time methods
	public Vector<String> getTimeAppearanceVector() {
		Object object = super.get("timeappearance");
		if (object != null && object instanceof JSONArray) {
			JSONArray timeAppearance = (JSONArray) object;
			Vector<String> timeA = new Vector<String>();
			for (Object time : timeAppearance) {
				timeA.add(time.toString());
			}
			return timeA;
		}
		return null;
	}

	/**
	 * 
	 * @param timeAppearance
	 *            JSONObject {from: ..., to: ...} for time interval
	 */
	@SuppressWarnings("unchecked")
	public void setTimeAppearance(JSONObject timeAppearance) {
		super.put("timeappearance", timeAppearance);
	}

	/**
	 * 
	 * @param values
	 *            vector of discrete points in time
	 */
	@SuppressWarnings("unchecked")
	public void setTimeAppearance(Vector<String> values) {
		Object object = super.get("timeappearance");
		if (object != null && object instanceof JSONArray) {
			JSONArray timeAppearance = (JSONArray) object;
			timeAppearance.clear();
			for (String time : values)
				timeAppearance.add(time);

			super.put("timeappearance", timeAppearance);
		} else {
			JSONArray timeAppearance = new JSONArray();
			for (String time : values)
				timeAppearance.add(time);

			super.put("timeappearance", timeAppearance);
		}
	}

	/**
	 * 
	 * @param values
	 *            array of discrete points in time
	 */
	@SuppressWarnings("unchecked")
	public void setTimeAppearance(JSONArray values) {
		Object object = super.get("timeappearance");
		if (object != null && object instanceof JSONArray) {
			JSONArray timeAppearance = (JSONArray) object;
			timeAppearance.clear();
			for (Object time : values)
				timeAppearance.add(time);

			super.put("timeappearance", timeAppearance);
		} else {
			JSONArray timeAppearance = new JSONArray();
			super.put("timeappearance", timeAppearance);
		}
	}

	// property methods
	@SuppressWarnings("unchecked")
	public void addProperty(String key, String value) {
		super.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public void addProperty(String key, JSONArray values) {
		super.put(key, values);
	}

	@SuppressWarnings("unchecked")
	public void addProperty(String key, Vector<String> values) {
		JSONArray array = new JSONArray();
		for (String value : values) {
			array.add(value);
		}
		super.put(key, array);
	}

	public String getProperty(String key) {
		Object value = super.get(key);
		if (value != null)
			return value.toString();
		return null;

	}

	public void removeProperty(String name) {
		super.remove(name);
	}

	// measure methods
	@SuppressWarnings("unchecked")
	public void addMeasure(String key, String value) {
		super.put(key, value);
	}

	public String getMeasure(String key) {
		Object value = super.get(key);
		if (value != null)
			return value.toString();
		return null;
	}

	public void removeMeasure(String key) {
		super.remove(key);
	}

	// id and label methods
	public String getId() {
		Object value = super.get("id");
		if (value != null)
			return value.toString();
		return null;
	}

	public String getLabel() {
		Object value = super.get("label");
		if (value != null)
			return value.toString();
		return null;
	}

	@SuppressWarnings("unchecked")
	public void setId(String id) {
		super.put("id", id);
	}

	@SuppressWarnings("unchecked")
	public void setLabel(String label) {
		super.put("label", label);
	}

	// source and target methods
	public String getSource() {
		Object value = super.get("source");
		if (value != null)
			return value.toString();
		return null;
	}

	@SuppressWarnings("unchecked")
	public void setSource(String source) {
		super.put("source", source);
	}

	public String getTarget() {
		Object value = super.get("target");
		if (value != null)
			return value.toString();
		return null;
	}

	@SuppressWarnings("unchecked")
	public void setTarget(String target) {
		super.put("target", target);
	}

	// backwards compatibility
	@SuppressWarnings("unchecked")
	public Set<String> getPropertyKeys() {
		return super.keySet();
	}

	@SuppressWarnings("unchecked")
	public JSONObject getEdgeAsJSON() {
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
	public void setEdgeAsJSON(JSONObject edge) {
		Set<String> keys = edge.keySet();
		Iterator<String> keyIterator = keys.iterator();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			Object value = edge.get(key);
			super.put(key, value);
		}
	}

}
