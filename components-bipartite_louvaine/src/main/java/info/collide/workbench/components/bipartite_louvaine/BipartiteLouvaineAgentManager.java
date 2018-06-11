/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.workbench.components.bipartite_louvaine;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.util.ConnectionType;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;

/**
 *
 * @author hecking
 */
public class BipartiteLouvaineAgentManager extends AgentManager {

    private static final String COMPONENT_NAME = "Bipartite Modularity Optimization";

    public BipartiteLouvaineAgentManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {

        super(commandMsg, mngId, connectionType);
    }

    @Override
    protected void createAgent(JsonObject coordinationMsg) {
        // TODO Auto-generated method stub

        System.out.println("create bip-louvaine agent");
        this.createAgent(new BipartiteLouvaineAgent(coordinationMsg));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Filter> getFilterDescriptions() {

        String shortDescription = "The bipartite input graphs are clutered using bipartite modularity optimization.";
        String longDescription = "For detailled information on the method see 'Incremental Clustering of Dynamic Bipartite Networks' by Hecking et al. (2014)";
        JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "graph"));
        JSONArray outputs = new JSONArray();
        outputs.add(new Output("out_1", "clustered graph."));

        JSONArray fields = new JSONArray();

        Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

        Filter filter = new Filter(COMPONENT_NAME, "Community Detection", container);
        List<Filter> filters = new ArrayList<Filter>(1);
        filters.add(filter);
        return filters;
    }
}
