package info.collide.workbench.components.activitylogsviewer;

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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

/**
 * 
 * @author Emmanuel Meinike
 *
 */
public class ActivityLogsViewerAgent extends Agent {
	
	public static final int BUFFER = 2048;

	// INPUT Zip File
	public static final String TECHNIQUE_ZIP = "info/collide/workbench/components/activitylogsviewer/technique.zip";
	public static final String HTML_FILE = "alv_index.html";

	// OUTPUT
	public String output_path = SISOBProperties.getResultLocation();

	public ActivityLogsViewerAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		System.out.println("received data");
		
		// here we extract the data from a data message
		Vector<JSONFile> fileSet = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
		
		try {
			// create destination directory
			File destinationDir = new File(getDestinationPath());
			if (!destinationDir.exists()) {
				destinationDir.mkdirs();
			}
			
			// copy all fix files and templates
			copyTechnique();
			
			// create views for data
			createViews(fileSet);

			// remove template
			new File(getDestinationPath() + HTML_FILE).delete();
			
			// create index
			createIndexFile(this.getWorkflowID());

			indicateDone();
		} catch (Exception ex) {
			indicateError("Error in creating Activity Logs View", ex);
		}
	}

	private void createIndexFile(String workflowId) throws IOException {
		String directoryPath = getDestinationPath();
		File directory = new File(directoryPath);
		File[] listing = directory.listFiles();
		File indexFile = new File(directoryPath + "index.html");
		FileWriter fw = new FileWriter(indexFile);
		String nl = System.getProperty("line.separator");
		String start = "<html><head><title>Results for Output "
				+ getAgentInstanceID() + " of Workflow " + workflowId
				+ "</title></head>" + "<body><h1>Results for Output "
				+ getAgentInstanceID() + " of Workflow " + workflowId
				+ "</h1><p>" + nl;
		String end = "</p></body></html>";
		String zipLink = new String();
		String fileLinks = new String();
		Arrays.sort(listing);
		for (File f : listing) {
			String link = "<a href=\"" + f.getName() + "\">" + f.getName()
					+ "</a>";
			if (f.getName().equals(
					workflowId + "_" + getAgentInstanceID() + "_result.zip")) {
				zipLink = "<ul><li>" + link + "</li></ul>";
			} else {
				if (f.getName().contains(new String(".html"))) {
					fileLinks = fileLinks + "<li>" + link + "</li>" + nl;
				}
			}
		}
		String html = start + zipLink + "<ul>" + nl + fileLinks + "</ul>" + nl
				+ end;
		fw.write(html);
		fw.close();
	}
	
	private void createViews(Vector<JSONFile> streamFiles) throws IOException, IllegalContentTypeException {
		for (JSONFile jsonFile : streamFiles) {
			FileWriter fw = new FileWriter(getDestinationPath() + jsonFile.getFileName());
			fw.write("var rawData = ");
			fw.write(jsonFile.getStringContent());
			fw.flush();
			fw.close();
//			jsonFile.write(getDestinationPath());
			writeContent(getDestinationPath() + jsonFile.getFileName() + ".html", modifyHtml(jsonFile.getFileName()));
		}
	}

	private void writeContent(String destination, ArrayList<String> inList) {
		FileWriter w = null;
		try {
			w = new FileWriter(destination);
			for (String s : inList) {
				w.write(s + "\n");
				w.flush();
			}
		} catch (IOException e) {
			indicateError("Could not write date", e);
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

	private ArrayList<String> getData(String data) {
		ArrayList<String> js = new ArrayList<String>();
		js.add("var rawData = ");
		js.add(data);
		js.add(";");
		return js;
	}
	
	protected String getDestinationPath() {
		return output_path + getWorkflowID() + File.separator + this.getAgentInstanceID() + File.separator;
	}

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
				// System.out.println("reader:" + line );
				if (line != null) {
					content.add(line);
				}
			}
		} catch (Exception e) {
			indicateError("Could not read data", e);
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
	
	private ArrayList<String> modifyHtml(String fileName_rawdata) {
		ArrayList<String> html = readFileContent(getDestinationPath() + HTML_FILE);
		for (int i = 0; i < html.size(); i++) {
			if (html.get(i).contains("[#RAWDATA]")) {
				html.set(i, html.get(i).replace("[#RAWDATA]", fileName_rawdata));
			}
			if (html.get(i).contains("[#WFID]")) {
				html.set(i, html.get(i).replace("[#WFID]", this.getWorkflowID()));
			}
		}
		return html;
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	protected void uploadResults() {

	}
	
	private void copyTechnique() throws IOException {
		String outputDestination = output_path + workflowID + File.separator + getAgentInstanceID() + File.separator;
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
	}

}
