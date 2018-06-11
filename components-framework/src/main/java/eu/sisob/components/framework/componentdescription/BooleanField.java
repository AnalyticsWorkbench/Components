package eu.sisob.components.framework.componentdescription;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class BooleanField extends Field {

    public BooleanField(String label, String rightLabel, String name, boolean required, Boolean value) {
        super(label, rightLabel, name, required, "boolean");
        this.setValue(value);
    }
    private BooleanField(){
        super();
    }
    public BooleanField(String label, String name, boolean required, Boolean value) {
        this(label, null, name, required, value);
    }

    public BooleanField(String label, String name, boolean required) {
        this(label, name, required, null);
    }

    public Boolean getValue() {
        return (Boolean) this.get("value");
    }
    public  static Field fromJSON(JSONObject obj) {
        BooleanField f = new BooleanField();
        fromJSONField(obj, f);
        if (obj.get("selectValues") != null) {
            f.setValue( (Boolean)obj.get("value") );
        }
        return f;
    }

}
