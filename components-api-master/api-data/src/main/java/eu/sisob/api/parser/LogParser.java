/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.api.parser;

import eu.sisob.api.visualization.format.datatable.Dataset;
import eu.sisob.api.visualization.format.eventlog.fields.EventLog;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author hecking
 */
public abstract class LogParser implements Parser {
    
    // The log data to be parsed
    protected JSONFile logfile;
    // The network data set
    protected Dataset dataset;
    // The network metadata
    protected Metadata metadata;
    
    protected List<EventLog> logs;
    
    protected List<String> invalidLogs;
    
    protected void addLog(EventLog log) {
        
        this.logs.add(log);    
    }
    
    protected void sortLogs() {
        
        Collections.sort(logs);
    }
    
    public void setLogfile(JSONFile logfile) {
        
        this.logfile = logfile;
    }
    
    public JSONFile getLogFile() {
        
        return this.logfile;
    }
    
    public void setMetadata(Metadata metadata) {
        
        this.metadata = metadata;
    }
    
    public Metadata getMetadata() {
        
        return this.metadata;
    }
    
    public List<EventLog> getEventLogs() {
        
        return this.logs;
    }
    
    public void setEventLogs(List<EventLog> logs) {
        
        this.logs = logs;
    } 
    
    protected abstract String encodeLogMetadata();
   
    /** 
     * This method is especially for log files that are likely to contain malformed log data.
     * It omits invalid logs while the normal parse method throws an exception when a malformed log is encountered.
     * 
     */
    protected abstract void parseRobust()  throws IllegalContentTypeException;
    
    protected List<String> getInvalidLogs() {
        
        return this.invalidLogs;
    }
   
}
