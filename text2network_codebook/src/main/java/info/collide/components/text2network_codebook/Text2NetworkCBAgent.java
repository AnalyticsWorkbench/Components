/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.components.text2network_codebook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import eu.sisob.components.restclient.RESTClientAgent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author hecking
 */
public class Text2NetworkCBAgent extends RESTClientAgent {
    
    protected static final String WS_URL = "http://textanalytics.collide.info:8080/textanalytics-jersey-1.0-SNAPSHOT/text2network/dict";
    //protected static final String WS_URL = "http://localhost:8080/textanalytics-jersey/text2network/dict";
    protected static final String WS_DICT = "dict";
    
    protected List<JSONFile> results;
    protected String serviceUrl;
    protected String windowSize;
    
    public Text2NetworkCBAgent(JsonObject commandMsg) {
        super(commandMsg);
        
        String wsBaseUrl = SISOBProperties.getProperty("text2network.serviceUrl");
        
        if (wsBaseUrl == null) {
            
            this.serviceUrl = WS_URL;
        } else {
            
            if (wsBaseUrl.endsWith("/")) {
                
                this.serviceUrl = wsBaseUrl + WS_DICT;
            } else {
                
                this.serviceUrl = wsBaseUrl + "/" + WS_DICT;
            }
        }
        this.windowSize = this.getFilterParameters().get(Text2NetworkCBManager.WINDOW_SIZE).toString();
        this.results = new ArrayList<JSONFile>();
    }

    @Override
    public void executeAgent(JsonObject dataMessage) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void executeAgent(List<JsonObject> dataMessages) {
        
        String texts = new Gson().toJson(dataMessages.get(0).get("payload"));
        String codebook = new Gson().toJson(dataMessages.get(1).get("payload"));
        
        Vector<JSONFile> textFiles = JSONFile.restoreJSONFileSet(texts);
        Vector<JSONFile> codebooks = JSONFile.restoreJSONFileSet(codebook);
        for (JSONFile file : textFiles) {
            
            try {
                this.results.add(this.getNetwork(file, codebooks.get(0)));
            } catch (IllegalContentTypeException ex) {
                
                this.indicateError("Cannot read input file " +  file.getFileName() + ".", ex);
            }
        }
        
        this.uploadResults();
    }

    @Override
    protected void uploadResults() {
        
        if (this.results.size() == 0) {

            // this.deleteWorkspace(new File(this.workspaceUrl));
            this.indicateError("No result data available.");
        } else {

            this.storeData(this.getWorkflowID(), this.getAgentInstanceID() + ".out_1", JSONFile.collectionToString(results));
            this.indicateDone();
        }
    }
    
    protected JSONFile getNetwork(JSONFile text, JSONFile codebook) throws IllegalContentTypeException {

        String response = null;
        Map<String, String> formParameters = new HashMap<>();
        formParameters.put("text", text.getStringContent());
        formParameters.put("dict", codebook.getStringContent());
        formParameters.put("window_size", this.windowSize);
        formParameters.put("doc_id", "workbench");
        
        response = callRestAPIPost(this.serviceUrl, formParameters);
        
        return new JSONFile((text.getFileName() + ".sgf"), "sgf", response, JSONFile.TEXT);
    }
}
