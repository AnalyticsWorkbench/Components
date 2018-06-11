package eu.sisob.components.framework.componentdescription;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class Output extends Terminal{

	public Output(String name, String label) {
		super(name, label);
	}

    public static JSONObject fromJSON(JSONObject obj) {
        Output newOutput = new Output(obj.get("name")!=null?obj.get("name").toString():null, obj.get("label")!=null?obj.get("label").toString():null);
        return newOutput;
    }

}
