package info.collide.workbench.components.isolatednodefilter;


import com.google.gson.JsonObject;

import eu.sisob.components.framework.util.ConnectionType;
import info.collide.sqlspaces.commons.Tuple;

public class Starter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		executeComponent();
	}
	
	private static void executeComponent() {
		/*
		 *  create a component description. This has to be the same as the one defined in the filter parameters.
		 *  See {@link AgentManager}
		 */
		String componentName = SingleNodeFilterManager.COMPONENT_NAME;

		/**
		 * Tell the manager which connection type we use.
		 * It can be SINGLE (single connection for manager and agents), MANAGER (one connection for manager and agents)
		 * or AGENTBUNDLE (shared connection with other analysis components.
		 */
		ConnectionType connectionType = ConnectionType.SINGLE;

		// create coordination message with id of manager, here we take the component name
		JsonObject coordinationMessage = new JsonObject();
		coordinationMessage.addProperty("agentid", componentName);

		SingleNodeFilterManager tutorialManager = new SingleNodeFilterManager(coordinationMessage, componentName, connectionType);
		// initialize
		tutorialManager.initialize();

		// create a thread for the manager ..
        if (tutorialManager instanceof Runnable) {
            Thread runtime = new Thread((Runnable) tutorialManager);
            runtime.start();
        }
    }

	 private static Tuple generateTemplateTuple(String componentName) {
	        return new Tuple(String.class, Integer.class, Integer.class, String.class, componentName, String.class, String.class);
	 }
	
}
