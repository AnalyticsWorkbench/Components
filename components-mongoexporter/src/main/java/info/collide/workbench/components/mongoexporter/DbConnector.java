package info.collide.workbench.components.mongoexporter;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

import eu.sisob.components.framework.json.util.JSONFile;

/**
 * @author verheyen
 * 
 */
public class DbConnector {
	private DBCollection mongoCollection;
	private boolean errorOccured = false;
	private final MExporterProps props;

	public DbConnector() {
		MongoClient mongoClient;
		props = MExporterProps.getInstance();
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
	public void writeEvents(Vector<JSONFile> fileSet) {
		for (JSONFile jFile : fileSet) {
			JSONArray jArray = (JSONArray) ((JSONObject) JSONValue.parse((String) jFile.toJSONObject().get("filedata")))
					.get("items");

			for (int i = 0; i < jArray.size(); i++) {
				JSONObject jo = (JSONObject) jArray.get(i);
				jo.put("published", (long) convertDateFormatToUnix(jo.get("published").toString()));
				DBObject dbO = (DBObject) JSON.parse((jo).toJSONString());

				if (mongoCollection.count(dbO) == 0) {
					mongoCollection.insert(dbO);
					// System.out.println("insert: " + jo.toJSONString());
				}
			}
		}
	}

	private long convertDateFormatToUnix(String timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
		try {
			return sdf.parse(timestamp).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
