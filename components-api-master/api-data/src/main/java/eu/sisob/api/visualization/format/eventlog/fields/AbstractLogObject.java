package eu.sisob.api.visualization.format.eventlog.fields;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * 
 * @author goehnert
 *
 */
public abstract class AbstractLogObject extends JSONObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7493134270053920311L;

	public String getId() {
		checkId();
		return this.get("id").toString();
	}

	@SuppressWarnings("unchecked")
	public void setId(String id) {
		if (id != null) {
			this.put("id", id);
		} else {
			throw new IllegalArgumentException("id must not be set to null");
		}
	}

	@SuppressWarnings("unchecked")
	public void addProperty(String key, String value) {
		this.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public void addProperty(String key, JSONArray values) {
		this.put(key, values);
	}

	@SuppressWarnings("unchecked")
	public void addProperty(String key, Vector<String> values) {
		JSONArray array = new JSONArray();
		for (String value : values) {
			array.add(value);
		}
		this.put(key, array);
	}

	public String getProperty(String key) {
		Object value = this.get(key);
		if (value != null)
			return value.toString();
		return null;

	}

	public void removeProperty(String name) {
		this.remove(name);
	}

	@SuppressWarnings("unchecked")
	public void setAsJSON(JSONObject jsonObject) {
		Set<String> keys = jsonObject.keySet();
		Iterator<String> keyIterator = keys.iterator();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			Object value = jsonObject.get(key);
			this.put(key, value);
		}
		checkId();
	}

	public String getAsJSON() {
		return this.toJSONString();
	}

	protected void checkId() {
		if (this.get("id") == null) {
			generateId();
		}
	}

	@SuppressWarnings("unchecked")
	protected void generateId() {
		String id = UUID.randomUUID().toString();
		this.put("id", id);
	}

}
