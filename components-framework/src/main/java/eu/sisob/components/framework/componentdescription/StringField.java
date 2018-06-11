package eu.sisob.components.framework.componentdescription;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class StringField extends Field {

    public StringField(String label, String rightLabel, String name, boolean required, String value) {
        super(label, rightLabel, name, required, "string");
        super.setValue(value);
    }

    private StringField() {
        super();
    }

    public StringField(String label, String name, boolean required, String value) {
        this(label, null, name, required, value);
    }

    public StringField(String label, String name, boolean required) {
        this(label, name, required, null);
    }

    public String getValue() {
        return this.get("value").toString();
    }

    public static Field fromJSON(JSONObject obj) {
        StringField f = new StringField();
        fromJSONField(obj, f);
        if (obj.get("selectValues") != null) {
            f.setValue((String) obj.get("value"));
        }
        return f;
    }

}
