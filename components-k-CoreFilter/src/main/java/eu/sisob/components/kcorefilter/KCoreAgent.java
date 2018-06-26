package eu.sisob.components.kcorefilter;

import com.github.rcaller.rStuff.RCode;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ClassLoaderUtility;
import eu.sisob.components.rwrapper.RWrapperAgent;

/**
 * 
 * @author hecking 
 */
public class KCoreAgent extends RWrapperAgent {

    private int k;
	private static final String SCRIPT = "extract_k_core.R";

    public KCoreAgent(JsonObject commandMsg) {
		super(commandMsg);

//		this.clustCount = this.getFilterParameters().get(BlockmodelManager.CLUSTER_COUNT).toString();
//            String[] params = this.getParamsList(commandTuple.getField(6).getValue().toString());
        
		this.k = Integer.parseInt(this.getFilterParameters().get(KCoreManager.K).toString());
		this.scriptStream = ClassLoaderUtility.getClassLoader().getResourceAsStream(SCRIPT);
    }

    @Override
    public void addScriptSpecificVariables(RCode code){
		code.addRCode("k <- " + this.k);
	}
}