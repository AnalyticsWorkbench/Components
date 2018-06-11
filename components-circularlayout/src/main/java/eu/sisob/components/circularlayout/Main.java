//package eu.sisob.components.circularlayout;
//
//import info.collide.sqlspaces.commons.Tuple;
//import eu.sisob.components.framework.AgentManager;
//import eu.sisob.components.framework.SISOBProperties;
//
//public class Main {
//
//    public static void main(String args[]) {
//    	  String serverlocation = SISOBProperties.getServerName();
//          int port = SISOBProperties.getServerPort();
//          executeManager(serverlocation, port);  
//    }
//
//    public static void executeManager(String serverlocation, int port) {
//        AgentManager flm = new CLManager(new Tuple(String.class, Integer.class, Integer.class, String.class, "Circular Layout", String.class, String.class), "Circular Layout Manager", serverlocation, port);
//        flm.initialize();
//        Thread runtime = new Thread(flm);
//        runtime.start();
//
//    }
//
//}