/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.components.bipartite_cpm;

import com.github.rcaller.rStuff.RCode;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ClassLoaderUtility;
import eu.sisob.components.rwrapper.RWrapperAgent;

/**
 *
 * @author hecking
 */
public class BipCpmAgent extends RWrapperAgent {

    private int k;
    private int l;

    public BipCpmAgent(JsonObject commandMessage) {
        super(commandMessage);
        // TODO Auto-generated constructor stub	

        try {

            this.k = Integer.parseInt((String) this.getFilterParameters().get(BipCpmAgentManager.INPUT_K));
            this.l = Integer.parseInt((String) this.getFilterParameters().get(BipCpmAgentManager.INPUT_L));
        } catch (NumberFormatException e) {

            this.k = 0;
            this.l = 0;
        }

        this.scriptStream = ClassLoaderUtility
                .getClassLoader().getResourceAsStream("bipartite_cpm.R");

        logger.fine(this.agentName + ": agent created!!!");
    }

    @Override
    protected void addScriptSpecificVariables(RCode code) {
        
        code.addRCode("k <- " + this.k);
        code.addRCode("l <- " + this.l);
    }
}
