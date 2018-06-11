package eu.sisob.components.ff;

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
        executeFileLoader();
    }
    
    public static void executeFileLoader() {
        String connectionType = SISOBProperties.getConnectionType();

        JsonObject command = new JsonObject();
        command.addProperty("agentid", "Format Transformation");

        AgentManager flm = new FormatFactoryManager(command, "Format Factory Manager", ConnectionType.valueOf(connectionType));;

        flm.initialize();
        Thread runtime = new Thread(flm);
        runtime.start();

    }  
}
