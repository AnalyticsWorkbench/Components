package info.collide.workbench.components.streamfilter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.sisob.api.parser.LogParser;
import eu.sisob.api.parser.activitystreams.ActivityStreamParser;
import eu.sisob.api.visualization.format.eventlog.fields.EventLog;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

/**
 * @author walter
 * @author goehnert
 */
public class StreamFilterAgent extends Agent {

    List<JSONFile> results;


    public StreamFilterAgent (JsonObject commandMsg) {
        super(commandMsg);
        JsonObject dataStructure = new JsonObject();

        this.results = new ArrayList<JSONFile>();

        dataStructure.addProperty("runid", commandMsg.get("runid").getAsString());
        dataStructure.addProperty("pipes", commandMsg.get("pipes").getAsString() );
        setDataStructure(dataStructure);
    }


    private JSONFile getStreams(JSONFile file) throws IllegalContentTypeException, JSONException {

        LogParser parser = new ActivityStreamParser();
        parser.setLogfile(file);
        parser.parse();

        JSONArray resultArr = new JSONArray();
        
        List<EventLog> eventlogs = parser.getEventLogs();
        
        for (EventLog log : eventlogs) {
        	if (matchesFilters(log)) resultArr.add(log);
        }

        JSONObject resultObj = new JSONObject();
        resultObj.put("items", resultArr);
        
        System.out.println("StreamFilter: Passing " + resultArr.size() + "/" + eventlogs.size() + " items");

        String outputName = file.getFileName().substring(0, file.getFileName().lastIndexOf(".")) + ".json";
        if (!outputName.startsWith("filtered_")) {
        	outputName = "filtered_" + outputName;
        }
        return new JSONFile(outputName, "json", resultObj.toJSONString(), JSONFile.TEXT);
    }

    private boolean matchesFilters(EventLog log) {
    	JSONObject params = this.getFilterParameters();
    	JSONArray filters = new JSONArray();
    	String from = null;
    	String to= null;
    	
    	try {
    		filters = (JSONArray) params.get("filters");
    	} catch(Exception e) {
    		//e.printStackTrace();
    	}
    	
    	try {
    		JSONObject dateRange = (JSONObject) params.get("dateRange");
    		from = (String) dateRange.get("from");
        	to = (String) dateRange.get("to");
    	} catch(Exception e) {
    		//e.printStackTrace();
    	}
    	
    	
    	boolean matches = true;
    	
    	matches = matchesDateRange(log, from, to);

    	if (filters != null && matches == true) {
	    	int length = filters.size();
	    	for(int i = 0; i < length; i++) {
	    		JSONObject filter = (JSONObject) filters.get(i);
	    		if (!matchesFilter(log, filter)) {
	    			matches = false;
	    		}
	    	}
    	}
    	
		return matches;
	}
    
    private Instant getInstant(String date) {
    	Instant i = null;
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		try {
			i = sdf.parse(date).toInstant();
		} catch (ParseException e) {
			try {
				SimpleDateFormat sdfAlt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
				i = sdfAlt.parse(date).toInstant();
			} catch (ParseException e2) {
				// swallow
			}
		}
		return i;
    }


	private boolean matchesDateRange(EventLog log, String from, String to) {
		boolean matches = true;
		
		try {
			String published = (String) log.get("published");
			Instant itemDate = getInstant(published);
			
			if (itemDate != null) {
				Instant fromDate = getInstant(from);
				if (fromDate != null) {
					if (itemDate.isBefore(fromDate)) matches = false;
				}
				Instant toDate = getInstant(to);
				if (toDate != null) {
					if (itemDate.isAfter(toDate)) matches = false;
				}
			}
			
		} catch (Exception e) {
			// swallow
		}
		
		return matches;
	}


	private boolean matchesFilter(EventLog log, JSONObject filter) {
		String property;
		String value;
		String type;

		try {
			property = filter.get("property").toString();
			value = filter.get("value").toString();
			type = filter.get("type").toString();
		} catch (NullPointerException e) {
			return true;
		}
		
		if (property == null || type == null) return true;
		
		LinkedList<String> path = new LinkedList<String>(Arrays.asList(property.split("\\.")));
		String logValue = this.getNestedProperty(log, path);
		if (logValue == null) return false;

		boolean result = logValue.equalsIgnoreCase(value);
		
		if (type.equals("include")) {
			return result;
		} else {
			return !result;
		}
	}


	private String getNestedProperty(JSONObject log, LinkedList<String> path) {
		if (path.size() == 1) {
			String property = path.get(0);
			String result;
			try {
				result = String.valueOf(log.get(property));
			} catch (Exception e) {
				result = null;
			}
			return result;
		} else if (path.size() > 1) {
			String property = path.removeFirst();
			try {
				JSONObject result = (JSONObject) log.get(property);
				return this.getNestedProperty(result, path);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

    @Override
    public void executeAgent(JsonObject dataMessage) {
        Vector<JSONFile> files = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
        System.out.println(this.getFilterParameters().toString());
        try {
            for (JSONFile file : files) {
                this.results.add(this.getStreams(file));
            }
            this.uploadResults();
        } catch (IllegalContentTypeException ex) {
            Logger.getLogger(StreamFilterAgent.class.getName()).log(Level.SEVERE, null, ex);
            this.indicateError(ex.getMessage(), ex);
        } catch (JSONException e) {
            Logger.getLogger(StreamFilterAgent.class.getName()).log(Level.SEVERE, null, e);
            this.indicateError(e.getMessage(), e);
        } catch (Exception e) {
            this.indicateError(null, e);
        }
    }

    @Override
    public void executeAgent(List<JsonObject> dataMessages) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void uploadResults() {

        storeData(this.getWorkflowID(), this.getAgentInstanceID() + ".out_1", JSONFile.collectionToString(this.results));
        indicateDone();
    }
}
