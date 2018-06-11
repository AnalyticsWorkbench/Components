package eu.sisob.components.datauploader;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.JSONFile;
import eu.sisob.components.framework.util.AgentStatus;

/**
 * @author Alfredo Ramos, Per Verheyen
 */
public class DataUploaderAgent extends Agent {

    private JsonObject commandMsg;
    private Vector<JSONFile> fileSet;

    public DataUploaderAgent(JsonObject commandMsg) {
        super(commandMsg);
        JsonObject dataStructure = new JsonObject();

        dataStructure.addProperty("runid", commandMsg.get("runid").getAsString());
        dataStructure.addProperty("pipes", commandMsg.get("pipes").getAsString());
        setDataStructure(dataStructure);
        this.commandMsg = commandMsg;
    }

    @Override
    public void executeAgent(JsonObject dataMessage) {
        String location = (String) getFilterParameters().get(DataUploaderManager.FILE_PARAMETER);
        String tmp = SISOBProperties.getProperty("upload.directory");
        if (tmp != null && !tmp.trim().isEmpty()) {
            location = tmp + File.separator + location;
        }
        File dir = new File(location);
        fileSet = new Vector<JSONFile>();

        // FIXME: Currently the data uploader declares any input to be text
        // so it can not be used for binary data like images
        // should be choosable in the future

        try {
            if (dir.isDirectory() != true) {
                //This means it is a file
                JSONFile dataFile = new JSONFile(dir, true);
                fileSet.add(dataFile);

            } else {
                // else you load a directory
                for (int i = 0; i < dir.listFiles().length; i++) {
                    JSONFile dataFile = new JSONFile(dir.listFiles()[i], true);
                    fileSet.add(dataFile);
                }
            }
            uploadResults();
        } catch (Exception g) {
            indicateError(null, g);
        }
    }

    @Override
    public void executeAgent(List<JsonObject> dataMessages) {

    }


    public void execAgentManually() {

        setAgentWorkingStatus(AgentStatus.RUNNING);
        setAlive(true);
        getAgentListener().notifyManager(this);
        executeAgent(commandMsg);
    }


    @Override
    protected void uploadResults() {
        try {
            System.out.println("DataUploader: waiting 2s");
            Thread.sleep(2000);
            System.out.println("DataUploader: starting");
            String uploadData = JSONFile.collectionToString(fileSet);

            storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", uploadData);
//            Tuple resultingDataTuple = new Tuple(getWorkflowID(), 1, getAgentInstanceID() + ".out_1", uploadData, "");
//            getSisobspace().write(resultingDataTuple);
            System.out.println("DataUploader: done");
            super.indicateDone();

        } catch (Exception e) {
            indicateError("Errror while uploading");
            logger.log(Level.SEVERE, "Error in upload-results", e);
        }
    }

    @Override
    public void run() {
        execAgentManually();
        super.run();
    }


}
