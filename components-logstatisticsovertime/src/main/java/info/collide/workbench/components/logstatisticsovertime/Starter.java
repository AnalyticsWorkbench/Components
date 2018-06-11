package info.collide.workbench.components.logstatisticsovertime;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.util.ConnectionType;

public class Starter {

	public static void main(String[] args) {
		executeComponent();
	}

	private static void executeComponent() {
		System.out.println("Starting Tutorial Manager...");

		/*
		 * create a component description. This has to be the same as the one
		 * defined in the filter parameters. See {@link AgentManager}
		 */
		String componentName = "Log Statistics Over Time";

		/**
		 * Tell the manager which connection type we use. It can be SINGLE
		 * (single connection for manager and agents), MANAGER (one connection
		 * for manager and agents) or AGENTBUNDLE (shared connection with other
		 * analysis components.
		 */
		ConnectionType connectionType = ConnectionType.MANAGER;

		// create coordination message with id of manager, here we take the
		// component name
		JsonObject coordinationMessage = new JsonObject();
		coordinationMessage.addProperty("agentid", componentName);

		LogStatisticsOverTimeManager manager = new LogStatisticsOverTimeManager(coordinationMessage,
				componentName, connectionType);
		// initialize
		manager.initialize();

		// create a thread for the manager ..
		if (manager instanceof Runnable) {
			Thread runtime = new Thread((Runnable) manager);
			runtime.start();
		}

		System.out.println("Tutorial Manager running ...");
	}

}
