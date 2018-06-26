package eu.sisob.resdownloader;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ConnectionType;

import java.io.BufferedReader;
import java.io.FileReader;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.SISOBProperties;

/**
 * 
 * @author hecking 
 */
public class Main {

    public static void main(String args[])
    {
        executeRwrapperManager();
    }


    public static void executeRwrapperManager() {
        String connectionType = SISOBProperties.getConnectionType();

        JsonObject command = new JsonObject();
        command.addProperty("agentid", "Result Downloader");

        AgentManager rWrapper = new ResultDownloaderManager(command, "Result Downloader Manager", ConnectionType.valueOf(connectionType));

        rWrapper.initialize();
        Thread runtime = new Thread(rWrapper);
        runtime.start();
    }
    
    public static String[] loadServerData() {
            String serverdata[] = new String[2];
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

