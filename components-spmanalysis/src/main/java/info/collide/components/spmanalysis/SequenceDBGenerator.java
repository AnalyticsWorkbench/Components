package info.collide.components.spmanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONObject;

import eu.sisob.api.visualization.format.eventlog.fields.EventLog;

public class SequenceDBGenerator {
	
	/** 
	 * Hashtable to contain the log sequences for all actors 
	 */
	private Hashtable<String, LinkedList<EventLog>> logTable;
	
	/**
	 * Hashtable to contain the mapping from log sequence id to actor name
	 */
	private Hashtable<Integer, String> sequenceIdToGroupId;
	
	/**
	 * Hashtable to contain the mapping from events to integer values for algorithm input
	 */
	private Hashtable<EventAbstraction, Integer> eventToInteger;
	private Hashtable<Integer, EventAbstraction> integerToEvent;
	
	/**
	 * Integer counter for translating events to integer values for algorithm input
	 */
	private int eventTypeCounter;
	
	/**
	 * Translate the incoming event logs into a String that is properly formatted as algorithm input
	 * 
	 * @param eventlogs
	 * @param shortDescription
	 * @return
	 */
	public String translateEventLogsToAlgorithmInput(List<EventLog> eventlogs, String[] fields, String[] groupByFields, String splitAtProperty, String splitAtValue) {
		generateActorSequences(eventlogs, groupByFields);
		return generateInputForAnalysis(fields, groupByFields, splitAtProperty, splitAtValue);
	}
	
	/**
	 * Get the table which allows determining which actor was involved in which sequence
	 * 
	 * @return
	 */
	public Hashtable<Integer, String> getSequenceIdToGroupId() {
		return sequenceIdToGroupId;
	}
	
	/**
	 * Takes the incoming event logs and splits them into individual logs per user
	 * 
	 * @param eventlogs The incoming event logs in activity stream format
	 */
	void generateActorSequences(List<EventLog> eventlogs, String[] groupByFields) {	
		// for each actor there are two actorsequences, containing all of his actions (eventlogs)
		// so that all eventlogs are contained in sequences refering to ONE actor 
		// (vgl customersequence in Mining Sequential Patterns;  Rakesh Agrawal Ramakrishnan Srikant)
		
		// initialize the tables to hold the events
		logTable = new Hashtable<String, LinkedList<EventLog>>();
		
		//sort all activities from input file 
		Collections.sort(eventlogs);
		
		for (EventLog event : eventlogs) {
			addEventToLogTable(event, logTable, groupByFields);
		}
		
	}
	
	/**
	 * Adds the given event to the event list of the appropriate actor in the given log table. If no such list
	 * exists, it is created and added to the log table.  
	 * 
	 * @param event
	 * @param logTable
	 */
	private void addEventToLogTable(EventLog event, Hashtable<String, LinkedList<EventLog>> logTable, String[] groupByFields) {
		String groupId = joinNestedEventLogProperties(groupByFields, event);
		LinkedList<EventLog> eventLog;
		if (logTable.containsKey(groupId)) {
			eventLog = logTable.get(groupId);
		} else {
			eventLog = new LinkedList<EventLog>();
			logTable.put(groupId, eventLog);
		}
		eventLog.add(event);
	}
	
	private String joinNestedEventLogProperties(String[] paths, EventLog event) {
		String[] elements = new String[paths.length];
		for(int i = 0; i < paths.length; i++) {
			String path = paths[i];
			elements[i] = getNestedEventLogProperty(path, event);
		}
		
		return String.join("_", elements);
	}
	
	private String getNestedEventLogProperty(String path, EventLog event) {
		String[] pathSplitted = path.split("\\.");
		String value = null;
		
		JSONObject tmp = (JSONObject) event;
		for(int j = 0; j < pathSplitted.length; j++) {
			String prop = pathSplitted[j];
			if (tmp == null) {
				value = null;
				break;
			}
			if (j < pathSplitted.length - 1) {
				tmp = (JSONObject) tmp.get(prop);
			} else {
				if (tmp.get(prop) != null) {
					value = String.valueOf(tmp.get(prop));
				}
			}
		}
		return value;
	}

	/**
	 * Translate the event log tables to the format that is needed as input by the algorithms 
	 */
	String generateInputForAnalysis(String[] fields, String[] groupByFields, String splitAtProperty, String splitAtValue) {
		eventToInteger = new Hashtable<EventAbstraction, Integer>();
		integerToEvent = new Hashtable<Integer, EventAbstraction>();
		eventTypeCounter = 0;
		sequenceIdToGroupId = new Hashtable<Integer, String>();
		StringBuilder fileBuilder = new StringBuilder();
		String lineBreak = "\r\n";
		int sequenceCounter = 0;
		String lastGroupId = null;
		boolean currentSequenceClosed = false;
		for (String groupId : logTable.keySet()) {
			List<EventLog> eventList = logTable.get(groupId);
			for (EventLog event : eventList) {
				int logValue = translateEventToInteger(event, fields);
				boolean split = getNestedEventLogProperty(splitAtProperty, event).equalsIgnoreCase(splitAtValue);
				if (split && !currentSequenceClosed && lastGroupId != null) {
					fileBuilder.append("-2");
					fileBuilder.append(lineBreak);
					sequenceCounter++;
					sequenceIdToGroupId.put(Integer.valueOf(sequenceCounter), lastGroupId);
				}
				fileBuilder.append(logValue);
				fileBuilder.append(" -1 ");
				currentSequenceClosed = false;
				lastGroupId = groupId;
			}
		}
		return fileBuilder.toString();
	}
	
	/**
	 * Translate the individual events to integer values for the output
	 */
	int translateEventToInteger(EventLog event, String[] fields) {
		ArrayList<String> eventDescriptionTmp = new ArrayList<String>();
		String[] eventDescription;

		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i];
			String[] path = fieldName.split("\\.");
			String value = null;
			
			JSONObject tmp = (JSONObject) event;
			for(int j = 0; j < path.length; j++) {
				String prop = path[j];
				if (tmp == null) {
					value = null;
					break;
				}
				if (j < path.length - 1) {
					tmp = (JSONObject) tmp.get(prop);
				} else {
					if (tmp.get(prop) != null) {
						value = String.valueOf(tmp.get(prop));
					}
				}
			}
			if (value != null) {
				eventDescriptionTmp.add(value);
			}
		}
		
		eventDescription = new String[eventDescriptionTmp.size()];
		eventDescription = eventDescriptionTmp.toArray(eventDescription);

		EventAbstraction abstraction = new EventAbstraction(eventDescription);
		if (eventToInteger.containsKey(abstraction)) {
			return eventToInteger.get(abstraction);
		} else {
			eventTypeCounter++;
			eventToInteger.put(abstraction, Integer.valueOf(eventTypeCounter));
			integerToEvent.put(Integer.valueOf(eventTypeCounter), abstraction);
			return eventTypeCounter;
		}
	}
	
	public Hashtable<Integer, EventAbstraction> getEvents() {
		return integerToEvent;
	}


}
