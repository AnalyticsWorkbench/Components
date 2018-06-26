package eu.sisob.components.tableviewer;

import java.io.BufferedReader;
import java.io.FileReader;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.SISOBProperties;

/**
 * 
 * @author detjen 
 */
public class Main {

    public static void main(String args[])
    {
    	 String serverlocation = SISOBProperties.getServerName(); 
         // int port = Integer.parseInt(serverdata[1]);
           int port = SISOBProperties.getServerPort();
        executeRwrapperManager(serverlocation, port);        
        
    }


    public static void executeRwrapperManager(String serverlocation, int port) {
//        AgentManager rWrapper = new TableViewerManager(new Tuple(String.class, Integer.class, Integer.class, String.class, "Table Viewer", String.class, String.class), "Table Viewer Manager", serverlocation, port);
//        rWrapper.initialize();
//        Thread runtime = new Thread(rWrapper);
//        runtime.start();
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

