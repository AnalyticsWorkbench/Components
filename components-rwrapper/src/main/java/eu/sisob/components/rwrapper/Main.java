package eu.sisob.components.rwrapper;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.util.ConnectionType;

import java.io.BufferedReader;
import java.io.FileReader;

import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.SISOBProperties;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author hecking
 */
public class Main extends Thread {

    InputStream is;
    
    public Main(InputStream is) {
        
        this.is = is;
    }
    public void run() {
        
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            String line = reader.readLine();
            
            while(line != null) {    
                System.out.println(line);
                line = reader.readLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();  
        }
    }
    
    public static void main(String args[]) {
        String RScriptExecutable = "C:/Program Files/R/R-3.2.4revised/bin/Rscript.exe";
        System.out.println(System.getProperty("user.home"));
        Main inputGobbler;
        Main errorGobbler;
        
        try {
            Process p = Runtime.getRuntime().exec(RScriptExecutable + " C:/Users/Yassin/Downloads/Uni/Hiwi/Workbench/Test_Scripts/test.R");
            
            inputGobbler = new Main(p.getInputStream());
            errorGobbler= new Main(p.getErrorStream());
            
            inputGobbler.start();
            errorGobbler.start();
            
            p.waitFor();

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void executeRwrapperManager() throws Exception {
        String connectionType = SISOBProperties.getConnectionType();

        JsonObject command = new JsonObject();
        command.addProperty("agentid", "R-Analysis");

        AgentManager rWrapper = new RWrapperManager(command, "R-wrapper Manager", ConnectionType.valueOf(connectionType));

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
