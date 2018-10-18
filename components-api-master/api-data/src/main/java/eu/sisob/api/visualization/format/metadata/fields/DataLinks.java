package eu.sisob.api.visualization.format.metadata.fields;

import org.json.simple.JSONArray;

public class DataLinks extends JSONArray{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8092761378932658260L;

	public DataLinks(){
		super();
	}

	@SuppressWarnings("unchecked")
	public void addDataLink(Object o){
		super.add(o);
	}
	
	public void removeDataLink(Object o){
		super.remove(o);
	}

	@SuppressWarnings("unchecked")
	public JSONArray getDataSet() {
		 JSONArray instance = new JSONArray();
		 for(int i=0;i<super.size();i++){
			 instance.add(super.get(i));
		 }
		 return instance;
	}

	@SuppressWarnings("unchecked")
	public void setDataSet(JSONArray dataLinks) {
		super.clear();
		for(Object object:dataLinks){
			super.add(object);
		}
	}
	
}
