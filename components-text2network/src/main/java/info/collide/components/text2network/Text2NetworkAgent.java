/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.components.text2network;

import com.google.gson.Gson;
import eu.sisob.components.restclient.RESTClientAgent;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Vector;

/**
 *
 * @author doberstein
 */
public class Text2NetworkAgent extends RESTClientAgent {

    // URL of the Text2Network Webservice.
    // TODO: Read from property file.
    protected static final String WS_URL = "http://textanalytics.collide.info:8080/textanalytics-jersey-1.0-SNAPSHOT/text2network/";
    protected static final String WS_CLAUSE = "clause";
    protected String method;

    protected String serviceUrl;
    protected String lang;
    protected String windowSize;

    protected List<JSONFile> results;

    public Text2NetworkAgent(JsonObject commandMsg) {
        super(commandMsg);

        this.results = new ArrayList<JSONFile>();
        this.method = this.getFilterParameters().get(Text2NetworkManager.METHOD).toString();
        this.windowSize = this.getFilterParameters().get(Text2NetworkManager.WINDOW_SIZE).toString();
        this.lang = this.getFilterParameters().get(Text2NetworkManager.LANG).toString();

        if (method.equals(Text2NetworkManager.GRAMMAR_BASED)) {

            this.serviceUrl = WS_URL;
        } else if (method.equals(Text2NetworkManager.CLAUSE_BASED)) {

            this.serviceUrl = WS_URL + WS_CLAUSE;
        }
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

    protected JSONFile getNetwork(JSONFile input) throws IllegalContentTypeException {

        String response = null;
        Map<String, String> formParameters = new HashMap<>();
        formParameters.put("text", input.getStringContent());
        formParameters.put("lang", lang);
        formParameters.put("doc_id", "workbench");

        if (method.equals(Text2NetworkManager.GRAMMAR_BASED)) {

            formParameters.put("window_size", windowSize);
        } 

        response = callRestAPIPost(this.serviceUrl, formParameters);
        
        return new JSONFile((input.getFileName() + ".sgf"), "sgf", response, JSONFile.TEXT);
    }

    @Override
    public void executeAgent(JsonObject dataMessage) {

        String input = new Gson().toJson(dataMessage.get("payload"));
        Vector<JSONFile> files = JSONFile.restoreJSONFileSet(input);

        for (JSONFile file : files) {
            
            try {
                this.results.add(this.getNetwork(file));
            } catch (IllegalContentTypeException ex) {
                
                this.indicateError("Cannot read input file " +  file.getFileName() + ".", ex);
            }
        }
        
        this.uploadResults();
    }

    @Override
    public void executeAgent(List<JsonObject> dataMessages) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
