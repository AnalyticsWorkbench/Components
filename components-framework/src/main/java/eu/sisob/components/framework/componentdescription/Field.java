package eu.sisob.components.framework.componentdescription;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public abstract class Field extends JSONObject {

    @SuppressWarnings("unchecked")
    protected Field(String label,  String rightLabel, String name, boolean required, String type) {
        if ( label != null ) {
            this.put("label", label);
        }
        if ( name != null ) {
            this.put("name", name);
        }
        if ( type != null ) {
            this.put("type", type);
        }
        this.put("required", new Boolean(required));
        if ( rightLabel != null ) {
            this.put("rightLabel", rightLabel);
        }
    }

    protected Field(){
        super();
    }
    
    protected Field(String label, String name, boolean required, String type) {
        this(label,null,name,required,type);
    }

    public String getLabel() {
        return this.get("label").toString();
    }

    public String getName() {
        return this.get("name").toString();
    }

    public String getType() {
        return this.get("type").toString();
    }

    public String getRightLabel() {
        return this.get("rightLabel").toString();
    }

    public boolean isRequired() {
        return ((Boolean) this.get("required")).booleanValue();
    }

    @SuppressWarnings("unchecked")
    protected void setValue(Object value) {
        if (value != null) {
            this.put("value", value);
        }
    }

    @SuppressWarnings("unchecked")
    protected void setSelectValues(JSONArray selection) {
        this.put("selectValues", selection);
    }

    @SuppressWarnings("unchecked")
    protected static Field fromJSONField(JSONObject obj, Field f){
        Object label = obj.get("label");
        if ( label != null ) {
            f.put("label", label);
        }
        Object name = obj.get("name");
        if ( name != null ) {
            f.put("name", name);
        }
        
        f.put("required", (Boolean) obj.get("required"));
        
        Object rightLabel = obj.get("rightLabel");
        if ( rightLabel != null ) {
            f.put("rightLabel", rightLabel);
        }
        
        return f;
        
    }
    
    
    public  static Field fromJSON(JSONObject obj) {
       Field newField;
       
       Object type = obj.get("type");
       if  ( "boolean".equals(type) ) {
           newField = BooleanField.fromJSON(obj);
       } else if  ( "string".equals(type) ) {
           newField = StringField.fromJSON(obj);
       } else if  ( "int".equals(type) ){
           newField = IntField.fromJSON(obj);
       } else if  ( "select".equals(type) ) {
           newField = SelectField.fromJSON(obj);
       } else if  ( "password".equals(type) ) {
           newField = PasswordField.fromJSON(obj);
       } else {
           System.err.println("Unknown type:"+type);
           return null;
       }
        return newField;
    }
    
    

}
