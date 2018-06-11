package info.collide.workbench.components.mongoexporter;

import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.JSONFile;

/**
 * @author verheyen
 * 
 */
public class MongoExporterAgent extends Agent {

	private DbConnector dbConnector;
	
	public MongoExporterAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
		dbConnector = new DbConnector();
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		boolean errors = false;
		Vector<JSONFile> fileSet  = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
		dbConnector.writeEvents(fileSet);
		if (!errors) {
			indicateDone();
		}
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		throw new UnsupportedOperationException("cannot handle multiple inputs");
	}

	@Override
	protected void uploadResults() {
		// no uploads necessary as this is an output agent
	}

}
