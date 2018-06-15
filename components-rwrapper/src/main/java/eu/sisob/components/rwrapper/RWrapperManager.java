package eu.sisob.components.rwrapper;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import eu.sisob.components.framework.util.Command;
import eu.sisob.components.framework.util.ConnectionType;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.componentdescription.FilterCategories;
import eu.sisob.components.framework.util.ClassLoaderUtility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

/**
 * 
 * @author hecking
 */
public class RWrapperManager extends AgentManager {

	public static final String PARAMETER_SCRIPT = "script";
	
	public static String[] scripts = {
			// "betweenness_centrality.R",
			// "closeness_centrality.R",
			// "degree_centrality.R",
			"graph_statistics.R",
			// "extract_degree_table.R",
			"gradient_table.R", "aggregate_tables.R",
			// "2ModeToOneModeA.R",
			// "2ModeToOneModeB.R",
			"simplify_graphs.R", "multmat.R", "relation_inclusion.R" };
	
	

	// Regular Expression Parameters by Popo
	private List<String> Regulars;
	private List<String> antiRegulars;
	private List<Pattern> Patterns;
	private List<Pattern> antiPatterns;
	private List<String> RegularerrorMsgs;

	// RManage vars
	protected ReentrantLock lock;
	private String scriptRepositoryPath = null;
	private RScriptConnection scriptConnection;
	private RScriptNotifier scriptNotifier;

	// Seperator for the user, uuid and scriptname of rscripts written to disk
	public static String SEP = "__sep__";

	public RWrapperManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) throws Exception {
		super(commandMsg, mngId, connectionType);

		scriptRepositoryPath = SISOBProperties.getProperty("rwrapper.scriptrepository");

		String server = SISOBProperties.getServerName();
		int port = SISOBProperties.getServerPort();
		String database = SISOBProperties.getMessageBackendInternalName();
		scriptConnection = new PostgresqlRScriptConnection(server, port, database);
		lock = new ReentrantLock();
		initRegulars();
		updateRScripts();
		setupScriptListener();

	}

	// Add by Popo

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
			logger.log(Level.SEVERE, null, e);
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
			String regularInput = "^(.*)<-(.*)graphs(.*) *[.,\\n,\\r,\\r\\n]*$|^(.*)\\\\((.*)graphs(.*)\\\\) *[.,\\n,\\r,\\r\\n]*$|^(.*)<-(.*)tables(.*) *[.,\\n,\\r,\\r\\n]*$|^(.*)\\\\((.*)tables(.*)\\\\) *[.,\\n,\\r,\\r\\n]*$";
			String regularWrite = "^( *)write\\.csv\\((.*)\\) *[.,\\n,\\r,\\r\\n]*$|^( *)write\\.graph\\((.*)\\) *[.,\\n,\\r,\\r\\n]*$";
			String regularOutput = "^( *)resultData( *)<-( *)list( *)\\((.*)dataUrl( *)=(.*)\\) *[.,\\n,\\r,\\r\\n]*$";

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
	 * check the format of R-scripts file
	 * 
	 * @return state (1 is OK, negative is for error code)
	 */
	public int checkRscripts(String file) {
		int state = 1;

		try {

			// split file into lines
			String[] lines = file.split("\\r?\\n");

			// all Patterns should be satisfied
			boolean Accept[] = new boolean[Patterns.size()];
			for (int j = 0; j < Accept.length; j++) {
				Accept[j] = false;
			}

			if (lines.length > 0) {
				for (String line : lines) {
					boolean bAnti = false;
					for (Pattern pattern : antiPatterns) {
						// if match antiPatterns, ignore this line
						if (pattern.matcher(line).matches()) {
							bAnti = true;
							break;
						}
					}
					if (bAnti)
						continue;

					// check if match Patterns
					for (int i = 0; i < Patterns.size(); i++) {
						if (!Accept[i]) {
							// match
							if (Patterns.get(i).matcher(line).matches()) {
								Accept[i] = true;
							}
						}
					}
				}
			}

			for (int i = 0; i < Accept.length; i++) {
				if (!Accept[i]) {
					// existing pattern is not matched, return negative
					state = i - Accept.length;
					return state;
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not read Rscripts from JSONFiles", e);
		}
		// OK
		return state;
	}

	/**
	 * Process unchecked R-scripts tuples it could be called when manager starts
	 */
	public void updateRScripts() throws Exception {

		ArrayList<JsonObject> uncheckedTuples = scriptConnection.readUncheckedTuples();

		for (JsonObject tuple : uncheckedTuples) {
			String scriptId = tuple.get("saveid").getAsString();
			String description = tuple.get("description").getAsString();
			String user = tuple.get("creator").getAsString();
			// rawData should be an Array of Maps therefore cant be gotten as
			// String Value via getAsString()
			JsonObject[] fileArray = new Gson().fromJson(tuple.get("robject").getAsString(), JsonObject[].class);

			JsonObject fileMap = fileArray[0];

			String fileName = fileMap.get("filename").getAsString();
			String fileContent = fileMap.get("filedata").getAsString();

			int check = checkRscripts(fileContent);
			if (check == 1) {
				// format is OK, save to R-scripts repository
				writeRScriptToDisk(fileName, user, scriptId, fileContent);
			} else {
				// do not match all patterns, set description field as error
				// message
				description = RegularerrorMsgs.get(check + RegularerrorMsgs.size());

			}
			lock.lock();
			// update Tuple. state represents the check if rscript is valid
			// according to regexes
			// description gets changed to error regex errormessage if error
			tuple.addProperty("description", description);
			tuple.addProperty("state", check);
			scriptConnection.updateTuple(tuple);
			lock.unlock();

		}

	}

	/**
	 * Tuple callback function
	 * 
	 * @param cmd
	 *            The type of Tuple command, e.g. DELETE, WRITE
	 * @param seqnum
	 *            The number of callback function, used for function
	 *            identification
	 * @param beforeTuple
	 *            The Tuple whose value is not changed
	 * @param afterTuple
	 *            The Tuple whose value is changed
	 */
	public void notifyRScript(Command cmd, JsonObject tuple) throws Exception {

		String scriptId = tuple.get("saveid").getAsString();
		String description = tuple.get("description").getAsString();
		String user = tuple.get("creator").getAsString();
		// rawData should be an Array of Maps therefore cant be gotten as
		// String Value via getAsString()
		JsonObject[] fileArray = new Gson().fromJson(tuple.get("robject").getAsString(), JsonObject[].class);

		JsonObject fileMap = fileArray[0];

		String fileName = fileMap.get("filename").getAsString();
		String fileContent = fileMap.get("filedata").getAsString();

		// If tuple got deleted delete files in scriptrepository
		if (cmd.equals(Command.DELETE)) {
			
			deleteRScriptFromDisk(fileName, user, scriptId);
		}
		// if script got written verify with regexp and writeback result into db
		else if (cmd.equals(Command.WRITE)) {
			int check = checkRscripts(fileContent);
			if (check == 1) {
				// format is OK, save to R-scripts repository
				
				writeRScriptToDisk(fileName, user, scriptId, fileContent);
			} else {
				// do not match all patterns, set description field as error
				// message
				description = RegularerrorMsgs.get(check + RegularerrorMsgs.size());
			}

			lock.lock();
			// update Tuple
			try {

				tuple.addProperty("description", description);
				tuple.addProperty("state", check);

				scriptConnection.updateTuple(tuple);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not update Rscript state...", e);
				throw e;
			} finally {
				lock.unlock();
			}
		}
	}

	// End Adding Popo

    @Override
	protected void createAgent(JsonObject commandMsg) {
		System.out.println("create r-wrapper agent");
		boolean canStart = true;

		RWrapperAgent agent = new RWrapperAgent(commandMsg);
		this.getAgents().add(agent);
		agent.setAgentListener(this);
		agent.initializeAgent();

		if (canStart) {
			Thread runtime = new Thread(this);
			runtime.start();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Filter> getFilterDescriptions() {

		String shortDescription = "This filter wraps various R scripts.";
		String longDescription = "R is a common programming language for analytical purpose. This filter wraps an "
				+ "extensible set of such scripts to make them usable as part of a workflow."
				+ " Depending on the selected script the output can be either a graph or a data table.";
		JSONArray inputs = new JSONArray();
		inputs.add(new Input("in_1", "graph(s) or table(s)"));
		JSONArray outputs = new JSONArray();
		// outputs.add(new Output("out_1", "raw output"));
		outputs.add(new Output("out_1", "decorated output"));

		JSONArray fields = new JSONArray();

		fields.add(new SelectField("script", PARAMETER_SCRIPT, true, this.getAvailableScripts()));

		Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

		Filter filter = new Filter("R-Analysis", FilterCategories.MISC, container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
		return filters;
	}

	private String[] getAvailableScripts() {
		
		ArrayList<String> scriptList = new ArrayList<>(Arrays.asList(scripts));
		ArrayList<String> userScriptList = new ArrayList<>();
		
		if (scriptRepositoryPath != null) {
			String [] rScriptList = readAllRScriptNames();
			for(String s : rScriptList){
				String[] fileNameParts = s.split(SEP);
				if(fileNameParts.length == 2){
					String onlyScriptName = fileNameParts[2];
					userScriptList.add(onlyScriptName);
				}
				
			}
		}
		scriptList.addAll(userScriptList);
		String [] result = scriptList.toArray(new String[0]);

		return result;
	}

	public void writeRScriptToDisk(String scriptName, String user, String scriptId, String content) throws IOException {

		String fileName = user + SEP + scriptId + SEP + scriptName;
		File rScript = new File(scriptRepositoryPath + File.separator + fileName);
		if (rScript.exists()) {
			rScript.delete();
		}
		rScript.createNewFile();
		
		try (PrintWriter out = new PrintWriter(rScript)) {
			out.write(content);
		}
	}
	
	private void deleteRScriptFromDisk(String scriptName, String user, String scriptId) throws IOException {

		String fileName = user + SEP + scriptId + SEP + scriptName;
		File rScript = new File(scriptRepositoryPath + File.separator + fileName);
		if (rScript.exists()) {
			rScript.delete();
		}
	}
	
	private String[] readAllRScriptNames (){
		File repository = new File(scriptRepositoryPath);
		return repository.list();
	}

	public void setupScriptListener() throws SQLException {
		Connection con = scriptConnection.getConnection();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		scriptNotifier = new RScriptNotifier(con, scriptConnection);
		scriptConnection.register(Command.UPDATE, new JsonObject(), this);
		scriptConnection.register(Command.DELETE, new JsonObject(), this);
		scriptConnection.register(Command.WRITE, new JsonObject(), this);
		executorService.execute(scriptNotifier);
	}
}
