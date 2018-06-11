/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.workbench.components.swimlanevisualization;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.StringField;
import eu.sisob.components.framework.util.ConnectionType;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

/**
 *
 * @author hecking
 */
public class SLAgentManager extends AgentManager {

    public static final String X_PARAM = "x_param";
    public static final String Y_PARAM = "y_param";
    private static final String FILTER_NAME = "Swim Lanes";

    public SLAgentManager(JsonObject coordinationMessageTemplate, String managerName, ConnectionType connectionType) {
        super(coordinationMessageTemplate, managerName, connectionType);
    }

    @Override
    protected void createAgent(JsonObject coordinationMessage) {
        super.createAgent(new SLAgent(coordinationMessage));
    }

    @Override
    protected List<Filter> getFilterDescriptions() {

        String shortDescription = "The incoming network(s) are visualized using the swim lanes visualization technique.";

        String longDescription = "The swim lanes visualization technique has been designed for visualizing directed acyclic networks that are ordered (e.g., by a time or revision attribute -> x-axis) and that have groups of nodes belonging together (e.g., revisions of one article -> y-axis). If no attributes for ordering or grouping are given, the layout will be calculated autromatically to a best-fit.";

        JSONArray inputs = new JSONArray();

        inputs.add(new Input("in_1", "SISOB Graph Data"));

        JSONArray outputs = null;

        JSONArray fields = new JSONArray();

        fields.add(new StringField("Generate Y Axis Using Node Property:", Y_PARAM, false, "y"));

        fields.add(new StringField("Generate X Axis Using Node Property:", X_PARAM, false, "x"));

        Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

        Filter filter = new Filter(FILTER_NAME, "Graph Visualizations", container);

        List<Filter> filters = new ArrayList<Filter>(1);

        filters.add(filter);

        return filters;

    }

}
