package eu.sisob.components.rwrapper;

//Modeled after package eu.sisob.components.framework.connection.postgresql.PostgresqlMessageConnection
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.postgresql.util.PGobject;
import org.postgresql.util.PSQLException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.NotificationReceiver;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.connection.factory.ConnectionClient;
import eu.sisob.components.framework.connection.postgresql.Notifier;
import eu.sisob.components.framework.connection.postgresql.PostgresqlDataConnection;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import eu.sisob.components.framework.util.Command;
import eu.sisob.components.framework.util.ConnectionType;
import eu.sisob.components.framework.util.DataType;
import info.collide.util.ClassLoaderUtility;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostgresqlRScriptConnection implements RScriptConnection {

	private String url;
	private int port;
	private String database;
	private boolean isConnected;
	private RScriptNotifier notifier;
	private boolean isGlobal;

	private HashMap<JsonObject, Agent> agentList;
	private HashMap<JsonObject, RWrapperManager> managerList;
	private HashMap<JsonObject, NotificationReceiver> receiverList;

	// Regular Expression Parameters by Popo
	private List<String> Regulars;
	private List<String> antiRegulars;
	private List<Pattern> Patterns;
	private List<Pattern> antiPatterns;
	private List<String> RegularerrorMsgs;

	protected static Logger logger = Logger.getLogger(PostgresqlRScriptConnection.class.getName());

	{
		boolean found = false;
		for (Handler h : logger.getHandlers()) {
			if (h instanceof ConsoleHandler) {
				found = true;
				h.setLevel(Level.ALL);
			}
		}
		if (!found) {
			ConsoleHandler ch = new ConsoleHandler();
			ch.setLevel(Level.ALL);
			logger.addHandler(ch);
		}
		logger.setLevel(Level.ALL);

	}

	public PostgresqlRScriptConnection(String url, int port, String database) {
		this.url = url;
		this.port = port;
		this.database = database;
		this.managerList = new HashMap<>();
		this.agentList = new HashMap<>();
		this.receiverList = new HashMap<>();
		this.isGlobal = false;
	}

	/**
	 * Create a new connection
	 * 
	 * @return sql connection
	 * @throws SQLException
	 *             - error if connection fails
	 */
	public Connection getConnection() throws SQLException {
		String connectionUrl = "jdbc:postgresql://" + url + ":" + port + "/" + database;
		return DriverManager.getConnection(connectionUrl, SISOBProperties.getDataBackendUsername(),
				SISOBProperties.getDataBackendPassword());
	}

	public boolean setupConnection() {

		this.isConnected = false;

		try {
			Class.forName("com.impossibl.postgres.jdbc.PGDriver");
			isConnected = true;
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			isConnected = false;
		}

		ExecutorService executorService = Executors.newSingleThreadExecutor();

		Connection connection;

		try {
			connection = getConnection();
			notifier = new RScriptNotifier(connection, this);
			executorService.execute(notifier);

		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		return isConnected;
	}

	public ResultSet executeQuery(PreparedStatement pst, Object[] parameters) throws SQLException {

		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] instanceof String) {
				pst.setString(i + 1, (String) parameters[i]);
			}
			if (parameters[i] instanceof Integer) {
				pst.setInt(i + 1, (Integer) parameters[i]);
			}
		}
		
		System.out.println(pst);

		boolean isRS = pst.execute();
		if( isRS){
			return pst.getResultSet();
		}
		else{
			return null;
		}
	}

	public ArrayList<JsonObject> readUncheckedTuples() throws SQLException {
		ArrayList<JsonObject> result;
		try (Connection conn = getConnection()) {
			result = new ArrayList<>();
			String query = "SELECT rtuple FROM public.availablescripts WHERE rtuple ->> 'state' = '0';";
			try (PreparedStatement pst = conn.prepareStatement(query)) {
				ResultSet rs = pst.executeQuery();
				while (rs.next()) {
					result.add(new Gson().fromJson(rs.getString(1), JsonObject.class));
				}

			}
		}
		return result;
	}

	public void updateTuple(JsonObject tuple) throws SQLException {
		try (Connection conn = getConnection()) {
			String saveid = tuple.get("saveid").getAsString();
			String query = "DELETE FROM public.availablescripts WHERE rtuple ->> 'saveid' = ?;";

			try (PreparedStatement pst = conn.prepareStatement(query)) {
				try (ResultSet rs = executeQuery(pst, new String[] { saveid })) {

				}
			}

			query = "INSERT INTO public.availablescripts (rtuple) VALUES (?)";
			try (PreparedStatement pst = conn.prepareStatement(query)) {
				PGobject dataObject = new PGobject();
				dataObject.setType("json");
				String json = new Gson().toJson(dataObject);
				System.out.println(json);
				dataObject.setValue(tuple.toString());
				pst.setObject(1, dataObject);
				System.out.println(pst);
				pst.execute();
			}
		}
	}

	public void eventDeRegister(int deleteId) {
		// TODO
	}

	@Override
	public void registerUserSpaceEvents() {
		// TODO Auto-generated method stub

	}

	/**
	 * Initialize Regular Expression
	 * 
	 */
	private void initRegulars() {
		// Initialize Parameters
		Regulars = new ArrayList<String>();
		Patterns = new ArrayList<Pattern>();
		antiRegulars = new ArrayList<String>();
		antiPatterns = new ArrayList<Pattern>();
		RegularerrorMsgs = new ArrayList<String>();

		// Get the configuration of regular.json file
		InputStream scriptStream = ClassLoaderUtility.getClassLoader().getResourceAsStream("regular.json");
		String Regularstr = null;
		try {
			Regularstr = IOUtils.toString(scriptStream, "UTF-8");
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not initialize regular expressions", e);
		}

		if (Regularstr != null) {
			// parse Json format
			JSONObject object = (JSONObject) JSONValue.parse(Regularstr);
			if (object != null) {
				for (Object clustId : (JSONArray) JSONValue.parse(object.get("Regulars").toString())) {
					String value = ((JSONObject) clustId).get("value").toString();
					String errorMsg = ((JSONObject) clustId).get("errorMsg").toString();
					Regulars.add(value);
					RegularerrorMsgs.add(errorMsg);
				}

				for (Object clustId : (JSONArray) JSONValue.parse(object.get("antiRegulars").toString())) {
					String value = ((JSONObject) clustId).get("value").toString();
					antiRegulars.add(value);
				}
			}
		} else {
			// if do not get configuration file, use default values
			String regularInput = "^(.*)<-(.*)graphs(.*)$|^(.*)\\((.*)graphs(.*)\\)$|^(.*)<-(.*)tables(.*)$|^(.*)\\((.*)tables(.*)\\)$";
			String regularWrite = "^( *)write.csv\\((.*)\\)$|^( *)write.graph\\((.*)\\)$";
			String regularOutput = "^( *)resultData( *)<-( *)list( *)\\(( *)dataUrl( *)=(.*)\\)$";

			Regulars.add(regularInput);
			Regulars.add(regularWrite);
			Regulars.add(regularOutput);

			String regularMark = "^( *)#(.*)$";
			antiRegulars.add(regularMark);
		}

		// compile patterns
		for (String regular : Regulars) {
			Patterns.add(Pattern.compile(regular));
		}

		for (String regular : antiRegulars) {
			antiPatterns.add(Pattern.compile(regular));
		}
	}

	/**
	 * For each register create a new thread and add it to the map eventmask ->
	 * thread
	 *
	 * @param command
	 * @param event
	 * @param agent
	 */
	@Override
	public void register(Command command, JsonObject event, Agent agent) {
		agentList.put(event, agent);
		if (SISOBProperties.getConnectionType().equals(ConnectionType.AGENTBUNDLE.name()) && !isGlobal) {
			ConnectionClient.getInstance().getGlobalMessageConnection().register(command, event, agent);
		}
	}

	@Override
	public void register(Command command, JsonObject event, RWrapperManager manager) {
		managerList.put(event, manager);
		if (SISOBProperties.getConnectionType().equals(ConnectionType.AGENTBUNDLE.name()) && !isGlobal) {
			ConnectionClient.getInstance().getGlobalMessageConnection().register(command, event, manager);
		}
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void callManager(Command command, JsonObject payload) throws Exception {

		for (Map.Entry<JsonObject, RWrapperManager> entry : managerList.entrySet()) {
			entry.getValue().notifyRScript(command, payload);
		}

	}

	@Override
	public void callAgent(Command command, JsonObject payload) {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregister(JsonObject event) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isGlobal() {
		// TODO Auto-generated method stub
		return isGlobal;
	}

	@Override
	public void setIsGlobal(boolean global) {
		isGlobal = global;

	}


	public void callReceiver(Command update, JsonObject payloadJson) {
		// Not Implemented
		
	}

}
