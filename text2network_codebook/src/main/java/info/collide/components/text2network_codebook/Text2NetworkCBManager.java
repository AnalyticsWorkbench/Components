/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.components.text2network_codebook;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.componentdescription.Container;
import eu.sisob.components.framework.componentdescription.Filter;
import eu.sisob.components.framework.componentdescription.Input;
import eu.sisob.components.framework.componentdescription.Output;
import eu.sisob.components.framework.componentdescription.SelectField;
import eu.sisob.components.framework.util.ConnectionType;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.componentdescription.FilterCategories;
import eu.sisob.components.framework.componentdescription.IntField;

/**
 * @author doberstein
 */
public class Text2NetworkCBManager extends AgentManager {

    public static final String WINDOW_SIZE = "window_size";
    public static final String COMPONENT_NAME = "Text to Network (Codebook)";

    public Text2NetworkCBManager(JsonObject commandMsg, String mngId, ConnectionType connectionType) {
        super(commandMsg, mngId, connectionType);
        System.out.println("commandMsg: " + commandMsg);
    }

    @Override
    protected void createAgent(JsonObject commandMsg) {
        System.out.println("create Graph Generator agent");
//		Text2NetworkAgent agent = new Text2NetworkAgent(commandMsg);
//		this.createAgent(agent);
        Text2NetworkCBAgent agent = new Text2NetworkCBAgent(commandMsg);
        this.getAgents().add(agent);
        agent.setAgentListener(this);
        agent.initializeAgent();
        Thread runtime = new Thread(agent);
        runtime.start();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Filter> getFilterDescriptions() {

        String shortDescription = "This filter generates a graph for each input text.";
        String longDescription = "Concepts will be connected to a concept network if "
                + "they fit into a sliding window of the specified number of words and occur in a specified codebook. "
                + "A codebook has to be a CSV file with "
                + "three columns 'term', 'concept', 'category'. "
                + "Edge weights are calculated based on the number of dinstinct pairs of concepts found in the text using the sliding window appraoch. "
                + "Window size is ignored for ignored for Clause based network extraction. "
                + "The language of the input text has to be specified correctly for this filter to function properly";

        JSONArray inputs = new JSONArray();
        inputs.add(new Input("in_1", "texts"));
        inputs.add(new Input("in_2", "codebook"));
        JSONArray outputs = new JSONArray();
        outputs.add(new Output("out_1", "graphs"));

        JSONArray fields = new JSONArray();

        /*
		 * since the different algorithms need different inputs some fields are
		 * only used for certain algorithms the letter in the beginning of each
		 * field indicates for which algorithm the field is needed R=Random;
		 * P=Preferential-attachment; F=Forest-fire with the new UI, when it is
		 * possible to hide fields depending on the chosen method, this filter
		 * can be reworked to only show the fitting fields
         */
        fields.add(new IntField(WINDOW_SIZE, WINDOW_SIZE, true, 4));

        Container container = new Container(shortDescription, longDescription, inputs, outputs, fields);

        Filter filter = new Filter(COMPONENT_NAME, FilterCategories.MODELLING, container);
        List<Filter> filters = new ArrayList<Filter>(1);
        filters.add(filter);
        return filters;
    }

}
