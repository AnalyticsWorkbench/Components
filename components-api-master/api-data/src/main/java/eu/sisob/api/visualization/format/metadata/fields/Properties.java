package eu.sisob.api.visualization.format.metadata.fields;

import java.util.ArrayList;
import java.util.LinkedList;

import org.json.simple.JSONArray;

public class Properties extends JSONArray {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5348703425699372194L;

	public Properties() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	public void addProperty(Property p) {
		super.add(p);
	}

	public void removeProperty(Property p) {
		super.remove(p);
	}
	
	public void removePropertyByKey(String propertyKey){
		LinkedList<Property> deleteList = new LinkedList<Property>();
		for (Object object : this) {
			Property p = (Property) object;
			if (propertyKey.equals(p.getPropertyKey())){
				deleteList.add(p);
			}
		}
		for (Property p : deleteList) {
			super.remove(p);
		}
	}
	
	public boolean containsPropertyByKey(String propertyKey) {
            
		for (Object object : this) {
			Property p = (Property) object;
			if (propertyKey.equals(p.getPropertyKey())){
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Property> getPropertyByKey(String propertyKey) {
		LinkedList<Property> tmp = new LinkedList<Property>();
		for (Object object : this) {
			Property p = (Property) object;
			if (propertyKey.equals(p.getPropertyKey())){
				tmp.add(p);
			}
		}
		return new ArrayList<>(tmp);
	}

	@SuppressWarnings("unchecked")
	public JSONArray getPropertySet() {
		JSONArray instance = new JSONArray();
		for (int i = 0; i < super.size(); i++) {
			instance.add(super.get(i));
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public void setPropertySet(JSONArray propertySet) {
		super.clear();
		for (Object object : propertySet) {
			super.add(object);
		}
	}

}
