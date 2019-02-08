package eu.sisob.components.centrality;

import java.io.BufferedReader;
import java.io.FileReader;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.util.ConnectionType;

/**
 * Hello world!
 */
public class App 
{
    public static void main( String[] args )
    {

        executeCentralityManager();
    }
    
    
    public static void executeCentralityManager() {
        String connectionType = SISOBProperties.getConnectionType();

        JsonObject command = new JsonObject();
        command.addProperty("agentid", "Centrality");

        AgentManager centrality = new CentralityManager(command, "Centrality Manager", ConnectionType.valueOf(connectionType));

        centrality.initialize();
        Thread runtime = new Thread(centrality);
        runtime.start();
    }
    
    public static String[] loadServerData() {
        String[] serverdata = new String[2];
            try {
                BufferedReader reader = new BufferedReader(new FileReader("server.conf"));
                // server location
                serverdata[0] = reader.readLine();
                // port number
                serverdata[1] = reader.readLine();

                reader.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return serverdata;
        }
    }
