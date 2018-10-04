package eu.sisob.resdownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.JSONFile;

public class ResultDownloaderAgent extends Agent {


    private File ComponentAddress = new File(SISOBProperties.getDefultUserDictonaryPath());
    private String ProjectFolder = ComponentAddress.getParent();

    private String path = ProjectFolder + "/UI/public_html/results";

    boolean ignoreErrors = false;

    public ResultDownloaderAgent(JsonObject commandMsg) {
        super(commandMsg);

        JsonObject dataStructure = new JsonObject();

        dataStructure.addProperty("runid", commandMsg.get("runid").getAsString());
        dataStructure.addProperty("pipes", commandMsg.get("pipes").getAsString());
        setDataStructure(dataStructure);

        String tmpPath = SISOBProperties.getSlideshowServerPath();
//        if (tmpPath != null && !tmpPath.trim().isEmpty()) {
//            path = tmpPath;
//        }
//        System.out.println(path);
    }

    @Override
    public void executeAgent(JsonObject dataMessage) {
        /*
         * expected input: one or several files in one tuple
         * as is the current format of the data uploader
         */
        boolean errors = false;
        String workflowId = dataMessage.get("runid").getAsString();
        String rawData = new Gson().toJson(dataMessage.get("payload"));

         /*
         * writeRawDataToDisk always handles JSON filesets now,
         * so no need to differentiate between single and multifile
         */
        try {
            writeRawDataToDisk(workflowId, rawData);
            System.out.println("written files to disk");
        } catch (IOException ex) {
            indicateError("Problems writing data to disk.", ex);
            errors = true;
        }
        if (!errors && getFilterParameters().get(ResultDownloaderManager.PARAMETER_JSON).equals(Boolean.TRUE)) {
        	modifyExtension(workflowId);
        }
        if (!errors) {
            try {
                createZipFile(workflowId);
                System.out.println("created zip file");
            } catch (IOException ex) {
                indicateError("Problems creating zip file.", ex);
                errors = true;
            }
        }
        if (!errors) {
            try {
                createIndexFile(workflowId);
                System.out.println("created index file");
            } catch (IOException ex) {
                indicateError("Could not create index file.", ex);
                errors = true;
            }
        }
        if (!errors) {
            super.indicateDone();
        }
    }


    @Override
    public void executeAgent(List<JsonObject> dataMessages) {

    }

    @Override
    protected void uploadResults() {
        // no uploads necessary as this is an output agent
    }

    private void writeRawDataToDisk(String workflowId, String rawData) throws IOException {
    	//System.out.println("ResultDownloader: rawdata = " + rawData);
    	Vector<JSONFile> fileSet = JSONFile.restoreJSONFileSet(rawData);
    	JSONFile.writeJSONFileSet(fileSet, path + File.separator+ workflowId + File.separator + getAgentInstanceID() + File.separator);
    }
    
    private void createZipFile(String workflowId) throws IOException {
        String directoryPath = path + File.separator+ workflowId + File.separator + getAgentInstanceID() + File.separator;// FBA orginal : path + workflowId + ....
        File directory = new File(directoryPath);
        File[] listing = directory.listFiles();
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(directoryPath + workflowId + "_" + getAgentInstanceID() + "_result.zip"));
        for (File f : listing) {
            FileInputStream fis = new FileInputStream(f);
            ZipEntry ze = new ZipEntry(f.getName());
            zos.putNextEntry(ze);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            fis.close();
            zos.closeEntry();
        }
        zos.close();
    }

    private void createIndexFile(String workflowId) throws IOException {
        String directoryPath = path + File.separator+ workflowId  + File.separator + getAgentInstanceID() + File.separator;
        File directory = new File(directoryPath);
        File[] listing = directory.listFiles();
        File indexFile = new File(directoryPath + "index.html");
        FileWriter fw = new FileWriter(indexFile);
        String nl = System.getProperty("line.separator");
        String start = "<html><head><title>Results for Output " + getAgentInstanceID() + " of Workflow " + workflowId + "</title></head>" +
                "<body><h1>Results for Output " + getAgentInstanceID() + " of Workflow " + workflowId + "</h1><p>" + nl;
        String end = "</p></body></html>";
        String zipLink = new String();
        String fileLinks = new String();
        Arrays.sort(listing);
        for (File f : listing) {
            String link = "<a href=\"" + f.getName() + "\">" + f.getName() + "</a>";
            if (f.getName().equals(workflowId + "_" + getAgentInstanceID() + "_result.zip")) {
                zipLink = "<ul><li>" + link + "</li></ul>";
            } else {
                fileLinks = fileLinks + "<li>" + link + "</li>" + nl;
            }
        }
        String html = start + zipLink + "<ul>" + nl +
                fileLinks + "</ul>" + nl +
                end;
        fw.write(html);
        fw.close();
    }
    
    private void modifyExtension(String workflowId) {
    	String directoryPath = path + File.separator+ workflowId + File.separator + getAgentInstanceID() + File.separator;
        File directory = new File(directoryPath);
        File[] listing = directory.listFiles();
        for (File f : listing) {
        	if (f.getName().endsWith(".sdt") || f.getName().endsWith(".sgf")) {
        		f.renameTo(new File(f.getAbsolutePath() + ".json"));
        	}
        }
    }

}
