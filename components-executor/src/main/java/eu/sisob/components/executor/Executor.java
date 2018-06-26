package eu.sisob.components.executor;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.util.ConnectionType;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.ComponentInterface;
import eu.sisob.components.framework.SISOBProperties;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * @author Alfredo Ramos
 * @author Per Verheyen
 * @author goehnert
 * @author manske
 */
public class Executor {

    private static final String defaultConfigFile = "execconfig.xml";
    private static final String execProperties = "executor.properties";
    private static String buildspecificConfigFileLocation;
    private static String connectionType;

    protected static final Logger logger = Logger.getLogger(Executor.class.getName());

    {
        boolean found = false;
        for (Handler h : logger.getHandlers()) {
            if (h instanceof ConsoleHandler) {
                found = true;
                h.setLevel(Level.ALL);
            }
        }
        if (!found) {
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(Level.ALL);
            logger.addHandler(ch);
        }
        logger.setLevel(Level.ALL);
    }

    public static void main(String args[]) {

        ArrayList<ComponentInterface> componentList = new ArrayList<ComponentInterface>();

        int selection;

        boolean runtime = true;

        ExecutorConfig configuration = null;

        connectionType = SISOBProperties.getConnectionType();

        // if postgresql is used as any communication channel -> init

        if (SISOBProperties.getMessageBackend().equals("postgresql") || SISOBProperties.getDataBackend().equals("postgresql")) {
        	try {
	        	PostgresInitializer postgresInitializer = new PostgresInitializer();
	        	postgresInitializer.initialize();
        	} catch (ClassNotFoundException ex) {
        		logger.log(Level.SEVERE, "Could not initialize PostgreSQL DBs", ex);
        		System.exit(-1);
        	}

        }

        String configFile = SISOBProperties.getProperty("executor.configuration.file");
        if (configFile == null || buildspecificConfigFileLocation != null) {
            //if the buildspecificConfigFileLocation starts with a $, then maven did not insert the property correctly
            if (buildspecificConfigFileLocation != null && !buildspecificConfigFileLocation.startsWith("$")) {
                configFile = buildspecificConfigFileLocation;
            } else {
                System.out.println("Could not find any build-specific execconfig. Probably the executor.properties file is missing or corrupt.");
                configFile = defaultConfigFile;
            }
        }
        try {
            configuration = new ExecutorConfig(configFile);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not find/open config file {0}", configFile);
            logger.log(Level.SEVERE, null, ex);
            System.err.println("Sorry, could not find the config '" + configFile + "' or it is not parseable!");
            System.exit(1);
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, "Could not configure the parser for the config file {0}", configFile);
            logger.log(Level.SEVERE, null, ex);
            System.err.println("Sorry, could not find the config '" + configFile + "' or it is not parseable!");
            System.exit(1);
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.err.println("Sorry, could not find the config '" + configFile + "' or it is not parseable!");
            System.exit(1);
        }

        System.out.println(" Starting Filter Components.");

        for (AgentInfo agent : configuration.getAgentInfos()) {
            if (agent.isAutostart()) {
                System.out.println("Starting manager: " + agent.getManagerName());
                executeComponent(agent, configuration, componentList);
            }
        }

        try {
            // print menu as greeting
            printMenu(configuration);
            while (runtime) {
                System.out.print("Please enter your choice: ");
                String input = new BufferedReader(new InputStreamReader(System.in)).readLine().toString();

                if (input.equals("m") || input.equals("?") || input.equals("h") || input.equals("help")) {
                    System.out.println("Printing choices again.");
                    printMenu(configuration);
                } // currently the configuration cannot be modified in here, so saving also not possible
                //                else if (input.equals("sc")) {
                //                    storeSISOBConfig();
                //                }
                else if (input.equals("lrc")) {
                    listRunningComponents(componentList);
                } else if (input.equals("eam")) {
                    startAllManagers(configuration, componentList);
                } else if (input.equals("q") || input.equals("bye")) {
                    runtime = false;
                } else {
                    String[] selections = input.split(",");
                    for (int i = 0; i < selections.length; i++) {
                        try {
                            selection = Integer.parseInt(selections[i]);
                        } catch (NumberFormatException n) {
                            selection = -1;
                        }
                        if (selection != 0) {
                            try {
                                executeComponent(selection, configuration, componentList);
                            } catch (IndexOutOfBoundsException e) {
                                System.out.println("Sorry, this is an unknown selection, printing choices again.");
                                printMenu(configuration);
                            }
                        } else {
                            runtime = false;
                        }
                    }
                }
            }
            System.out.println("Goodbye...");
            System.exit(0);
        } catch (Exception g) {
            g.printStackTrace();
        }
    }

    private static void storeSISOBConfig() {
        try {
            SISOBProperties.storeProperties();
            System.out.println("SISOB SYSTEM configuration has been saved!");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Unable to save SISOB System configuration");
            e.printStackTrace();
        }
    }

    private static void executeComponent(AgentInfo agentInfo, ExecutorConfig configuration, List<ComponentInterface> componentList) {
        String serverLocation = SISOBProperties.getServerName();
        int serverPort = SISOBProperties.getServerPort();

        ComponentInterface c = null;


        if (agentInfo.getFilterName() != null) {

            // managers for UI triggered components
            try {
                Class<?> managerClass = Class.forName(agentInfo.getManagerClassName());
                Constructor<?> constructor;
                Object o = null;
                constructor = managerClass.getConstructor(JsonObject.class, String.class, ConnectionType.class);
                o = constructor.newInstance(generateTemplateJson(agentInfo.getFilterName()), agentInfo.getManagerName(), ConnectionType.valueOf(connectionType));
                c = (AgentManager) o;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {

            // other managers / agents
            try {
                Class<?> componentClass = Class.forName(agentInfo.getManagerClassName());
                Constructor<?> constructor = componentClass.getConstructor(String.class, String.class, int.class);
                Object o = constructor.newInstance(agentInfo.getManagerName(), serverLocation, serverPort);
//                c = (InfrastructureComponent) o;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        if (c != null && !isTheComponentRunning(componentList, c)) {
            componentList.add(c);
            executeComponent(c);
        }

    }

    private static void executeComponent(int selection, ExecutorConfig configuration, List<ComponentInterface> componentList) {
        AgentInfo agentInfo = configuration.getAgentInfos().get(selection - 1);
        executeComponent(agentInfo, configuration, componentList);
    }

    private static void executeComponent(ComponentInterface component) {
        try {
            System.out.println("Initializing " + component.getClass().getName());
            component.initialize();
            if (component instanceof Runnable) {
                Thread runtime = new Thread((Runnable) component);
                runtime.start();
            }
        } catch (Exception tse) {
            logger.log(Level.SEVERE, "Could not initialize " + component.getManagerName(), tse);
        }
    }

    private static void startAllManagers(ExecutorConfig configuration, List<ComponentInterface> componentList) {
        for (int index = 1; index <= configuration.getAgentInfos().size(); index++) {
            executeComponent(index, configuration, componentList);
        }
    }

    private static JsonObject generateTemplateJson(String componentName) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("agentid", componentName);
        return jsonObject;
    }

    private static void listRunningComponents(ArrayList<ComponentInterface> componentList) {
        System.out.println(" ");
        System.out.println("Initiating Exploration...");
        System.out.println("Displaying Running Components:");
        System.out.println("------------------------------");

        for (ComponentInterface runningComponent : componentList) {
            System.out.println(" - " + runningComponent.getManagerName());
        }

        System.out.println("------------------------------");
        System.out.println("Exploration finished!");
        System.out.println("Returning to main menu...");
        System.out.println(" ");
    }

    private static void printMenu(ExecutorConfig configuration) {
        System.out.println(" ");
        System.out.println("Welcome to the SISOB system executor");
        System.out.println("Please select the SISOB agent manager you want to execute:");
        int indicator = 1;
        for (AgentInfo agent : configuration.getAgentInfos()) {
            System.out.println(indicator + " - " + agent.getManagerName().trim());
            indicator++;
        }
        System.out.println(" ");
        System.out.println("m - Show this menu");
        System.out.println("lrc - List running components");
        System.out.println("eam - Execute available managers");
        System.out.println("q - Terminate SISOB system execution");
        System.out.println("bye - Terminate SISOB system execution");
        System.out.println(" ");
    }

    private static boolean isTheComponentRunning(List<ComponentInterface> componentList, ComponentInterface c) {

        for (ComponentInterface runningComponent : componentList) {
            if (runningComponent.getManagerName().equals(c.getManagerName())) {
                System.out.println("");
                System.out.println(c.getManagerName() + " Is already running...");
                System.out.println("");
                return true;
            }
        }
        return false;
    }

}
