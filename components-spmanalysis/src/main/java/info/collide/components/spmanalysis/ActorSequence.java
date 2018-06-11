package info.collide.components.spmanalysis;

import java.util.ArrayList;

import eu.sisob.api.visualization.format.eventlog.fields.EventLog;


/**
 * @author doberstein
 */

public class ActorSequence {
	
	private ArrayList<EventLog> eventlogs = new ArrayList<EventLog>();
	private String actor;
	
	public ActorSequence(EventLog eventlog) {
		this.setActor(eventlog.getActor().toString());
		if (eventlogs.isEmpty()) {
			eventlogs = new ArrayList<EventLog>();
			eventlogs.add(eventlog);
		} else {
			eventlogs.add(eventlog);
		}		
	}
	
	public void addEvent(EventLog event) {
		eventlogs.add(event);
	}

	public ArrayList<EventLog> getEventlogs() {
		return eventlogs;
	}

	public void setEventlogs(ArrayList<EventLog> eventlogs) {
		this.eventlogs = eventlogs;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}
	

}
