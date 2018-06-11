package info.collide.workbench.components.mongoimporter;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.util.ConnectionType;

/**
 * @author verheyen
 * 
 */
public class RunMImporter {

    public static void main(String[] args) {
    	/*
		 *  create a component description. This has to be the same as the one defined in the filter parameters.
		 *  See {@link AgentManager}
		 */
		String componentName = MongoImporterManager.COMPONENT_NAME;

		/**
		 * Tell the manager which connection type we use.
		 * It can be SINGLE (single connection for manager and agents), MANAGER (one connection for manager and agents)
		 * or AGENTBUNDLE (shared connection with other analysis components.
		 */
		ConnectionType connectionType = ConnectionType.SINGLE;

		// create coordination message with id of manager, here we take the component name
		JsonObject coordinationMessage = new JsonObject();
		coordinationMessage.addProperty("agentid", componentName);

		MongoImporterManager manager = new MongoImporterManager(coordinationMessage, componentName, connectionType);
		// initialize
		manager.initialize();

		// create a thread for the manager ..
        if (manager instanceof Runnable) {
            Thread runtime = new Thread((Runnable) manager);
            runtime.start();
        }
    }
}
