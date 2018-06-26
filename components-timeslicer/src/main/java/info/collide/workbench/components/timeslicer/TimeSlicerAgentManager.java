/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.workbench.components.timeslicer;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.BooleanField;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.util.ConnectionType;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;

/**
 *
 * @author hecking
 */
public class TimeSlicerAgentManager extends AgentManager {

    public static final String BEGIN = "begin";
    public static final String END = "end";
    public static final String WINDOW = "window";
    public static final String INCLUDE_ISOLATES = "isolates";
    public static final String OUTLET = "out_1";
    
    public TimeSlicerAgentManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
        super(commandMsg, mngId, connectionType);
    }
    @Override
    protected void createAgent(JsonObject commandMsg) {
        
        System.out.println("create timeslicer agent");
                boolean canStart = true;
		TimeSlicerAgent agent = new TimeSlicerAgent(commandMsg);
		this.createAgent(agent);
                
                if (canStart) {
                    agent.setAgentListener(this);
                    Thread runtime = new Thread(this);
                    runtime.start();
                }
    }

    @Override
    protected List<Filter> getFilterDescriptions() {
        
        String shortDescription = "This filter creates a time series of graphs from a graph with timestamp information.";
    	String longDescription = "The input needs to be a timestaped graph. Based on the timestamps this agent creates a series of "
                + "graphs corresponding to the given time window size.";
    	JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "timstamped graph"));
    	JSONArray outputs = new JSONArray();
    	outputs.add(new Output(OUTLET, "timeslices"));
        
    	
    	JSONArray fields = new JSONArray();
    	
        fields.add(new StringField("begin", BEGIN, true));
        fields.add(new StringField("end", END, true));
        fields.add(new StringField("window size", WINDOW, true));
        fields.add(new BooleanField("include isolated nodes?", INCLUDE_ISOLATES, true));

        Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);
    	
    	Filter filter = new Filter("Timeslicer", "Tools", container);
		List<Filter> filters = new ArrayList<Filter>(1);
		filters.add(filter);
    	return filters;
    }
    
}
