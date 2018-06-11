package eu.sisob.components.framework.componentdescription;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class IntField extends Field {

    public IntField(String label, String rightLabel, String name, boolean required, Integer value) {
        super(label, rightLabel, name, required, "int");
        this.setValue(value);
    }

    private IntField() {
        super();
    }

    public IntField(String label, String name, boolean required, Integer value) {
        this(label, null, name, required, value);
    }

    public IntField(String label, String name, boolean required) {
        this(label, name, required, null);
    }

    public Integer getValue() {
        return (Integer) this.get("value");
    }

    public static Field fromJSON(JSONObject obj) {
        IntField f = new IntField();
        fromJSONField(obj, f);
        if (obj.get("selectValues") != null) {
            f.setValue((Integer) obj.get("value"));
        }
        return f;
    }
}
