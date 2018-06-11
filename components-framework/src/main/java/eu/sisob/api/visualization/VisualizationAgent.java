package eu.sisob.api.visualization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.api.parser.sisob.SDTParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.metadata.fields.DataLinks;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

public abstract class VisualizationAgent extends Agent {

    private Vector<JSONFile> jsonNetworks;
    public final int SISOB_GRAPH_VISUALIZATION_FORMAT = 0;
    public final int SISOB_GRAPH_DATA_TABLE_FORMAT = 1;
    
    public static final int BUFFER = 2048;
    
    public VisualizationAgent(JsonObject commandMsg) {
        super(commandMsg);
    }

    @Override
    public void executeAgent(JsonObject dataMessage) {
        try {
            System.out.println("Visualization Agent Has Received Data...");
            super.setWorkflowID(dataMessage.get("runid").getAsString());
            this.jsonNetworks = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
            this.jsonNetworks = isInputBASE64(this.jsonNetworks) ? transformToText(this.jsonNetworks) : this.jsonNetworks;

            System.out.println("Creating Visualization...");
            createVisualization();
            System.out.println("Visualization Generated!");
            uploadResults();
        } catch (Exception g) {
            super.indicateError("Error during agent excecution:" + g.getMessage());
            g.printStackTrace();
        }
    }

    // please override if you need a different behavior!
    public void createVisualization() throws Exception {			
		saveVisualizationTechnique(getJsonNetworks());		
	}
    
    public abstract String getZipPath();
    
    public String getOutputPath() {
    	return SISOBProperties.getResultLocation() + workflowID + File.separator + getAgentInstanceID() + File.separator;
    }

    public void saveVisualizationTechnique(Vector<JSONFile> networkData) throws Exception {

        int dataFormat = detectSISOBFormatInputType(networkData);
        switch (dataFormat) {
            case 0: {
                networkData = updateSGFDataLinks(networkData);
                break;
            }
            case 1: {
                networkData = updateSDTDataLinks(networkData);
                break;
            }
        }

        String outputLocation = getOutputPath();
        File outputDirectory = new File(outputLocation);
        BufferedWriter writer;
        outputDirectory.mkdirs();

        // write data files
        for (int i = 0; i < networkData.size(); i++) {
            writer = new BufferedWriter(new FileWriter(new File(outputLocation + i + ".json")));
            writer.write(networkData.get(i).getStringContent());
            writer.flush();
            writer.close();
        }
        // write src file
        writer = new BufferedWriter(new FileWriter(new File(outputLocation + "src.js")));
        String srcFileContent = createSRCFileContent(networkData);
        writer.write(srcFileContent);
        writer.flush();
        writer.close();

        // write the js code in the output location
        copyTechnique(true);
    }

    public int detectSISOBFormatInputType(Vector<JSONFile> updatedNetworks) throws IllegalContentTypeException {
        JSONObject network = (JSONObject) JSONValue.parse(updatedNetworks.get(0).getStringContent());
        int format = network.get("data") instanceof JSONArray ? SISOB_GRAPH_DATA_TABLE_FORMAT : SISOB_GRAPH_VISUALIZATION_FORMAT;
        return format;
    }

    private Vector<JSONFile> updateSGFDataLinks(Vector<JSONFile> updatedNetworks) throws Exception {

        Vector<JSONFile> updatedNets = new Vector<JSONFile>();
        DataLinks updatedLinks = generateDataLinks(updatedNetworks);

        for (JSONFile network : updatedNetworks) {
            SGFParser parser = new SGFParser();
            parser.setNetwork(network);
            parser.parse();
            parser.getParsedMetadata().setDatalinks(updatedLinks);
            
            JSONFile newNetwork = new JSONFile(network.getFileName(), network.getFileType(), parser.encode(), JSONFile.TEXT);
            updatedNets.add(newNetwork);
            
//            network.setData(parser.encode());
//            updatedNets.add(network);
        }
        return updatedNets;
    }

    private Vector<JSONFile> updateSDTDataLinks(Vector<JSONFile> updatedTables) throws Exception {

        Vector<JSONFile> updatedTabs = new Vector<JSONFile>();
        DataLinks updatedLinks = generateDataLinks(updatedTables);

        for (JSONFile table : updatedTables) {
            SDTParser parser = new SDTParser();
            parser.setTabledata(table);
            parser.parse();
            parser.getParsedMetadata().setDatalinks(updatedLinks);
            
            JSONFile newTable = new JSONFile(table.getFileName(), table.getFileType(), parser.encode(), JSONFile.TEXT);
            updatedTabs.add(newTable);
            
//            table.setData(parser.encode());
//            updatedTabs.add(table);
        }
        return updatedTabs;
    }

    private DataLinks generateDataLinks(Vector<JSONFile> updatedNetworks) {
        DataLinks datalinks = new DataLinks();
        for (int i = 0; i < updatedNetworks.size(); i++) {
            datalinks.addDataLink(i + ".json");
        }
        return datalinks;
    }

    private String createSRCFileContent(Vector<JSONFile> updatedNetworks) {
        StringBuilder builder = new StringBuilder();
        boolean insertComa = false;
        builder.append("var src = [");
        for (int i = 0; i < updatedNetworks.size(); i++) {

            String link = !insertComa ? "'" + i + ".json'" : ",'" + i + ".json'";
            builder.append(link);
            insertComa = true;

        }
        builder.append("]");
        return builder.toString();
    }


    @Override
    public void executeAgent(List<JsonObject> dataMessages) {

    }

    @Override
    protected void uploadResults() {
        super.indicateDone();

    }

    public Vector<JSONFile> getJsonNetworks() {
        return jsonNetworks;
    }

    public void setJsonNetworks(Vector<JSONFile> jsonNetworks) {
        this.jsonNetworks = jsonNetworks;
    }
    
    void copyTechnique(boolean useZip) throws IOException {
    	String outputDestination = getOutputPath();
    	if (useZip) {
			ClassLoader cl = getClass().getClassLoader();
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(cl.getResourceAsStream(getZipPath())));
			
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

}
