package eu.sisob.components.framework.componentdescription;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public abstract class Terminal extends JSONObject {

	@SuppressWarnings("unchecked")
	protected Terminal(String name, String label) {
		this.put("name", name);
		this.put("label", label);
	}

	public String getName() {
		return this.get("name").toString();
	}
	@SuppressWarnings("unchecked")
	protected void setName(String name) {
		this.put("name", name);
	}

	public String getLabel() {
		return this.get("label").toString();
	}
	@SuppressWarnings("unchecked")
	protected void setLabel(String label) {
		this.put("label", label);
	}
}
