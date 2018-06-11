package info.collide.workbench.components.mongoimporter;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

/**
 * @author verheyen
 * 
 */
public class DbConnector {
    private DBCollection mongoCollection;
    private boolean errorOccured = false;
    private final MImporterProps props;

    public DbConnector() {
	MongoClient mongoClient;
	props = MImporterProps.getInstance();
	try {
	    mongoClient = new MongoClient(props.getMongoServerHost(), props.getMongoServerPort());
	    DB mongoDb = mongoClient.getDB(props.getMongoDbName());
	    mongoCollection = mongoDb.getCollection(props.getMongoDBCollectionName());
	} catch (UnknownHostException e) {
	    errorOccured = true;
	    e.printStackTrace();
	}

    }

    @SuppressWarnings("unchecked")
    public String getEvents(String[] fromTo) {
	JSONArray resultingEvents = null;
	String result = null;
	if (!errorOccured) {
	    resultingEvents = new JSONArray();
	    BasicDBObject query = new BasicDBObject();
	    query.put("published", new BasicDBObject("$gt", convertDateFormatToUnix(fromTo[0])).append("$lt", convertDateFormatToUnix(fromTo[1])));
	    DBCursor dbc = mongoCollection.find(query);
	    try {
		while (dbc.hasNext()) {
		    JSONObject jo = (JSONObject) JSONValue.parse(dbc.next().toString());
		    jo.put("published", convertDateFormat((long) jo.get("published")));
		    jo.remove("_id");
		    JSONObject j = (JSONObject) JSONValue.parse(jo.toJSONString());
		    resultingEvents.add(j);

		}
	    } finally {
		dbc.close();
		result = resultingEvents.toJSONString();
		result = "{\"items\":" + result + "}";

	    }

	}
	return result;
    }

    private long convertDateFormatToUnix(String timestamp) {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
	Long result = null;
	try {
	    result = sdf.parse(timestamp).getTime();
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	return result;
    }

    private String convertDateFormat(long timestamp) {
	Date date = new Date(timestamp);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
	String formattedDate = sdf.format(date);
	return formattedDate;
    }

}
