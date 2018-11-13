package eu.sisob.components.tableviewer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.JSONFile;

public class TableViewerAgent extends Agent {

	public static final int BUFFER = 2048;

	// INPUT
	public static final String TV_HOME = SISOBProperties.getThirdPartyPath() + "tv" + File.separator;
	public static final String TV_HTML_NAME = "tv_index.html";

	// INPUT Zip File
	public static final String TECHNIQUE_ZIP = "eu/sisob/components/tableviewer/technique.zip";

	// OUTPUT
	/**
	 **if you do not put the Component and UI folder in common path you have to set the address manually. for example: "/Users/farbodaprin/Desktop/WorkbenchAnalysis/results"
	 * * results/result should be in public html folder in the frontend UI path
	 * for example: public String output_path = "/Users/farbodaprin/Desktop/WorkbenchAnalysis/UI/public_html/results";
	 */
	private File ComponentAddress = new File(SISOBProperties.getDefultUserDictonaryPath());
	private String ProjectFolder = ComponentAddress.getParent();

	private String output_path = ProjectFolder + "/UI/public_html/results";
//	public String output_path = SISOBProperties.getResultLocation();
//	public String output_path2 = SISOBProperties.getUiPublicPass();
//	public String output_path = "/Users/farbodaprin/Desktop/WorkbenchAnalysis/UI/public_html/results";

	public TableViewerAgent(JsonObject commandMsg) {
		super(commandMsg);
		JsonObject dataStructure = new JsonObject();

		dataStructure.addProperty("runid", commandMsg.get("runid").getAsString());
		dataStructure.addProperty("pipes", commandMsg.get("pipes").getAsString());
		setDataStructure(dataStructure);
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		/*
		 * expected input: one or several files in one tuple as is the current
		 * format of the data uploader
		 */
		String workflowId = dataMessage.get("runid").getAsString();
		String rawData = new Gson().toJson(dataMessage.get("payload"));

		// Get the incoming data in the right format
		Vector<JSONFile> fileSet = JSONFile.restoreJSONFileSet(rawData);
		// ...and decode Base64, if necessary
		if (this.isInputBASE64(fileSet)) {
			try {
				fileSet = this.transformToText(fileSet);
			} catch (Exception e) {
				indicateError("Could not transform to Text", e);
			}
		}

		try {
			String dirName = output_path + File.separator+ workflowId + File.separator + agentInstanceID + File.separator;
			File outDir = new File(dirName);
			if (!outDir.exists()) {
				outDir.mkdirs();
			}
			copyTechnique(true);
			// then write the incoming data into the outputfolder as html-files
			for (JSONFile file : fileSet) {
				// set Filename
				String name = clearFileName(file.getFileName());

				this.createDynamicFiles(workflowId, file.toJSONString(), new String(name + "_table.html"));
			}
			removeUnusedIndexFile(dirName);
			if (outDir.list().length == 0) {
				throw new Exception("No output created!");
			}

			// at least create an index file as the workbench result
			createIndexFile(workflowId);
			indicateDone();
		} catch (Exception ex) {
			indicateError("Error in creating table view", ex);
		}
	}

	private void removeUnusedIndexFile(String outDirPath) {
		File unused = new File(outDirPath + TV_HTML_NAME);
		if (unused.exists()) {
			unused.delete();
		}
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {

	}

	@Override
	protected void uploadResults() {
		// no uploads necessary as this is an output agent
	}

	/**
	 * the files, which have to be recreated per date
	 * 
	 * @param workflowId
	 * @param data
	 * @param filename
     */
	private void createDynamicFiles(String workflowId, String data, String filename) {
		String dirName = output_path + File.separator+ workflowId + File.separator + agentInstanceID + File.separator;
		try {
			File dir = new File(dirName);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String html_fileName = filename;
			String filePath = dirName + filename;

			// check if there are already files with same name, if so, rename
			// actual File
			File test = new File(filePath);
			int counter = 0;
			while (test.exists()) {
				html_fileName = html_fileName + "(" + counter + ")";
				html_fileName = html_fileName.replace(new String("(" + (counter - 1) + ")"), "");
				counter++;
				filePath = dirName + filename;
				test = new File(filePath);
			}

			// 1. write DATA
			// i.e. filename_sisob_table.js
			String filename_data = filePath.replace(".html", ".js");
			writeContent(filename_data, this.getData(data));

			// 2. write HTML
			// i.e filename_sisob_table.html
			String data_fileName = html_fileName.replace(".html", ".js");
			writeContent(filePath, modifyHtml(data_fileName, workflowId, dirName));

		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * creates index file for all .html-files found in the result directory
	 * 
	 * @param workflowId
	 * @throws IOException
	 */
	private void createIndexFile(String workflowId) throws IOException {
		//String directoryPath = output_path + workflowId + File.separator + getAgentInstanceID() + File.separator;// FBA previous version
		String directoryPath = output_path + File.separator + workflowId + File.separator + getAgentInstanceID() + File.separator;
		File directory = new File(directoryPath);
		File[] listing = directory.listFiles();
		File indexFile = new File(directoryPath + "index.html");
		FileWriter fw = new FileWriter(indexFile);
		String nl = System.getProperty("line.separator");
		String start = "<html><head><title>Results for Output " + getAgentInstanceID() + " of Workflow " + workflowId
				+ "</title></head>" + "<body><h1>Results for Output " + getAgentInstanceID() + " of Workflow "
				+ workflowId + "</h1><p>" + nl;
		String end = "</p></body></html>";
		String zipLink = new String();
		String fileLinks = new String();
		Arrays.sort(listing);
		for (File f : listing) {
			String link = "<a href=\"" + f.getName() + "\">" + f.getName() + "</a>";
			if (f.getName().equals(workflowId + "_" + getAgentInstanceID() + "_result.zip")) {
				zipLink = "<ul><li>" + link + "</li></ul>";
			} else {
				if (f.getName().contains(new String(".html"))) {
					fileLinks = fileLinks + "<li>" + link + "</li>" + nl;
				}
			}
		}
		String html = start + zipLink + "<ul>" + nl + fileLinks + "</ul>" + nl + end;
		fw.write(html);
		fw.close();
	}

	private ArrayList<String> getHtml(String outputDirectory) {
		return this.readFileContent(outputDirectory + TV_HTML_NAME);
	}

	private ArrayList<String> getData(String data) {
		ArrayList<String> js = new ArrayList<String>();
		js.add("var rawData = ");
		js.add(data);
		js.add(";");
		return js;
	}

	/**
	 * Fills in the variable content of the html-base
	 * 
	 * @param fileName_rawdata
	 *            (the filename)
	 * @param wfid
	 * @param outDir
	 *            path to output directory
	 * @return content (ArrayList)
	 */
	private ArrayList<String> modifyHtml(String fileName_rawdata, String wfid, String outDir) {
		ArrayList<String> html = getHtml(outDir);
		for (int i = 0; i < html.size(); i++) {
			if (html.get(i).contains("[#RAWDATA]")) {
				html.set(i, html.get(i).replace("[#RAWDATA]", fileName_rawdata));
			}
			if (html.get(i).contains("[#WFID]")) {
				html.set(i, html.get(i).replace("[#WFID]", wfid));
			}
		}
		return html;
	}

	/**
	 * Writes StringArray in File
	 * 
	 * @param destination
	 *            (Filepath, have to be created - new File(path))
	 * @param inList
	 *            = the lines to write
	 */
	private void writeContent(String destination, ArrayList<String> inList) {
		FileWriter w = null;
		try {
			w = new FileWriter(destination);
			for (String s : inList) {
				w.write(s + "\n");
				w.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * reads a file at path
	 * 
	 * @param path
	 *            (Filepath of file to read)
	 * @return ArrayList of Lines in Strings
	 */
	private ArrayList<String> readFileContent(String path) {
		ArrayList<String> content = new ArrayList<String>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			String line = "";
			while (line != null) {
				line = br.readLine();
				if (line != null) {
					content.add(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
				}
			}
			if (br != null) {
				try {
					fr.close();
				} catch (IOException e) {
				}
			}
		}
		return content;
	}

	/**
	 * 
	 * @param name
	 *            inString
	 * @return cleared String
	 */
	private String clearFileName(String name) {
		name = name.toLowerCase();
		name = name.replace(".", "_");
		name = name.replace("(", "");
		name = name.replace(")", "");
		name = name.replace(" ", "_");
		name = name.replace("__", "_");
		return name;
	}

	private void copyTechnique(boolean useZip) throws IOException {
		String outputDestination = output_path + File.separator + workflowID + File.separator + getAgentInstanceID() + File.separator;
		if (useZip) {
			ClassLoader cl = getClass().getClassLoader();
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(cl.getResourceAsStream(TECHNIQUE_ZIP)));

			ZipEntry entry;

			while ((entry = zis.getNextEntry()) != null) {
				int count;
				byte[] data = new byte[BUFFER];
				String outputFileName = outputDestination + entry.getName();
				if (entry.isDirectory()) {
					new File(outputFileName).mkdirs();
				} else {
					BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(outputFileName), BUFFER);

					while ((count = zis.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}

					dest.flush();
					dest.close();
				}
			}

			zis.close();
		} else {
			FileUtils.copyDirectory(new File(TV_HOME), new File(outputDestination));
		}
	}

}
