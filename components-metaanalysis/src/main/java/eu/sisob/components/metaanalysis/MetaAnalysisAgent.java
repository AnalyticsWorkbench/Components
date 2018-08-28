package eu.sisob.components.metaanalysis;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

/**
 * This is the agent belonging to the {@link MetaAnalysisManager}. Actually,
 * this agent does no analysis. Instead it prints details about recieved data and passes
 * the data without changing anything.
 *
 * @author remberg
 *
 * First extend from the {@link Agent} class ..
 *
 */
public class MetaAnalysisAgent extends Agent {
	
	public String output_path = SISOBProperties.getResultLocation();
		
	/**
	 * Constructor for the agent. It first calls the constructor of the superclass, then
	 * the data structure is set, which is used to determine if a received data message is
	 * for this agent. This is used by specifying the runid und the ingoing pipes
	 * @param coordinationMessage coordination message which is passed from the {@link MetaAnalysisManager}
     */
	public MetaAnalysisAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
		// parameters can be accessed via coordinationMessage.get("parameters").getAsString()
		// which returns a json string containing all parameters or coordinationMessage.get("parameters").getAsJsonObject()
		// which returns a json object. If you want to convert the json string to a json object you can use
		// new Gson().fromJson(jsonString, JsonObject.class).
	}

	/**
	 * Here the Analysis would take place. In this case, we only inspect the data and then pass it on
	 * @param dataMessage
     */

	@Override
	public void executeAgent(JsonObject dataMessage) {
		try {
			String workflowId = dataMessage.get("runid").getAsString();
			JsonObject meta = this.analyseInput(dataMessage);
			createMetaFile(workflowId, meta);
			//this.outputAgent = "meta.js";  // FBA
			//this.outputFile = "meta.js";
			indicateDone();
		} catch (Exception g) {
            indicateError(null, g);
        }
	}
	
	private JsonObject analyseInput(JsonObject dataMessage) {
		Vector<JSONFile> data = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
		JSONFile file = data.get(0);
		String fileType = file.getFileType();
		switch(fileType) {
			case "json": {
				return this.analyseJSON(file);
			}
			default: {
				JsonObject meta = new JsonObject();
				meta.addProperty("fileType", fileType);
				meta.addProperty("dataType", "Unknown");
				return meta;
			}
		}
	}

	private JsonObject analyseJSON(JSONFile file) {
		JsonObject meta = new JsonObject();
		meta.addProperty("fileType", "json");
		meta.addProperty("dataType", "Unknown");
		
		JsonObject data;

		data = (JsonObject) new JsonParser().parse(file.toJSONString()); // FBA APRIN was cathch here

		// Test for activity streams
		try {
			JsonArray items = (JsonArray) data.get("items");
			if (items != null) {
				JsonObject sample = (JsonObject) items.get(0);
				if (sample != null) {
					// Indicator for activity streams
					// See: http://activitystrea.ms/specs/json/1.0/#activity
					if (sample.get("actor") != null && sample.get("published") != null) {
						return this.analyseJSONActivityStream(meta, items);
					}
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		return meta;
	}

	private JsonObject analyseJSONActivityStream(JsonObject meta, JsonArray items) {
		HashMap<String, ArrayList<JsonElement>> valuesByField = new HashMap<String, ArrayList<JsonElement>>();
		int l = items.size();
		
		Instant minDate = null;
		Instant maxDate = null;
		
		for(int i = 0; i < l; i++) {
			JsonObject item = (JsonObject) items.get(i);
			this.collectJSONActivityStreamFieldValues(valuesByField, item, null);
			
			try {
				String published = item.get("published").getAsString();
				Instant itemDate = Instant.parse(published);
				if (minDate == null || minDate.isAfter(itemDate)) minDate = itemDate;
				if (maxDate == null || maxDate.isBefore(itemDate)) maxDate = itemDate;
			} catch(Exception e) {
				// swallow
			}
		}

		meta.addProperty("dataType", "ActivityStream");
		JsonArray fields = new JsonArray();

		
		JsonObject values = new JsonObject();
		for(Map.Entry<String, ArrayList<JsonElement>> entry : valuesByField.entrySet()) {
			String key = entry.getKey();
			ArrayList<JsonElement> value = entry.getValue();
			JsonArray arr = new JsonArray();
			for (JsonElement v: value) arr.add(v);
			fields.add(new JsonPrimitive(key));
			values.add(key, arr);
		}
		
		meta.add("values", values);
		meta.add("fields", fields);
		
		JsonArray dateRange = new JsonArray();
		dateRange.add(new JsonPrimitive(minDate.toString()));
		dateRange.add(new JsonPrimitive(maxDate.toString()));
		meta.add("dateRange", dateRange);
		return meta;
	}
	
	private void collectJSONActivityStreamFieldValues(HashMap<String, ArrayList<JsonElement>> values, JsonObject jo, String path) {
		Set<Map.Entry<String, JsonElement>> entries = jo.entrySet();
		for (Map.Entry<String, JsonElement> entry: entries) {
			JsonElement value = entry.getValue();
			String key = entry.getKey();
			if(value instanceof JsonObject) {
				this.collectJSONActivityStreamFieldValues(values, (JsonObject) value, key);
			} else {
				// String strValue = (String) value.toString();
				int intValue;
				Boolean isInt;
				
				try {
					intValue = value.getAsInt();
					isInt = true;
				} catch(Exception e) {
					isInt = false;
				}
				
				String prop = key;
				if (path != null) prop = path + '.' + prop;
				
				ArrayList<JsonElement> list;
				if (!values.containsKey(prop)) {
					list = new ArrayList<JsonElement>();
					values.put(prop, list);
				} else {
					list = values.get(prop);
				}
				
				if (!list.contains(value)) {
					if (isInt || prop.equals("published")) continue;
					list.add(value);
				}
				
			}
		}
	}

	private void createMetaFile(String workflowId, JsonObject meta) throws IOException {
		String directoryPath = output_path + workflowId + File.separator + getAgentInstanceID() + File.separator;
		File outDir = new File(directoryPath);
		if (!outDir.exists()) outDir.mkdirs();
		File metaFile = new File(directoryPath + "meta.js");
		FileWriter fw = new FileWriter(metaFile);
		fw.write("(function() { window['" + workflowId + "'] = (" + meta.toString() + "); })()");
		fw.close();
	}

	/**
	 * This method would be used it there are multiple data messages coming in.
	 * Here we do not need this method.
	 * @param dataMessages list with incoming data messages
     */
	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		// no op
	}

	/**
	 *  Here the results of our analysis are handled
	 */
	@Override
	protected void uploadResults() {
		// no op
	}

}
