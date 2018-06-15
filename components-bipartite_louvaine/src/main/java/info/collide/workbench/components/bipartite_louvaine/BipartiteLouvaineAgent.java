/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package info.collide.workbench.components.bipartite_louvaine;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ClassLoaderUtility;
import eu.sisob.components.rwrapper.RWrapperAgent;

/**
 *
 * @author hecking
 */
public class BipartiteLouvaineAgent extends RWrapperAgent {

    public BipartiteLouvaineAgent(JsonObject commandMsg) {
        super(commandMsg);
        
        this.scriptStream = ClassLoaderUtility
                .getClassLoader().getResourceAsStream("bipartite_louvaine.R");

        System.out.println("agent created!!!");
    }
}
