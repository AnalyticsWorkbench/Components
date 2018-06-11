package eu.sisob.components.duplexer;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.collide.sqlspaces.commons.Tuple;
import info.collide.sqlspaces.commons.TupleSpaceException;
import eu.sisob.components.framework.Agent;

public class DuplexerAgent extends Agent {

	private String data;

	public DuplexerAgent(JsonObject commandMsg) {
		super(commandMsg);
		JsonObject dataStructure = new JsonObject();

		dataStructure.addProperty("runid", commandMsg.get("runid").getAsString());
		dataStructure.addProperty("pipes", commandMsg.get("pipes").getAsString());
		setDataStructure(dataStructure);
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		this.data = new Gson().toJson(dataMessage.get("payload"));
		this.uploadResults();
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {

	}


	@Override
	protected void uploadResults() {
		storeData(this.getWorkflowID(), this.getAgentInstanceID() + ".out_1" ,this.data);
		storeData(this.getWorkflowID(), this.getAgentInstanceID() + ".out_2" ,this.data);
		super.indicateDone();
	}

}
