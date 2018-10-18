package eu.sisob.components.datatablemerge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import eu.sisob.api.parser.sisob.SDTParser;
import eu.sisob.api.visualization.format.datatable.Dataset;
import eu.sisob.api.visualization.format.datatable.fields.DataField;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.Properties;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import org.json.simple.JSONArray;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author walter
 */
public class DataTableMergeAgent extends Agent {

	List<JSONFile> results;

	private Vector<JSONFile> data1;

	private Vector<JSONFile> data2;

	private String fieldIdentifier;

	public DataTableMergeAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);

		fieldIdentifier = getFilterParameters().get(DataTableMergeManager.FIELD_ID).toString();

	}

	private List<JSONFile> mergeData(Vector<JSONFile> data1,
			Vector<JSONFile> data2) throws IllegalContentTypeException {
		List<JSONFile> result = new LinkedList<JSONFile>();

		for (int i = 0; i < data1.size(); i++) {
			SDTParser parser1 = new SDTParser();
			parser1.setTabledata(data1.get(i));
			parser1.parse();

			SDTParser parser2 = new SDTParser();
			parser2.setTabledata(data2.get(i));
			parser2.parse();

			Metadata mergedMetadata = mergeMetadata(
					parser1.getParsedMetadata(), parser2.getParsedMetadata());

			Dataset mergedDataset = mergeDatasets(parser1.getParsedDataSet(),
					parser2.getParsedDataSet());

			SDTParser mergedParser = new SDTParser();
			
			mergedParser.updateMetadata(mergedMetadata);
			mergedParser.updateDataSet(mergedDataset);

			result.add(new JSONFile("merged_datatable.stf", "stf", mergedParser
					.encode(), JSONFile.TEXT));
		}

		return result;
	}

	private Dataset mergeDatasets(Dataset ds1, Dataset ds2) {
		Dataset mergedDs = new Dataset();
		mergedDs.addAll(ds1);
		for (DataField df : ds2.getDataSet()) {
			if (!containsDataField(df, mergedDs, fieldIdentifier)) {
				mergedDs.addDataField(df);
			}
		}
		return mergedDs;
	}

	private boolean containsDataField(DataField field, Dataset dataset,
			String identifier) {
		if ("id".equals(identifier)) {
			for (DataField df : dataset.getDataSet()) {
				if (df.getId().equals(field.getId())) {
					return true;
				}
			}
		} else if ("label".equals(identifier)) {
			for (DataField df : dataset.getDataSet()) {
				if (df.getLabel().equals(field.getLabel())) {
					return true;
				}
			}
		}
		return false;
	}

	private Metadata mergeMetadata(Metadata m1, Metadata m2) {
		Metadata mergedMd = new Metadata("merged_" + m1.getTitle() + "_"
				+ m2.getTitle(), "Merged table of "
				+ m1.getTitle() + " and " + m2.getTitle());

		// merge measures
		Properties prop1 = m1.getProperties();
		Properties prop2 = m2.getProperties();
		//Measures meas1 = m1.getNetworkMeasures();
		//Measures meas2 = m2.getNetworkMeasures();
		//Measures mergedM = new Measures();
		Properties mergedP = new Properties();
		if (prop1 != null && prop2 != null) {
			mergedP.addAll(prop1);
			mergedP.addAll(prop2);
		} else if (!(prop1 == null && prop2 == null)) {
			mergedP.addAll(prop1 != null ? prop1 : prop2);
		}
		mergedMd.setProperties(mergedP);

		return mergedMd;
	}
	
	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		try {
			if (dataMessages.size() < 2) {
			super.indicateError("Unable to join input data because one of the required inputs is missing.");
			} else {
				JsonObject dataMessage1 = dataMessages.get(0);
				JsonObject dataMessage2 = dataMessages.get(1);
				
				data1 = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage1.get("payload")));
				data2 = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage2.get("payload")));
				
				results = mergeData(data1, data2);
				
				uploadResults();
			}
		} catch (Exception g) {
			super.indicateError(g.getMessage());
			g.printStackTrace();
		}
	}


	/*
	@Override
	protected void uploadResults() {
		Tuple resultingDataTuple = new Tuple(this.getWorkflowID(), 1,
				this.getAgentInstanceID() + ".out_1",
				JSONFile.collectionToString(this.results), "");
		try {
			this.getSisobspace().write(resultingDataTuple);
			indicateDone();
		} catch (TupleSpaceException ex) {
			Logger.getLogger(GraphPropertyFilterAgent.class.getName()).log(
					Level.SEVERE, null, ex);
			this.indicateError("Cannot connect to memory.");
		}
	}*/
	/*
	@Override
	protected void uploadResults() {

		Tuple resultingDataTuple = new Tuple(this.getWorkflowID(), 1,
				this.getAgentInstanceID() + ".out_1",
				JSONFile.collectionToString(this.results), "");
		try {
			this.getSisobspace().write(resultingDataTuple);
			indicateDone();
		} catch (TupleSpaceException ex) {
			Logger.getLogger(DataTableMergeAgent.class.getName()).log(
					Level.SEVERE, null, ex);
			this.indicateError("Cannot connect to memory.");
		}
	}*/
	
	@Override
	protected void uploadResults() {
		 try {
			 System.out.println("Uploading Results...");
	            Thread.sleep(2000);
	            
	            JSONArray fileSet = new JSONArray();
	            for(int i=0; i<results.size();i++){
	            	JSONFile file = results.get(i);
	            	fileSet.add(file);
	            }            
	            
	            storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", fileSet.toJSONString());
	            
	        } catch (Exception e) {
	        	indicateError(e.getMessage());
	            e.printStackTrace();
	        } 
		 System.out.println("Task Complete!");
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		throw new UnsupportedOperationException("Unable to join input data because one of the required inputs is missing."); //To change body of generated methods, choose Tools | Templates.
	}

}
