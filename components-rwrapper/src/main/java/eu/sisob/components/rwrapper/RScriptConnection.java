package eu.sisob.components.rwrapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.json.util.JSONFile;
import eu.sisob.components.framework.util.Command;
import eu.sisob.components.framework.util.DataType;

public interface RScriptConnection {

	void registerUserSpaceEvents();

	public Connection getConnection() throws SQLException;

	public boolean setupConnection();

	public ArrayList<JsonObject> readUncheckedTuples() throws Exception;

	public void updateTuple(JsonObject tuple) throws Exception;


	public void eventDeRegister(int deleteId);

	void register(Command command, JsonObject event, Agent agent);


	boolean isConnected();

	void callManager(Command command, JsonObject callback) throws Exception;

	void callAgent(Command command, JsonObject callback);

	void shutdown();

	void unregister(JsonObject event);

	boolean isGlobal();

	void setIsGlobal(boolean global);

	void register(Command command, JsonObject event, RWrapperManager manager);

}
