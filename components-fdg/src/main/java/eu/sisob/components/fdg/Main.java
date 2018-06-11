package eu.sisob.components.fdg;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.util.ConnectionType;

public class Main {

    public static void main(String args[]) {
System.out.println("Starting Tutorial Manager...");
		
		/*
		 *  create a component description. This has to be the same as the one defined in the filter parameters.
		 *  See {@link AgentManager}
		 */
		String componentName = FDGManager.COMPONENT_NAME;

		/**
		 * Tell the manager which connection type we use.
		 * It can be SINGLE (single connection for manager and agents), MANAGER (one connection for manager and agents)
		 * or AGENTBUNDLE (shared connection with other analysis components.
		 */
		ConnectionType connectionType = ConnectionType.SINGLE;

		// create coordination message with id of manager, here we take the component name
		JsonObject coordinationMessage = new JsonObject();
		coordinationMessage.addProperty("agentid", componentName);

		FDGManager fdgManager = new FDGManager(coordinationMessage, componentName, connectionType);
		// initialize
		fdgManager.initialize();

		// create a thread for the manager ..
        if (fdgManager instanceof Runnable) {
            Thread runtime = new Thread((Runnable) fdgManager);
            runtime.start();
        }
        
        System.out.println("Tutorial Manager running ...");
    }

}