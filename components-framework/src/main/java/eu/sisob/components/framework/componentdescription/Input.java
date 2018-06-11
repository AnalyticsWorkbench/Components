package eu.sisob.components.framework.componentdescription;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class Input extends Terminal{

	public Input(String name, String label) {
		super(name, label);
	}

    public static Input fromJSON(JSONObject obj) {
        Input newInput = new Input(obj.get("name")!=null?obj.get("name").toString():null, obj.get("label")!=null?obj.get("label").toString():null);
        return newInput;
    }

}
