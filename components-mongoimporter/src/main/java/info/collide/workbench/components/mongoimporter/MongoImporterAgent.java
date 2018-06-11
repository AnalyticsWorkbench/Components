package info.collide.workbench.components.mongoimporter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.JSONFile;
import eu.sisob.components.framework.util.AgentStatus;

/**
 * @author verheyen
 * 
 */
public class MongoImporterAgent extends Agent {

	private ArrayList<JSONFile> results;
	private DbConnector dbConnector;
	private JsonObject commandMsg;

	public MongoImporterAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
		commandMsg = coordinationMessage;
		this.results = new ArrayList<JSONFile>();
		dbConnector = new DbConnector();
	}
	
	@Override
	public void executeAgent(JsonObject dataMessage) {
		String[] parameters = getParameters();
		String[] fromTo = timeConverter(parameters);
		String eventResult = dbConnector.getEvents(fromTo);
		if (eventResult != null) {
			if (!eventResult.isEmpty()) {
				this.results.add(new JSONFile("activity_stream.json", "json",
						StandardCharsets.UTF_8.decode(
								ByteBuffer.wrap(eventResult.getBytes()))
								.toString(), JSONFile.TEXT));
				
				// now the analysis is done, upload results
				this.uploadResults();

				// tell the manager that everyhing is alright and the agent is now ready to be finished
				indicateDone();
			} else {
				indicateError("No events available");
			}
		} else {
			indicateError("Problems connectiong to database");
		}
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		// intentionally left blank
	}

	@Override
	protected void uploadResults() {
		// convert the data into a String
		String uploadData = JSONFile.collectionToString(results);

		// just call storeData from the superclass {@link Agent} with the following parameters and everything is just fine
		// 1. Runid, which can be accessed via getWorkflowID()
		// 2. The pipe in which the data "is put". It is constructed from the agent instance id and the output identifier.
		// 3. The data, in this case it is our String uploadData
		storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", uploadData);
	}

	private String[] timeConverter(String[] splitted) {
		String[] result = new String[2];
		String from = "";
		String to = "";

		boolean lastMonth = Boolean.parseBoolean(splitted[12]);
		boolean lastXDays = false;
		int xDays = 0;
		try {
			xDays = Integer.parseInt(splitted[13]) * -1;
			lastXDays = true;
		} catch (NumberFormatException e) {
			lastXDays = false;
		}
		boolean untilNow = Boolean.parseBoolean(splitted[14]);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");
		if (lastMonth) {
			GregorianCalendar today = (GregorianCalendar) GregorianCalendar
					.getInstance(Locale.GERMANY);
			int month = today.get(Calendar.MONTH);
			month -= 1;
			GregorianCalendar fromCal = (GregorianCalendar) GregorianCalendar
					.getInstance(Locale.GERMANY);
			GregorianCalendar toCal = (GregorianCalendar) GregorianCalendar
					.getInstance(Locale.GERMANY);
			if (month < 0) {
				month = today.getActualMaximum(Calendar.MONTH);
				fromCal.set(Calendar.YEAR, today.get(Calendar.YEAR) - 1);
			}
			fromCal.set(Calendar.MONTH, month);
			fromCal.set(Calendar.DAY_OF_MONTH,
					fromCal.getActualMinimum(Calendar.DAY_OF_MONTH));
			fromCal.set(Calendar.HOUR_OF_DAY,
					fromCal.getActualMinimum(Calendar.HOUR_OF_DAY));
			fromCal.set(Calendar.MINUTE,
					fromCal.getActualMinimum(Calendar.MINUTE));
			fromCal.set(Calendar.SECOND,
					fromCal.getActualMinimum(Calendar.SECOND));
			fromCal.set(Calendar.MILLISECOND,
					fromCal.getActualMinimum(Calendar.MILLISECOND));
			dateFormat.setCalendar(fromCal);
			from = dateFormat.format(fromCal.getTime());
			toCal.set(Calendar.YEAR, fromCal.get(Calendar.YEAR));
			toCal.set(Calendar.MONTH, month);
			toCal.set(Calendar.DAY_OF_MONTH,
					toCal.getActualMaximum(Calendar.DAY_OF_MONTH));
			toCal.set(Calendar.HOUR_OF_DAY,
					toCal.getActualMaximum(Calendar.HOUR_OF_DAY));
			toCal.set(Calendar.MINUTE, toCal.getActualMaximum(Calendar.MINUTE));
			toCal.set(Calendar.SECOND, toCal.getActualMaximum(Calendar.SECOND));
			toCal.set(Calendar.MILLISECOND,
					toCal.getActualMaximum(Calendar.MILLISECOND));
			dateFormat.setCalendar(toCal);
			to = dateFormat.format(toCal.getTime());
		} else if (lastXDays) {
			GregorianCalendar fromCal = (GregorianCalendar) GregorianCalendar
					.getInstance(Locale.GERMANY);
			long toTime = fromCal.getTimeInMillis();
			Long days = Long.valueOf(xDays);
			Long hours = Long.valueOf(24);
			Long minutes = Long.valueOf(60);
			Long seconds = Long.valueOf(60);
			Long millis = Long.valueOf(1000);
			long offset = days * hours * minutes * seconds * millis;
			long fromTime = toTime + offset;
			fromCal.setTimeInMillis(fromTime);
			dateFormat.setCalendar(fromCal);
			from = dateFormat.format(fromCal.getTime());
			GregorianCalendar toCal = (GregorianCalendar) GregorianCalendar
					.getInstance(Locale.GERMANY);
			toCal.setTimeInMillis(toTime);
			dateFormat.setCalendar(toCal);
			to = dateFormat.format(toCal.getTime());
		} else if (untilNow) {
			for (int i = 0; i < 6; i++) {
				if (i < 6) {
					from = concateSubString(splitted, i, from);
				}
			}
			GregorianCalendar toCal = (GregorianCalendar) GregorianCalendar
					.getInstance(Locale.GERMANY);
			dateFormat.setCalendar(toCal);
			to = dateFormat.format(toCal.getTime());
		} else {
			for (int i = 0; i < 12; i++) {
				if (i < 6) {
					from = concateSubString(splitted, i, from);
				} else {
					to = concateSubString(splitted, i, to);
				}
			}
		}
		result[0] = from;
		result[1] = to;

		return result;
	}

	private String concateSubString(String[] splitted, int i, String sString) {
		if (i == 2 || i == 8) {
			return sString += splitted[i] + "T";
		} else if (i == 5 || i == 11) {
			return sString += splitted[i] + "Z";
		} else if (i < 2 || (i > 5 && i < 8)) {
			return sString += splitted[i] + "-";
		} else if ((i > 2 && i < 5) || i > 8) {
			return sString += splitted[i] + ":";
		}
		return null;
	}
	
	private String[] getParameters() {
		String[] parameters = new String[15];
		parameters[0] = (String) getFilterParameters().get("value1");
		parameters[1] = (String) getFilterParameters().get("value2");
		parameters[2] = (String) getFilterParameters().get("value3");
		parameters[3] = (String) getFilterParameters().get("value4");
		parameters[4] = (String) getFilterParameters().get("value5");
		parameters[5] = (String) getFilterParameters().get("value6");
		parameters[6] = (String) getFilterParameters().get("value7");
		parameters[7] = (String) getFilterParameters().get("value8");
		parameters[8] = (String) getFilterParameters().get("value9");
		parameters[9] = (String) getFilterParameters().get("value10");
		parameters[10] = (String) getFilterParameters().get("value11");
		parameters[11] = (String) getFilterParameters().get("value12");
		parameters[12] = (String) getFilterParameters().get("value13").toString();
		parameters[13] = (String) getFilterParameters().get("value14");
		parameters[14] = (String) getFilterParameters().get("value15").toString();
		return parameters;
	}

	@Override
	public void run() {
		execAgentManually();
		super.run();
	}
	
	public void execAgentManually() {
        setAgentWorkingStatus(AgentStatus.RUNNING);
        setAlive(true);
        getAgentListener().notifyManager(this);
        executeAgent(commandMsg);
    }

}
