/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.api.parser.activitystreams;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import eu.sisob.api.parser.LogParser;
import eu.sisob.api.visualization.format.eventlog.fields.Actor;
import eu.sisob.api.visualization.format.eventlog.fields.EventLog;
import eu.sisob.api.visualization.format.eventlog.fields.Generator;
import eu.sisob.api.visualization.format.eventlog.fields.ObjectLog;
import eu.sisob.api.visualization.format.eventlog.fields.Target;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

/**
 * 
 * @author hecking
 * @author goehnert
 */
public class ActivityStreamParser extends LogParser {

	public ActivityStreamParser() {

		this.logs = new ArrayList<EventLog>();
	}

	public ActivityStreamParser(JSONFile logfile) {

		this.logfile = logfile;
		this.logs = new ArrayList<EventLog>();
	}

	protected void checkLog(JSONObject log) throws IllegalContentTypeException {

		if (((JSONObject) log).get("actor") == null) {

			throw new IllegalContentTypeException(
					"Invalid logs in the activity stream. Actor is missing."
							+ log.toString());
		}

	}

	@Override
	public void parse() throws IllegalContentTypeException {

		// JSONArray data =
		// (JSONArray)JSONValue.parse(this.logfile.getTextContent());
		JSONObject data = (JSONObject) JSONValue.parse(this.logfile.getStringContent());
		JSONArray logsData = (JSONArray) data.get("items");

		this.metadata = new Metadata(this.logfile.getFileName(),
				"activity logs");

		for (Object log : logsData) {

			this.checkLog((JSONObject) log);

			this.logs.add(this.createEventLog((JSONObject) log));
		}
	}

	@Override
	public String encode() {

		boolean isFirst = true;

		String stream = this.encodeLogMetadata() + "items:[ \n";
		for (EventLog el : this.logs) {

			if (!isFirst) {

				stream = stream + ",";
			} else {

				isFirst = true;
			}
			stream = stream + el.getActor().toJSONString() + ",\n"
					+ el.getVerb() + ",\n" + el.getObject().toJSONString()
					+ ",\n" + el.getTarget().toJSONString();
		}

		stream = stream + "]";

		return stream;
	}

	@Override
	public String requestFileTypeExtension() {

		return ".jsonAS";
	}

	@Override
	public String requestFileType() {

		return "activitystream";
	}

	@Override
	protected String encodeLogMetadata() {

		// Should be changed soon
		return "metadata : [type:\"logfilde\"]";
	}

	@SuppressWarnings("unchecked")
	private EventLog createEventLog(JSONObject log) {

		EventLog el = new EventLog();

		for (Object key : log.keySet()) {
			Object value = log.get(key);
			boolean handled = false;
			if (value != null) {
				if ("actor".equalsIgnoreCase(key.toString())) {
					Actor act = new Actor();
					act.setAsJSON((JSONObject) value);
					el.setActor(act);
					handled = true;
				} else if ("target".equalsIgnoreCase(key.toString())) {
					Target tar = new Target();
					tar.setAsJSON((JSONObject) value);
					el.setTarget(tar);
					handled = true;
				} else if ("object".equalsIgnoreCase(key.toString())) {
					if (!(value.toString()).equals("{}")) {
						ObjectLog obj = new ObjectLog();
						obj.setAsJSON((JSONObject) value);
						el.setObject(obj);
					}
					handled = true;
				} else if ("generator".equalsIgnoreCase(key.toString())) {
					Generator gen = new Generator();
					gen.setAsJSON((JSONObject) value);
					el.setGenerator(gen);
					handled = true;
				} else if ("published".equalsIgnoreCase(key.toString())) {
					String published = value.toString();
					Calendar timestamp = Calendar.getInstance();
					try {
						// this is the format as specified by the activity streams format - therefore this should be first try
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
						timestamp.setTime(df.parse((String) ((JSONObject) log).get("published")));
						el.setPublished(timestamp);
					} catch (ParseException ex) {
						try {
							// this seems to be an alternative format used in some contexts
		                    SimpleDateFormat dfAlternative = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
							timestamp.setTime(dfAlternative.parse((String) ((JSONObject) log).get("published")));
							el.setPublished(timestamp);
						} catch (ParseException e) {
							// none of the two formats did work as expected
							e.printStackTrace();
							el.setPublished(published);
						}
					}
					handled = true;
				}
			}
			if (!handled) {
				el.put(key, value);
			}
		}

		// act = new Actor();
		//
		// act.setAsJSON((JSONObject)((JSONObject)log).get("actor"));
		//
		// if (((JSONObject)log).get("object") != null) {
		// obj = new ObjectLog();
		// obj.setAsJSON((JSONObject)((JSONObject)log).get("object"));
		// } else {
		// obj = null;
		// }
		//
		// if (((JSONObject)log).get("target") != null) {
		// tar = new Target();
		// tar.setAsJSON((JSONObject)((JSONObject)log).get("target"));
		// } else {
		// tar = null;
		// }
		//
		// if (((JSONObject)log).get("generator") != null) {
		// gen = new Generator();
		// gen.setAsJSON((JSONObject)((JSONObject)log).get("generator"));
		// } else {
		// gen = null;
		// }
		//
		// el.setActor(act);
		// el.setObject(obj);
		// el.setTarget(tar);
		// el.setGenerator(gen);
		//
		// Calendar cal = Calendar.getInstance();
		// SimpleDateFormat df = new
		// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		// // SimpleDateFormat df = new
		// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		// try {
		// cal.setTime(df.parse((String)((JSONObject)log).get("published")));
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }
		// el.setPublished((String) ((JSONObject) log).get("published"));
		// el.setTimestamp(cal);
		//
		// el.setVerb((String) ((JSONObject)log).get("verb"));

		return el;
	}

	@Override
	protected void parseRobust() throws IllegalContentTypeException {

		// JSONArray data =
		// (JSONArray)JSONValue.parse(this.logfile.getTextContent());
		JSONObject data;

		data = (JSONObject) JSONValue.parse(this.logfile.getStringContent());

		JSONArray logsData = (JSONArray) data.get("items");

		this.metadata = new Metadata(this.logfile.getFileName(),
				"activity logs");

		for (Object log : logsData) {
			try {
				this.checkLog((JSONObject) log);

				this.logs.add(this.createEventLog((JSONObject) log));

			} catch (IllegalContentTypeException ex) {

				this.invalidLogs.add(log.toString());
				Logger.getLogger(ActivityStreamParser.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
	}
}
