/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.api.visualization.format.eventlog.fields;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.simple.JSONObject;

/**
 * 
 * @author hecking
 * @author goehnert
 */
public class EventLog extends AbstractLogObject implements Comparable<EventLog> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5433619104067704742L;
	private long timestamp;

	public Long getTimestamp() {
		return this.timestamp;
	}
	
	@SuppressWarnings("unchecked")
	public void setPublished(Calendar timestamp) {
		this.timestamp = timestamp.getTimeInMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String formattedDate = sdf.format(timestamp.getTime());
		this.put("published", formattedDate);
	}
	
	public String getPublished() {
		return this.get("published").toString();
	}

	// Published has to be in "Internet Date/Time Format [RFC3339]"
	@SuppressWarnings("unchecked")
	public void setPublished(String published) {
		Calendar timestamp = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		try {
			timestamp.setTime(df.parse(published));
			this.timestamp = timestamp.getTimeInMillis();
		} catch (ParseException e) {
			SimpleDateFormat sdfAlt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
			try {
			timestamp.setTime(sdfAlt.parse(published));
			this.timestamp = timestamp.getTimeInMillis();
			} catch (ParseException pe) {
				// FIXME this not a good way of handling errors... 
				pe.printStackTrace();
			}
		}
		this.put("published", published);
	}

	public Actor getActor() {
		JSONObject actor = (JSONObject)this.get("actor");
		if (actor instanceof Actor) {
			return (Actor)actor;
		} else {
			Actor tmpActor = new Actor();
			tmpActor.setAsJSON(actor);
			return tmpActor;
		}
	}

	@SuppressWarnings("unchecked")
	public void setActor(Actor actor) {
            this.put("actor", actor);
	}

	public String getVerb() {
		if (this.get("verb") != null) {
			return this.get("verb").toString();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public void setVerb(String verb) {
		this.put("verb", verb);
	}

	public ObjectLog getObject() {
		if (this.get("object") != null) {
			JSONObject object = (JSONObject)this.get("object");
			if (object instanceof ObjectLog) {
				return (ObjectLog)object;
			} else {
				ObjectLog tmpObject = new ObjectLog();
				tmpObject.setAsJSON(object);
				return tmpObject;
			}
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public void setObject(ObjectLog object) {
		this.put("object", object);
	}

	public Target getTarget() {
		if (this.get("target") != null) {
			JSONObject target = (JSONObject)this.get("target");
			if (target instanceof Target) {
				return (Target)target;
			} else {
				Target tmpTarget = new Target();
				tmpTarget.setAsJSON(target);
				return (Target)tmpTarget;
			}
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public void setTarget(Target target) {
		this.put("target", target);
	}
	
	public Generator getGenerator() {
		if (this.get("generator") != null) {
			JSONObject generator = (JSONObject)this.get("generator");
			if (generator instanceof Generator) {
				return (Generator)generator;
			} else {
				Generator tmpGenerator = new Generator();
				tmpGenerator.setAsJSON(generator);
				return tmpGenerator;
			}
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public void setGenerator(Generator generator) {
		this.put("generator", generator);
	}

	@Override
	public int compareTo(EventLog log) {
		return Long.compare(this.timestamp, log.getTimestamp());
	}

}
