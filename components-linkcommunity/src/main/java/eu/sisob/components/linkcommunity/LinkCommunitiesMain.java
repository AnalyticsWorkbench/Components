//package eu.sisob.components.linkcommunity;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//
//import eu.sisob.components.framework.AgentManager;
//import info.collide.sqlspaces.commons.Tuple;
//
//public class LinkCommunitiesMain 
//{
//	public static void main(String args[]) 
//	{
//        String serverdata[] = loadServerData();
//        String serverlocation = serverdata[0];
//        int port = Integer.parseInt(serverdata[1]);
//        executeFileLoader(serverlocation, port);
//
//    }
//
//    public static void executeFileLoader(String serverlocation, int port) 
//    {
//        AgentManager lcam = new LinkCommunitiesAgentManager(new Tuple(String.class, Integer.class, Integer.class, String.class, "Link Communities Analysis", String.class, String.class), "Link Communities Analysis Manager", serverlocation, port);
//        lcam.initialize();
//        Thread runtime = new Thread(lcam);
//        runtime.start();
//
//    }
//
//    public static String[] loadServerData() 
//    {
//        String serverdata[] = new String[2];
//        try 
//        {
//            BufferedReader reader = new BufferedReader(new FileReader("server.conf"));
//            // server location
//            serverdata[0] = reader.readLine();
//            //serverdata[0] = "localhost";
//            // port number
//            serverdata[1] = reader.readLine();
//            //serverdata[1] = "2525";
//
//            reader.close();
//        } 
//        catch (Exception e) 
//        {
//            e.printStackTrace();
//        }
//        return serverdata;
//    }
//}
