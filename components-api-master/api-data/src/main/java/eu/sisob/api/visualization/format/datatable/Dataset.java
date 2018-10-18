package eu.sisob.api.visualization.format.datatable;



import java.util.Vector;

import org.json.simple.JSONArray;

import eu.sisob.api.visualization.format.datatable.fields.DataField;


public class Dataset extends JSONArray{
	
	private static final long serialVersionUID = 1759548549390864748L;

	public Dataset(){	
		super();				
	}
	
	@SuppressWarnings("unchecked")
	public void addDataField(DataField df){
		super.add(df);
	}
	
	public void removeDataField(DataField df){
		super.remove(df);
	}
	
	
	public void cleardataSet(){
		super.clear();
	}	

	@SuppressWarnings("unchecked")
	public JSONArray getDataSetAsJSON() {
		 JSONArray instance = new JSONArray();
		 for(int i=0;i<super.size();i++){
			 instance.add(super.get(i));
		 }
		 return instance;
	}

	public Vector<DataField> getDataSet() {
		Vector<DataField> instance = new Vector<DataField>();
		 for(int i=0;i<super.size();i++){
			 instance.add((DataField)super.get(i));
		 }
		 return instance;
	}

	@SuppressWarnings("unchecked")
	public void setDataSet(Vector<DataField> dataSet) {
		super.clear();
		for(DataField field:dataSet){
			super.add(field);
		}
	}	
	
}
