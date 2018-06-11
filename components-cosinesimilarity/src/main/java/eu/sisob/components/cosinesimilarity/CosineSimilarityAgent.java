package eu.sisob.components.cosinesimilarity;

import java.util.List;
import java.util.Vector;

import org.json.simple.JSONArray;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.JSONFile;

public class CosineSimilarityAgent extends Agent {

	private Vector <JSONFile> datafiles;
	
	public CosineSimilarityAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}
	
	@Override
	public void executeAgent(JsonObject dataMessage) {
		try{
			
			 this.datafiles = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
			 CosineSimilarity similarity = new CosineSimilarity();			 
			 JSONFile cosinegraph = similarity.calculateCosineSimilarity(datafiles.get(0));			  			 
			 this.datafiles.clear();
			 this.datafiles.add(cosinegraph);
			 
			 uploadResults();
			 
			 indicateDone();
			 
		}catch (Exception g){
			super.indicateError(g.getMessage());
			g.printStackTrace();
		}
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		// not implemented on purpose - we are not handling multiple inputs
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void uploadResults() {
		 try {
			 System.out.println("Uploading Results...");
	            Thread.sleep(2000);
	            
	            JSONArray fileSet = new JSONArray();
	            for(int i=0; i<datafiles.size();i++){
	            	JSONFile file = datafiles.get(i);
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
