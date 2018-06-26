/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.components.graph_aggregator;

import com.github.rcaller.rStuff.RCode;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ClassLoaderUtility;
import eu.sisob.components.rwrapper.RWrapperAgent;

/**
 *
 * @author hecking
 */
public class GraphAggregatorAgent extends RWrapperAgent {

    protected String method;
    protected boolean byLabel;
    private static String INTERSECTION_METHOD = "intersection";
    private static String UNION_METHOD = "union";

    public GraphAggregatorAgent(JsonObject commandMsg) {
        super(commandMsg);

        this.method = this.getFilterParameters().get(GraphAggregatorManager.METHOD).toString();
        this.byLabel = Boolean.parseBoolean(this.getFilterParameters().get(GraphAggregatorManager.BY_LABEL).toString());

        this.scriptStream = ClassLoaderUtility
                .getClassLoader().getResourceAsStream("mergeGraphs.R");

        System.out.println("agent created!!!");
    }

    @Override
    protected void addScriptSpecificVariables(RCode code) {
        System.out.println("---------- add script specific variables ------------" + this.method);
        code.addRCode("byLabel <- " + (this.byLabel ? "TRUE" : "FALSE"));
        code.addRCode("doUnion <- " + (this.method.equals(UNION_METHOD) ? "TRUE" : "FALSE"));
    }
    // This methods overrides the parent methods only for the reason to 
    // delete old measure metadata.
//    @Override
//    protected boolean prepareAgentWorkspace(String data) {
//        
//        boolean success = super.prepareAgentWorkspace(data);
//        this.nodePropertyMetadata = new ArrayList<Property>();
//		this.edgePropertyMetadata = new ArrayList<Property>();
//        
//        return success;
//    }
}
