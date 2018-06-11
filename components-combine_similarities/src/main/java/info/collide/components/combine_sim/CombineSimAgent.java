package info.collide.components.combine_sim;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.json.simple.JSONArray;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.api.parser.sisob.SDTParser;
import eu.sisob.api.visualization.format.datatable.Dataset;
import eu.sisob.api.visualization.format.datatable.fields.DataField;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

/**
 * @author doberstein
 */
public class CombineSimAgent extends Agent {
	
	protected Vector<JSONFile> data = null;
	private double sigma1 = 0;
	private double sigma2 = 0;
	
	

	public CombineSimAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
		

	this.sigma1 = Double.parseDouble(this.getFilterParameters().get(CombineSimManager.SIGMA1).toString());
	this.sigma2 = Double.parseDouble(this.getFilterParameters().get(CombineSimManager.SIGMA2).toString());
		
		
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		this.data = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
		uploadResults();
		indicateDone();
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		
		// get the two input datamessages (in_1; in_2)
		JsonObject dataMessage1 = dataMessages.get(0);
		JsonObject dataMessage2 = dataMessages.get(1);
		this.data = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage1.get("payload")));
		
		
		Vector <JSONFile> dataFiles1;
		Vector <JSONFile> dataFiles2;
		
		// get the payload from dataMessage1
		dataFiles1 = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage1.get("payload")));
		dataFiles2 = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage2.get("payload")));
		
		// parse dataFiles1 to .sdt format
		SDTParser sdtParser1 = new SDTParser();
		sdtParser1.setTabledata(dataFiles1.get(0));
		try {
			sdtParser1.parse();
		} catch (IllegalContentTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// parse dataFiles2 to .sdt format
		SDTParser sdtParser2 = new SDTParser();
		sdtParser2.setTabledata(dataFiles2.get(0));
		try {
			sdtParser2.parse();
		} catch (IllegalContentTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// get the parsed data as dataset
		Dataset dataset1 = sdtParser1.getParsedDataSet();
		Dataset dataset2 = sdtParser2.getParsedDataSet();
		
		/*
		the two scripts for the regular/structural similarity values produce different keys
		for regular: nodes are labeled "V1,V2,..." 
		for structural: nodes are labeled "1,2,..."
		thus it is necessary to work with lists of the keys and not the actual keys themselves
		the user has to make sure, that the corresponding nodes are at the same position in the table
		furthermore the keys "id" and "label" are deleted
		both keys are added again after the computation of the combined similarity values
		*/
		
		DataField datafield1 = (DataField)dataset1.get(0);
		DataField datafield2 = (DataField)dataset2.get(0);
		
		Set<String> keys1 = datafield1.getPropertyKeys();
		Set<String> keys2 = datafield2.getPropertyKeys();
		
		// delete id and label
		for(int i = 0 ; i < dataset1.size(); i++) {
			DataField datafield = (DataField)dataset1.get(i);
			Set<String> keys = datafield.getPropertyKeys();
			keys.remove("id");
			keys.remove("label");
		}
		
		for(int i = 0 ; i < dataset2.size(); i++) {
			DataField datafield = (DataField)dataset2.get(i);
			Set<String> keys = datafield.getPropertyKeys();
			keys.remove("id");
			keys.remove("label");
		}
		
		
		String[] keys1array = keys1.toArray(new String[keys1.size()]);
		String[] keys2array = keys2.toArray(new String[keys2.size()]);
		
		
		// add the values for all fields 
		for(int i=0;i<dataset1.size(); i++) {
			DataField df1 = (DataField)dataset1.get(i);
			DataField df2 = (DataField)dataset2.get(i);

			for (int j = 0; j < keys1array.length; j++) {
					
				// *** use this calculation when sigma is included
				double normalizeValue = sigma1+sigma2;
				double newValue = (sigma1*Double.parseDouble((String) df1.get(keys1array[j])) + sigma2*Double.parseDouble((String) df2.get(keys2array[j])))/normalizeValue;
				
				// calculation without sigma
				//double newValue = (Double.parseDouble((String) df1.get(keys1array[j])) + Double.parseDouble((String) df2.get(keys2array[j])))/2;
				df1.addMeasure(keys1array[j], String.valueOf(newValue));
			}
			
		}
		
		// add ids and labels to the dataset
		for(int i = 0; i<dataset1.size();i++) {
			DataField df = (DataField)dataset1.get(i);
			df.addMeasure("id", (i+1)+"");
			df.addMeasure("label", "label"+(i+1));
		}
		
		sdtParser1.updateDataSet(dataset1);
		String data = sdtParser1.encode();
		
		JSONFile combinedValues = new JSONFile("combined_similarity.stf", ".stf", data, JSONFile.TEXT);
		this.data.clear();
		this.data.add(combinedValues);		

		uploadResults();
		indicateDone();
		
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void uploadResults() {
		 try {
			 System.out.println("Uploading Results...");
	            Thread.sleep(2000);
	            
	            JSONArray fileSet = new JSONArray();
	            for(int i=0; i<data.size();i++){
	            	JSONFile file = data.get(i);
	            	fileSet.add(file);
	            }            
	            
	            storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", fileSet.toJSONString());
	            
	        } catch (Exception e) {
	        	indicateError(e.getMessage());
	            e.printStackTrace();
	        } 
		 System.out.println("Task Complete!");
	}
}
