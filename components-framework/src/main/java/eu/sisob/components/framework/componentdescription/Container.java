package eu.sisob.components.framework.componentdescription;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class Container extends JSONObject {

    @SuppressWarnings("unused")
    private Container() {
        // intentionally left blank
    }

    @SuppressWarnings("unchecked")
    public Container(String shortDescription, String longDescription, JSONArray inputs, JSONArray outputs, JSONArray fields) {
        this(inputs, outputs, fields);
        if (shortDescription != null) {
            this.put("legend", shortDescription);
        }
        if (longDescription != null) {
            this.put("descriptionText", longDescription);
        }
    }

    @SuppressWarnings("unchecked")
    public Container(JSONArray inputs, JSONArray outputs, JSONArray fields) {
        this.put("xtype", "WireIt.SISOBContainer");
        if (inputs != null) {
            this.put("inputs", inputs);
        }
        if (outputs != null) {
            this.put("outputs", outputs);
        }
        if (fields != null) {
            this.put("fields", fields);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Container(String shortDescription, String longDescription, JSONArray inputs, JSONArray outputs,
			JSONArray fields, JSONObject form) {
		this(shortDescription, longDescription, inputs, outputs, fields);
		this.put("form", form);
	}
    
    @SuppressWarnings("unchecked")
    public Container(String shortDescription, String longDescription, JSONArray inputs, JSONArray outputs,
			JSONArray fields, JSONObject form, String jsUpdateForm, String jsTransformMeta) {
		this(shortDescription, longDescription, inputs, outputs, fields, form);
        this.put("js_transform_meta", jsTransformMeta);
		this.put("js_update_form", jsUpdateForm);

	}

	public String getXType() {
        return get("xtype").toString();
    }

    public JSONArray getFields() {
        return (JSONArray) this.get("fields");
    }

    public JSONArray getInputs() {
        return (JSONArray) this.get("inputs");
    }

    public JSONArray getOutputs() {
        return (JSONArray) this.get("outputs");
    }
    
    public JSONObject getForm() {
        return (JSONObject) this.get("form");
    }

    /**
     * Get the short description <br/>
     * Is "legend" in JSON
     * 
     * @return
     */
    public String getShortDescription() {
        Object description = this.get("legend");
        if (description == null) {
            return null;
        } else {
            return description.toString();
        }
    }

    /**
     * Get the long description <br/>
     * Is "descriptionText" in JSON
     * 
     * @return
     */
    public String getLongDescription() {
        Object description = this.get("descriptionText");
        if (description == null) {
            return null;
        } else {
            return description.toString();
        }
    }

    @SuppressWarnings("unchecked")
    public static Container fromJSON(JSONObject json) {
        JSONArray inputs, outputs, fields, rawInputs, rawOutputs, rawFields;
        JSONObject form, rawForm;

        rawInputs = (JSONArray) json.get("inputs");
        if (rawInputs != null) {
            inputs = new JSONArray();
            for (Object obj : rawInputs) {
                JSONObject input = Input.fromJSON((JSONObject) obj);
                inputs.add(input);
            }
        } else {
            inputs = null;
        }

        rawOutputs = (JSONArray) json.get("outputs");

        if (rawOutputs != null) {
            outputs = new JSONArray();
            for (Object obj : rawOutputs) {
                JSONObject output = Output.fromJSON((JSONObject) obj);
                outputs.add(output);
            }
        } else {
            outputs = null;
        }

        rawFields = (JSONArray) json.get("fields");
        if (rawFields != null) {
            fields = new JSONArray();
            for (Object obj : rawFields) {
                JSONObject field = Field.fromJSON((JSONObject) obj);
                fields.add(field);
            }
        } else {
            fields = null;
        }
        
        rawForm = (JSONObject) json.get("form");
        if (rawForm!= null) {
            form = rawForm;
        } else {
            form = null;
        }
    /**
     *
     * js_transform_meta if exist inside transformation of each module then we use it !
     *
    * */
        Container newContainer = new Container(
        	json.get("legend") != null ? json.get("legend").toString() : null, 
        	json.get("descriptionText") != null ? json.get("descriptionText").toString() : null, 
        	inputs,
        	outputs,
        	fields,
        	form,
        	json.get("js_update_form") != null ? json.get("js_update_form").toString() : null, 
        	json.get("js_transform_meta") != null ? json.get("js_transform_meta").toString() : null 
    	);
        return newContainer;
    }
}
