/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.components.bipartite_cpm;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.FilterCategories;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.IntField;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.util.ConnectionType;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;

/**
 *
 * @author hecking
 */
public class BipCpmAgentManager extends AgentManager {

    private static final String MANAGER_NAME = "BipCpmAgentManager";
    public static final String INPUT_K = "k";
    public static final String INPUT_L = "l";

    public BipCpmAgentManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {

        super(commandMsg, mngId, connectionType);
    }

    protected void createAgent(JsonObject coordinationMsg) {
        // TODO Auto-generated method stub

        this.createAgent(new BipCpmAgent(coordinationMsg));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Filter> getFilterDescriptions() {

        String shortDescription = "Overlapping bipartite subgroup detection.";
        String longDescription = "For detailled information on the method see 'Biclique Communities' by Lehmann, Schwartz, Hansen (2008)";
        JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "graph"));
        JSONArray outputs = new JSONArray();
        outputs.add(new Output("out_1", "clustered graph."));

        JSONArray fields = new JSONArray();

        fields.add(new IntField("k", INPUT_K, true, 2));
        fields.add(new IntField("l", INPUT_L, true, 2));

        Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

        Filter filter = new Filter("Biclique Communities", FilterCategories.COMMUNITY_DETECTION, container);
        List<Filter> filters = new ArrayList<Filter>(1);
        filters.add(filter);
        return filters;
    }
}
