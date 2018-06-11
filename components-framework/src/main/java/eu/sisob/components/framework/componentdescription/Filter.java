package eu.sisob.components.framework.componentdescription;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class Filter extends JSONObject {
	public Filter(String name, String category, String shortDescription,
			String longDescription, JSONArray inputs, JSONArray outputs,
			JSONArray fields) {
		this(name, category,new Container(shortDescription, longDescription, inputs, outputs, fields));
	}
	
	@SuppressWarnings("unchecked")
	public Filter(String name, String category, Container container) {
		this.put("name", name);
		this.put("category", category);
		this.put("container", container);
	}
        
        public Filter(String name, String category, String shortDescription,
			String longDescription, JSONArray inputs, JSONArray outputs,
			JSONArray fields, JSONObject form, String jsUpdateForm, String jsTransformMeta) {
		this(name, category, new Container(shortDescription, longDescription, inputs, outputs, fields, form, jsUpdateForm, jsTransformMeta));
	}

	public String getCategory() {
		return this.get("category").toString();
	}

	public String getName() {
		return this.get("name").toString();
	}

	public Container getContainer() {
			return (Container)this.get("container");
	}
	
	@SuppressWarnings("unchecked")
	protected void setCategory(String category) {
		this.put("category", category);
	}
	@SuppressWarnings("unchecked")
	protected void setName(String name) {
		this.put("name", name);
	}
	
	public static Filter fromJSON(JSONObject json) {
	    Container container = Container.fromJSON((JSONObject)json.get("container"));
	    Filter newFilter = new Filter(json.get("name").toString(), json.get("category").toString(), container);
	    return newFilter;
	}
	
}
