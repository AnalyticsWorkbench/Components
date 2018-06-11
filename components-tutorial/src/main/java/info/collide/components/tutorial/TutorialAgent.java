package info.collide.components.tutorial;

import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.JSONFile;

/**
 * This is the agent belonging to the {@link TutorialManager}. Actually,
 * this agent does no analysis. Instead it prints details about recieved data and passes
 * the data iwhtout changing anything.
 *
 * @author remberg
 *
 * First extend from the {@link Agent} class ..
 *
 */
public class TutorialAgent extends Agent {
	
	/**
	 * Local field for storing the data handled in this agent
	 */
	protected Vector<JSONFile> data;
	
	/**
	 * Constructor for the agent. It first calls the constructor of the superclass, then
	 * the data structure is set, which is used to determine if a received data message is
	 * for this agent. This is used by specifying the runid und the ingoing pipes
	 * @param coordinationMessage coordination message which is passed from the {@link TutorialManager}
     */
	public TutorialAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
		// parameters can be accessed via coordinationMessage.get("parameters").getAsString()
		// which returns a json string containing all parameters or coordinationMessage.get("parameters").getAsJsonObject()
		// which returns a json object. If you want to convert the json string to a json object you can use
		// new Gson().fromJson(jsonString, JsonObject.class).
	}

	/**
	 * Here the Analysis would take place. In this case, we only inspect the data and then pass it on
	 * @param dataMessage
     */

	@Override
	public void executeAgent(JsonObject dataMessage) {

		// here we extract the data from a data message
		this.data = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));

		System.out.println("####################");
		System.out.println("I am the tutorial Agent :-)");
		System.out.println("I recieved some data:");
		for (JSONFile jsonFile : this.data) {
			System.out.println(jsonFile.getFileName() + " " + jsonFile.getFileType());
		}
		System.out.println("####################");

		// now the analysis is done, upload results
		this.uploadResults();

		// tell the manager that everything is alright and the agent is now ready to be finished
		indicateDone();
	}

	/**
	 * This method would be used it there are multiple data messages coming in.
	 * Here we do not need this method.
	 * @param dataMessages list with incoming data messages
     */
	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		// no op
	}

	/**
	 *  Here the results of our analysis are handled
	 */
	@Override
	protected void uploadResults() {
		// convert the data into a String
		String uploadData = JSONFile.collectionToString(data);

		// just call storeData from the superclass {@link Agente} with the following parameters and everything is just fine
		// 1. Runid, which can be accessed via getWorkflowID()
		// 2. The pipe in which the data "is put". It is constructed from the agent instance id and the output identifier.
		// 3. The data, in this case it is our String uploadData
		storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", uploadData);
	}

}
