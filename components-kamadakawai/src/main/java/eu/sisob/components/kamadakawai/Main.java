//package eu.sisob.components.kamadakawai;
//
//import info.collide.sqlspaces.commons.Tuple;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//
//import eu.sisob.components.framework.AgentManager;
//
//public class Main {
//
//    public static void main(String args[]) {
//        String serverdata[] = loadServerData();
//        String serverlocation = serverdata[0];
//        int port = Integer.parseInt(serverdata[1]);
//        executeFileLoader(serverlocation, port);
//
//    }
//
//    public static void executeFileLoader(String serverlocation, int port) {
//        AgentManager flm = new KKManager(new Tuple(String.class, Integer.class, Integer.class, String.class, "Kamada-Kawai", String.class, String.class), "Kamada-Kawai Manager", serverlocation, port);
//        flm.initialize();
//        Thread runtime = new Thread(flm);
//        runtime.start();
//
//    }
//
//    public static String[] loadServerData() {
//        String serverdata[] = new String[2];
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader("server.conf"));
//            // server location
//            serverdata[0] = reader.readLine();
//            // port number
//            serverdata[1] = reader.readLine();
//
//            reader.close();
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return serverdata;
//    }
//}