package info.collide.workbench.components.dynnetvis;

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

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.JSONFile;

public class DynNetVisAgent extends Agent{
	
	public static final int BUFFER = 2048;
	
	// INPUT Folder
	public static final String VIS_HOME = SISOBProperties.getThirdPartyPath() + "3ddnv" + File.separator;
	
	// INPUT Zip File
	public static final String TECHNIQUE_ZIP = "info/collide/workbench/components/dynnetvis/technique.zip";
	
	// OUTPUT
	public String outputPath = SISOBProperties.getResultLocation();

	
    public DynNetVisAgent(JsonObject coordinationMsg) {
        super(coordinationMsg);
        String tmpPath = SISOBProperties.getSlideshowServerPath();
        if (tmpPath != null && !tmpPath.trim().isEmpty()) {
            outputPath = tmpPath;
        }
    }

    @Override
    public void executeAgent(JsonObject dataMessage) {
    	System.out.println("received data");
        /*
         * expected input: one or several files in one tuple
         * as is the current format of the data uploader
         */
        boolean errors = false;
        String rawData = new Gson().toJson(dataMessage.get("payload"));

        /*
         * writeRawDataToDisk always handles JSON filesets now,
         * so no need to differentiate between single and multifile
         */
        try {
        	String[] fileNames = writeRawDataToDisk(rawData);
        	createDataJS(fileNames);
//        	FileUtils.copyDirectory(new File(VIS_HOME), new File(outputPath + workflowId + File.separator + getAgentInstanceID() + File.separator));
        	copyTechnique(true);
        	System.out.println("created 3DDynNetVis output");
        } catch (IOException ex) {
        	indicateError("Problems creating 3DDynNetVis output.", ex);
        	errors = true;
        }
        
        if (!errors) {
        	indicateDone();
        }
       
    }

	@Override
    public void executeAgent(List<JsonObject> dataMessages) {
		// intentionally left blank
    }

    @Override
    protected void uploadResults() {
        // no uploads necessary as this is an output agent
    }
    
    /**
     * Takes the rawdata String for a JSONFileSet, restores it to a JSONFileSet, and writes the data to disk.
     * 
     * @param workflowId
     * @param rawData
     * @throws IOException
     */
    private String[] writeRawDataToDisk(String rawData) throws IOException {
    	Vector<JSONFile> fileSet = JSONFile.restoreJSONFileSet(rawData);
    	String[] fileNames = new String[fileSet.size()];
    	JSONFile.writeJSONFileSet(fileSet, outputPath + workflowID + File.separator + getAgentInstanceID() + File.separator);
    	int i = 0;
    	for (JSONFile jFile : fileSet) {
    		fileNames[i] = jFile.getFileName();
    		i++;
    	}
    	return fileNames;
    }
    
    /**
     * Creates the data.js file needed as input description for the visualization technique
     * 
     * @param workflowId
     * @param fileNames
     */
    private void createDataJS(String[] fileNames) throws IOException {
    	BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath + workflowID + File.separator + getAgentInstanceID() + File.separator + "data.js"));
    	
    	writer.write("var INPUT = [");
    	writer.newLine();
    	
    	if (fileNames.length > 0) {
    		
    		for (int i = 0; i < fileNames.length - 1; i++) {
    			writer.write("\"" + fileNames[i] + "\",");
    			writer.newLine();
    		}
    		
    		writer.write("\"" + fileNames[fileNames.length - 1] + "\"");
    		writer.newLine();
    		
    	}
    	
    	writer.write("];");
    	writer.newLine();
    	
    	writer.flush();
    	writer.close();
    }
    
    private void copyTechnique(boolean useZip) throws IOException {
    	String outputDestination = outputPath + workflowID + File.separator + getAgentInstanceID() + File.separator;
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
    		FileUtils.copyDirectory(new File(VIS_HOME), new File(outputDestination));
    	}
    }
    
    

}
