package info.collide.components.tutorial;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.util.ConnectionType;

/**
 * Usually analysis components are started by the executor. This class can be used to start an analysis component manually.
 * @author remberg
 */
public class TutorialStarter {

	public static void main(String[] args) {

		System.out.println("Starting Tutorial Manager...");
		
		/*
		 *  create a component description. This has to be the same as the one defined in the filter parameters.
		 *  See {@link AgentManager}
		 */
		String componentName = TutorialManager.COMPONENT_NAME;

		/**
		 * Tell the manager which connection type we use.
		 * It can be SINGLE (single connection for manager and agents), MANAGER (one connection for manager and agents)
		 * or AGENTBUNDLE (shared connection with other analysis components.
		 */
		ConnectionType connectionType = ConnectionType.SINGLE;

		// create coordination message with id of manager, here we take the component name
		JsonObject coordinationMessage = new JsonObject();
		coordinationMessage.addProperty("agentid", componentName);

		TutorialManager tutorialManager = new TutorialManager(coordinationMessage, componentName, connectionType);
		// initialize
		tutorialManager.initialize();

		// create a thread for the manager ..
        if (tutorialManager instanceof Runnable) {
            Thread runtime = new Thread((Runnable) tutorialManager);
            runtime.start();
        }
        
        System.out.println("Tutorial Manager running ...");
	}

}
