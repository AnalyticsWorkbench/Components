package info.collide.workbench.components.logstatisticsovertime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.api.parser.DataTableParser;
import eu.sisob.api.parser.LogParser;
import eu.sisob.api.parser.activitystreams.ActivityStreamParser;
import eu.sisob.api.parser.sisob.SDTParser;
import eu.sisob.api.visualization.format.datatable.Dataset;
import eu.sisob.api.visualization.format.datatable.fields.DataField;
import eu.sisob.api.visualization.format.eventlog.fields.EventLog;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.Properties;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

public class LogStatisticsOverTimeAgent extends Agent {

	private List<JSONFile> results;
	private String verb;
	private String objectType;
	private String targetType;
	private String groupByCategory;
	private String groupByProperty;
	private String timeInterval;

	private SimpleDateFormat sdf;
	private Vector<Entry> entries;
	private HashSet<String> groups;
	
	private Date earliestEntry;
	private Date latestEntry;

	public LogStatisticsOverTimeAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);

		results = new LinkedList<JSONFile>();
		JSONObject params = getFilterParameters();
		verb = (String) params.get("value1");
		objectType = (String) params.get("value2");
		targetType = (String) params.get("value3");
		groupByCategory = (String) params.get("value4");
		groupByProperty = (String) params.get("value5");

		timeInterval = (String) params.get("value6");
		if (timeInterval.equals("day")) {
			sdf = new SimpleDateFormat("yyyy-MM-dd");
		} else if (timeInterval.equals("month")) {
			sdf = new SimpleDateFormat("yyyy-MM");
		} else {
			sdf = new SimpleDateFormat("yyyy-ww");
		}

		entries = new Vector<Entry>();
		groups = new HashSet<String>();
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		Vector<JSONFile> files = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
		try {
			for (JSONFile file : files) {
				results.add(generateStatistics(file));
			}
			this.uploadResults();
		} catch (IllegalContentTypeException ex) {
			Logger.getLogger(LogStatisticsOverTimeAgent.class.getName()).log(Level.SEVERE, null, ex);
			this.indicateError(ex.getMessage());
		}
		indicateDone();
	}

	private JSONFile generateStatistics(JSONFile file) throws IllegalContentTypeException {
		// parse logfile
		LogParser parser = new ActivityStreamParser();
		parser.setLogfile(file);
		parser.parse();

		SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
		boolean isWeek = timeInterval.equals("week");

		for (EventLog event : parser.getEventLogs()) {
			if (isVerbCriteraMet(event) && isObjectTypeCriteraMet(event) && isTargetTypeCriteraMet(event)) {
				Date d = new Date(event.getTimestamp());
				String date = sdf.format(d);
				
				updateDates(d);

				// days of the last week in december can be in week one of the
				// new year
				if (isWeek && sdfMonth.format(d).equals("12")) {
					String[] tmp = date.split("-");
					if (tmp[1].equals("01")) {
						int year = Integer.parseInt(tmp[0]) + 1;
						date = year + "-" + tmp[1];
					}
				}

				String group = getGroup(event);
				boolean found = false;
				for (int i = 0; i < entries.size() && !found; i++) {
					Entry e = entries.get(i);
					if (e.date.equals(date)) {
						if (!groupByCategory.equals("no grouping")
								&& (!groupByProperty.isEmpty() || groupByCategory.equals("verb"))) {
							if (group != null && e.group != null && e.group.equalsIgnoreCase(group)) {
								found = true;
								e.amount++;
							}
						} else {
							found = true;
							e.amount++;
						}
					}
				}
				if (!found) {
					Entry entry = new Entry(date, group);
					entries.add(entry);
					if (group != null) {
						groups.add(group);
					}
				}
			}
		}

		DataTableParser dtparser = new SDTParser();
		dtparser.updateMetadata(createMetadata());
		dtparser.updateDataSet(generateDataSet());
		JSONFile dataFile = new JSONFile("datatable" + System.currentTimeMillis() + ".sdt", "sdt", dtparser.encode(),
				JSONFile.TEXT);

		return dataFile;
	}

	private Dataset generateDataSet() {
		Dataset dataset = new Dataset();

		int i = 0;
		if (!entries.isEmpty()) {

			Collections.sort(entries);

			// init calendar & start / stop date
//			String dateStart = entries.get(0).date;
//			String dateStop = sdf.format(new Date());
			String dateStart = sdf.format(earliestEntry);
			String dateStop = sdf.format(latestEntry);

			String[] tmpdate = dateStart.split("-");
			Calendar c = null;
			if (timeInterval.equals("day")) {
				c = new GregorianCalendar(Integer.parseInt(tmpdate[0]), Integer.parseInt(tmpdate[1]) - 1,
						Integer.parseInt(tmpdate[2]));
			} else if (timeInterval.equals("month")) {
				c = new GregorianCalendar(Integer.parseInt(tmpdate[0]), Integer.parseInt(tmpdate[1]) - 1, 1);
			} else {
				c = new GregorianCalendar();
				c.set(GregorianCalendar.YEAR, Integer.parseInt(tmpdate[0]));
				c.set(GregorianCalendar.WEEK_OF_YEAR, Integer.parseInt(tmpdate[1]));
				c.set(GregorianCalendar.DAY_OF_WEEK, 1);
			}

			// process all time intervals
			String date = sdf.format(c.getTime());
			while (date.compareTo(dateStop) <= 0) {
				// init list of fields
				DataField df = new DataField(String.valueOf(i), date);
				if (!groupByCategory.equals("no grouping")) {
					if (!groups.isEmpty()) {
						Iterator<String> groupIt = groups.iterator();
						while (groupIt.hasNext()) {
							df.addMeasure("frequency" + groupIt.next(), "0");
						}
					}
				} else {
					df.addMeasure("frequency", "0");
				}

				// process data / update fields
				if (!entries.isEmpty()) {
					String cDate = entries.get(0).date;
					while (date.equals(cDate) && !entries.isEmpty()) {
						String group = entries.get(0).group;
						if (groupByCategory.equals("no grouping")
								|| (groupByProperty.isEmpty() && !groupByCategory.equals("verb"))) {
							df.addMeasure("frequency", String.valueOf(entries.get(0).amount));
						} else if (group != null) {
							df.addMeasure("frequency" + group, String.valueOf(entries.get(0).amount));
						}
						entries.remove(0);
						if (!entries.isEmpty()) {
							cDate = entries.get(0).date;
						}
					}
				}

				dataset.addDataField(df);

				if (timeInterval.equals("day")) {
					c.add(Calendar.DAY_OF_MONTH, 1);
				} else if (timeInterval.equals("month")) {
					c.add(Calendar.MONTH, 1);
				} else {
					c.add(Calendar.WEEK_OF_YEAR, 1);
				}
				date = sdf.format(c.getTime());
				i++;
			}

		}

		return dataset;
	}

	private String getGroup(EventLog event) {
		if (groupByCategory.equals("no grouping") || (groupByProperty.isEmpty() && !groupByCategory.equals("verb"))) {
			return null;
		} else if (groupByCategory.equals("actor")) {
			return event.getActor().getProperty(groupByProperty);
		} else if (groupByCategory.equals("object")) {
			return event.getObject().getProperty(groupByProperty);
		} else if (groupByCategory.equals("target") && event.getTarget() != null) {
			return event.getTarget().getProperty(groupByProperty);
		} else if (groupByCategory.equals("generator") && event.getGenerator() != null) {
			return event.getGenerator().getProperty(groupByProperty);
		} else if (groupByCategory.equals("verb")) {
			return event.getVerb();
		}
		return null;
	}

	private boolean isVerbCriteraMet(EventLog event) {
		return verb.isEmpty() || (!verb.isEmpty() && event.getVerb().equalsIgnoreCase(verb));
	}

	private boolean isObjectTypeCriteraMet(EventLog event) {
		return objectType.isEmpty()
				|| (!objectType.isEmpty() && event.getObject().getProperty("objectType").equalsIgnoreCase(objectType));
	}

	private boolean isTargetTypeCriteraMet(EventLog event) {
		return targetType.isEmpty() || (!targetType.isEmpty() && event.getTarget() != null
				&& event.getTarget().getProperty("objectType").equalsIgnoreCase(targetType));
	}

	@SuppressWarnings("unchecked")
	private Metadata createMetadata() {
		Metadata meta = new Metadata("Time based log statistics", "");
		
		Properties properties = new Properties();

		// one measure for each group, if "groupby" is selected
		if (!groupByCategory.equals("no grouping") && !groups.isEmpty()) {
			Iterator<String> groupIt = groups.iterator();
			while (groupIt.hasNext()) {
				String group = groupIt.next();
				String label = "frequency " + group;
				String propertyName = "frequency" + group;
				properties.add(new Property(propertyName, "Integer", label, null, null));
			}
		} else {
			properties.add(new Property("frequency", "Integer", "frequency", null, null));
		}

		meta.setProperties(properties);

		return meta;
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void uploadResults() {
		// convert the data into a String
		String uploadData = JSONFile.collectionToString(this.results);
		storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", uploadData);
	}
	
	private void updateDates(Date date) {
		if (date != null) {
			if (earliestEntry == null || latestEntry == null) {
				earliestEntry = date;
				latestEntry = date;
			} else {
				if (date.before(earliestEntry)) {
					earliestEntry = date;
				}
				if (date.after(latestEntry)) {
					latestEntry = date;
				}
			}
		} else {
			throw new IllegalArgumentException("null is not accepted as parameter");
		}
	}

	private class Entry implements Comparable<Entry> {
		String date;
		String group;
		int amount;

		public Entry(String date, String group) {
			super();
			this.date = date;
			this.group = group;
			this.amount = 1;
		}

		@Override
		public int compareTo(Entry e) {
			return date.compareTo(e.date);
		}
	}
}
