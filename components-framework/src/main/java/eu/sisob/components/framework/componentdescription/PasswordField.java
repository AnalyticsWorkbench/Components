package eu.sisob.components.framework.componentdescription;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class PasswordField extends Field {

	    public PasswordField(String label, String rightLabel, String name, boolean required) {
	        super(label, rightLabel, name, required, "password");
	    }

	    private PasswordField() {
	        super(null,null,null,false,"password");
	    }

	    public PasswordField(String label, String name, boolean required, String value) {
	    	super(label, null, name, required, "password");
	    }

	    public PasswordField(String label, String name, boolean required) {
	    	super(label, null, name, required, "password");
	    }

	    public String getValue() {
	        return this.get("value").toString();
	    }

	    public static Field fromJSON(JSONObject obj) {
	    	PasswordField f = new PasswordField();
	        fromJSONField(obj, f);
	        if (obj.get("selectValues") != null) {
	            f.setValue((String) obj.get("value"));
	        }
	        return f;
	    }

	

}
