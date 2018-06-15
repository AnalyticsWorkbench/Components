package info.collide.components.cliquepercolation;

import rcaller.RCaller;
import rcaller.RCode;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ClassLoaderUtility;

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

            this.executeAgent(commandMessage);
        }
    }

    @Override
    protected void addScriptSpecificVariables(RCode code) {

        code.addRCode("k <- " + this.k);
    }
}
