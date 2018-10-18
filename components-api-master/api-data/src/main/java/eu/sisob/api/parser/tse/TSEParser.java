/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.api.parser.tse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import eu.sisob.api.parser.LogParser;
import eu.sisob.api.visualization.format.eventlog.fields.Actor;
import eu.sisob.api.visualization.format.eventlog.fields.EventLog;
import eu.sisob.api.visualization.format.eventlog.fields.ObjectLog;
import eu.sisob.api.visualization.format.eventlog.fields.Target;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

/**
 * 
 * @author hecking
 */
public class TSEParser extends LogParser {

	public TSEParser() {

		this.logs = new ArrayList<EventLog>();
	}

	public TSEParser(JSONFile logfile) {

		this.logfile = logfile;
		this.logs = new ArrayList<EventLog>();
	}

	@Override
	public void parse() throws IllegalContentTypeException {

		StringTokenizer tokenizer1;
		StringTokenizer tokenizer2;
//		String verb = "";
//		ObjectLog obj = null;
//		Target tar = null;
		EventLog log;
		String logs = this.logfile.getStringContent();
		BufferedReader reader = new BufferedReader(new StringReader(logs));

		try {

			String line = reader.readLine();
			while (line != null) {

				log = new EventLog();
				tokenizer1 = new StringTokenizer(line);
				log.setActor(new Actor(tokenizer1.nextToken()));
				tokenizer2 = new StringTokenizer(tokenizer1.nextToken(), "|");

				log.setVerb(tokenizer2.nextToken());
				if (tokenizer2.hasMoreTokens()) {

					log.setObject(new ObjectLog(tokenizer2.nextToken()));
				} else {

					log.setObject(new ObjectLog("null_object"));
				}
				if (tokenizer2.hasMoreTokens()) {

					log.setTarget(new Target(tokenizer2.nextToken()));
				} else {

					log.setTarget(new Target("null_target"));
				}

				Calendar.getInstance().setTimeInMillis(
						Long.parseLong(tokenizer1.nextToken()));
				line = reader.readLine();
			}

		} catch (IOException e) {

			throw new IllegalContentTypeException(
					"Cannot read the input data. " + e.getMessage());
		}
	}

	@Override
	public String encode() {

		return encode("id", "id", null, null);
	}

	public String encode(String objectProperty, String targetProperty,
			String startVerb, String endVerb) {

		this.sortLogs();
		if (startVerb != null) {
			// build map userId -> events
			Map<Actor, List<EventLog>> tempMap = new HashMap<Actor, List<EventLog>>();
			List<EventLog> evList;
			for (EventLog ev : this.logs) {
				if (!tempMap.containsKey(ev.getActor())) {
					evList = new ArrayList<EventLog>();
					evList.add(ev);
					tempMap.put(ev.getActor(), evList);
				} else {
					tempMap.get(ev.getActor()).add(ev);
				}
			}

			// build new list
			Map<Integer, List<EventLog>> eventSeqMap = new TreeMap<Integer, List<EventLog>>();
			for (List<EventLog> actorEventsList : tempMap.values()) {
				int i = eventSeqMap.size();
				List<EventLog> sList;
				for (EventLog el : actorEventsList) {
					if (el.getVerb().equals(startVerb) && !eventSeqMap.containsKey(i)) {
						sList = new ArrayList<EventLog>();
						sList.add(el);
						eventSeqMap.put(i, sList);
					} else if (el.getVerb().equals(endVerb) && eventSeqMap.containsKey(i)) {
						if (endVerb.equals(startVerb)) {
							sList = new ArrayList<EventLog>();
							sList.add(el);
							i++;
							eventSeqMap.put(i, sList);
						} else {
							eventSeqMap.get(i).add(el);
							i++;
						}

					} else {
						if (eventSeqMap.containsKey(i)) {
							eventSeqMap.get(i).add(el);
						}
					}
				}
			}
			return generateStringFromMap(objectProperty, targetProperty,
					eventSeqMap);
		} else {
			return generateStringFromLog(objectProperty, targetProperty);
		}
	}

	private String generateStringFromLog(String objectProperty,
			String targetProperty) {
		String tseData = "";
		String event = "";
		for (EventLog log : this.logs) {

			event = log.getVerb();

			if (log.getObject() != null && objectProperty != null) {
				event = event + "|"
						+ log.getObject().getProperty(objectProperty);
			}

			if (log.getTarget() != null && targetProperty != null) {

				event = event + "|"
						+ log.getTarget().getProperty(targetProperty);
			}

			tseData = tseData + log.getActor().getId() + " "
					+ log.getTimestamp() + " " + event.replace(" ", "_") + "\n";
		}
		return tseData;
	}

	private String generateStringFromMap(String objectProperty,
			String targetProperty, Map<Integer, List<EventLog>> map) {
		String tseData = "";
		String event = "";
		for (Entry<Integer, List<EventLog>> entry : map.entrySet()) {
			for (EventLog el : entry.getValue()) {
				event = el.getVerb();

				if (el.getObject() != null && objectProperty != null) {
					event = event + "|"
							+ el.getObject().getProperty(objectProperty);
					if(el.getObject().getProperty("objectType").equals("view")&&el.getObject().getProperty("name")!=null){
						event +="("+el.getObject().getProperty("name")+")";
					}
				}

				if (el.getTarget() != null && targetProperty != null) {

					event = event + "|"
							+ el.getTarget().getProperty(targetProperty);
					if(el.getTarget().getProperty("objectType").equals("view")&&el.getTarget().getProperty("name")!=null){
						event +="("+el.getTarget().getProperty("name")+")";
					}
					
				}

				tseData = tseData + entry.getKey() + " " + el.getTimestamp()
						+ " " + event.replace(" ", "_") + "\n";
			}
		}
		return tseData;
	}

	@Override
	public String requestFileTypeExtension() {

		return ".tse";
	}

	@Override
	public String requestFileType() {

		return "tse";
	}

	@Override
	protected String encodeLogMetadata() {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	protected void parseRobust() throws IllegalContentTypeException {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

}
