package info.collide.components.cliquepercolation;

import eu.sisob.api.parser.gml.GMLNetworkParser;
import info.collide.sqlspaces.commons.Tuple;
import info.collide.util.ClassLoaderUtility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;

import rcaller.RCaller;
import rcaller.RCode;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.components.framework.AgentProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import eu.sisob.components.rwrapper.RWrapperAgent;
import org.json.simple.JSONValue;

import com.google.gson.JsonObject;

import rcaller.exception.RCallerExecutionException;

public class CliquepercolationAgent extends RWrapperAgent {

    private int k;

    private static final String SCRIPT = "cpm.R";

    public CliquepercolationAgent(JsonObject coordinationMessage) {
        super(coordinationMessage);
        // TODO Auto-generated constructor stub

        this.k = Integer.parseInt(this.getFilterParameters().get(CliquepercolationManager.K).toString());

        this.scriptStream = ClassLoaderUtility
                .getClassLoader().getResourceAsStream(SCRIPT);
    }

    @Override
    public void executeAgent(JsonObject commandMessage) {

        if (this.k < 2) {

            this.indicateError("K has to be a number > 2.");
        } else {

            super.executeAgent(commandMessage);
        }
    }

    @Override
    protected void addScriptSpecificVariables(RCode code) {

        code.addRCode("k <- " + this.k);
    }
}
