package info.collide.workbench.components.mongoexporter;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

/**
 * @author verheyen
 * 
 */
public class TestMongo {

    public static void main(String[] args) {
	MongoClient mongoClient;
	DBCursor dbc = null;
	try {
	    mongoClient = new MongoClient("localhost", 27017);
	    DB mongoDb = mongoClient.getDB("kolegeaEvents");
	    // Set<String> colls = mongoDb.getCollectionNames();
	    // System.out.println("Collections: \n");
	    DBCollection mongoCollection = mongoDb.getCollection("aggEvents");
	    BasicDBObject query = new BasicDBObject();
	    // query.put("published", new BasicDBObject("$gt",
	    // fromTo[0]).append("$lt", fromTo[1]));
	    query.put("published", new BasicDBObject("$lt", new Long("1234564123211")));
	    dbc = mongoCollection.find(query);
	    while (dbc.hasNext()) {
		System.out.println(dbc.next().toString());
	    }

	} catch (UnknownHostException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} finally {
	    dbc.close();
	}
    }
}
// for (String s : colls) {
//
// System.out.println(s);
// DBCollection coll = mongoDb.getCollection(s);
// // BasicDBObject query = new BasicDBObject("published", new
// // BasicDBObject("$lt","2014-02-03T13:33:32Z"));
// BasicDBObject query = new BasicDBObject("published", "2014-02-03T13:33:32Z");
//
// // DBCursor dbc = coll.find().sort(new BasicDBObject( new
// // BasicDBObject("published", 1)));
// DBCursor dbc = coll.find().sort(new BasicDBObject(new
// BasicDBObject("published", -1))).limit(1);
// // DBCursor dbc = coll.find();
// try {
// while (dbc.hasNext()) {
// System.out.println(dbc.next());
// }
// } finally {
// dbc.close();
// }
// }
// } catch (UnknownHostException e) {
//
// e.printStackTrace();
// }
// }

// }
