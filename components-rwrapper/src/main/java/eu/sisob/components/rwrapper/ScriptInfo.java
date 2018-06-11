/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.components.rwrapper;

import info.collide.util.ClassLoaderUtility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hecking
 */
public class ScriptInfo {
    
    private boolean acceptGraphs;
    private boolean acceptTables;
    
    private boolean dataOutput;
    private boolean graphOutput;
    private boolean decoratesGraph;
    
    private String url;
    
    private ScriptInfo() {
        
        this.acceptGraphs = false;
        this.acceptTables = false;
        this.dataOutput = false;
        this.graphOutput = false;
    }
    public static ScriptInfo getInfo(String scriptName) {
        
        ScriptInfo info = new ScriptInfo();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoaderUtility.getClassLoader()
                                    .getResourceAsStream("scriptinfo.config")));
            StringTokenizer tokenizer;
            String line = reader.readLine();
            boolean configFound = false;
            
            
            while (line != null && !configFound) {
                
                tokenizer = new StringTokenizer(line);
                configFound = tokenizer.nextToken().equals(scriptName);
                
                if (configFound) {
                    
                    info.setAcceptGraphs(Boolean.parseBoolean(tokenizer.nextToken()));
                    info.setAcceptTables(Boolean.parseBoolean(tokenizer.nextToken()));
                    info.setGraphOutput(Boolean.parseBoolean(tokenizer.nextToken()));
                    info.setDataOutput(Boolean.parseBoolean(tokenizer.nextToken()));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ScriptInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return info;
    }

    public boolean AcceptGraphs() {
        return acceptGraphs;
    }

    private void setAcceptGraphs(boolean acceptGraphs) {
        this.acceptGraphs = acceptGraphs;
    }

    public boolean AcceptTables() {
        return acceptTables;
    }

    private void setAcceptTables(boolean acceptTables) {
        this.acceptTables = acceptTables;
    }

    public boolean isDataOutput() {
        return dataOutput;
    }

    private void setDataOutput(boolean dataOutput) {
        this.dataOutput = dataOutput;
    }

    public boolean isGraphOutput() {
        return graphOutput;
    }

    private void setGraphOutput(boolean graphOutput) {
        this.graphOutput = graphOutput;
    }

    public boolean isDecoratesGraph() {
        return decoratesGraph;
    }

    private void setDecoratesGraph(boolean decoratesGraph) {
        this.decoratesGraph = decoratesGraph;
    }

    public String getUrl() {
        return url;
    }

    private void setUrl(String url) {
        this.url = url;
    }
}
