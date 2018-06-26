//package eu.sisob.components.barchart;
//
//
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//
//import eu.sisob.components.framework.AgentManager;
//import eu.sisob.components.framework.SISOBProperties;
//
//public class Main {
//
//    public static void main(String args[]) {
//    	  String serverlocation = SISOBProperties.getServerName();
//          int port = SISOBProperties.getServerPort();
//          executeManager(serverlocation, port);  
//
//    }
//
//    public static void executeManager(String serverlocation, int port) {
//        AgentManager flm = new BarChartManager(new Tuple(String.class, Integer.class, Integer.class, String.class, "Bar Chart", String.class, String.class), "Bar Chart Manager", serverlocation, port);
//        flm.initialize();
//        Thread runtime = new Thread(flm);
//        runtime.start();
//
//    }
//
//}