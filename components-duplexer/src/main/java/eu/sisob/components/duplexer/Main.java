package eu.sisob.components.duplexer;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.connection.factory.ConnectionClient;
import eu.sisob.components.framework.util.ConnectionType;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.SISOBProperties;


/**
 * Hello world!
 *
 */
public class Main 
{
    public static void main( String[] args )
    {
    	  String serverlocation = SISOBProperties.getServerName(); 
         // int port = Integer.parseInt(serverdata[1]);
           int port = SISOBProperties.getServerPort();
        executeFileLoader(serverlocation, port);
    }
    
    public static void executeFileLoader(String serverlocation, int port) {
        String connectionType = SISOBProperties.getConnectionType();

        JsonObject command = new JsonObject();
        command.addProperty("agentid", "Duplicator");
        AgentManager flm = new DuplexerManager(command, "Duplicator Manager", ConnectionType.valueOf(connectionType));

        flm.initialize();
        Thread runtime = new Thread(flm);
        runtime.start();

    }  
}
