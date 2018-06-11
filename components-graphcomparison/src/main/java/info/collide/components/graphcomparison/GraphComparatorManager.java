/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.components.graphcomparison;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.BooleanField;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.FilterCategories;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.util.ConnectionType;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;

/**
 *
 * @author hecking
 */
public class GraphComparatorManager extends AgentManager {

    protected static final String BY_LABEL = "by label";
    protected static final String COMPONENT_NAME = "Merge and Compare Graphs";

    public GraphComparatorManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
        super(commandMsg, mngId, connectionType);
    }

    @Override
    protected void createAgent(JsonObject commandMsg) {
        System.out.println("create GraphAggregator agent");
        GraphComparisonAgent agent = new GraphComparisonAgent(commandMsg);
        this.createAgent(agent);
    }

    @Override
    protected List<Filter> getFilterDescriptions() {

        String shortDescription = "Merges two graphs and tags nodes and edges.";
        String longDescription = "This filter merges the two input graphs (or two lists of input graphs pairwise) combining nodes and edges. The resulting graph "
                + "has edge and node attributes indicating whether the node/edge exists in the first, second, or both input graphs. "
                + "All node and edge attributes of both input graphs are kept in the result, while the keys will have a postfix indicating "
                + "to which input graph the attribute value belongs, e.g. (attrX_in_ingraph1=6, attrX_in_ingraph2=3). "
                + "If the attribute is not present in both graphs NAs and NaNs will be inserted (attrY_in_ingraph1=9, attrX_in_ingraph2=NaN). "
                + "If nodes are not matched by label, the node ids are used instead";
        JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "graphs"));
        inputs.add(new Input("in_2", "graphs"));
        JSONArray outputs = new JSONArray();
        outputs.add(new Output("out_1", "merged graph"));

        JSONArray fields = new JSONArray();

        fields.add(new BooleanField("Match nodes by label", BY_LABEL, true, true));

        Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

        Filter filter = new Filter(COMPONENT_NAME, FilterCategories.NODE_EDGE_ANNOTATORS, container);
        List<Filter> filters = new ArrayList<Filter>(1);
        filters.add(filter);
        return filters;
    }
}
