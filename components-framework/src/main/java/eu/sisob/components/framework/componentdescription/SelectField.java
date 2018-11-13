package eu.sisob.components.framework.componentdescription;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class SelectField extends Field {

    @SuppressWarnings("unchecked")
    public SelectField(String label,String rightLabel, String name, boolean required, String... choices) {
        super(label, rightLabel,name, required, "select" );
        JSONArray selection = new JSONArray();
        for (String choice : choices) {
            selection.add(choice);
        }
        this.setSelectValues(selection);
    }

    private SelectField(){
        super();
    }
    
    public SelectField(String label, String name, boolean required, String... choices) {
        this(label, null, name, required, choices);
    }

    public JSONArray getSelectValues() {
        return (JSONArray) this.get("selectValues");
    }
    
    public  static Field fromJSON(JSONObject obj) {
        SelectField f = new SelectField();
        fromJSONField(obj, f);
        if (obj.get("selectValues") != null) {
            f.setSelectValues( (JSONArray)obj.get("selectValues") );
        }
        return f;
    }

}
