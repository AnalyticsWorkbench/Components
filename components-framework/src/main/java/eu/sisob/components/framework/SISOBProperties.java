package eu.sisob.components.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import eu.sisob.components.framework.util.ConnectionType;

/**
 * Properties class to hold information about the SISOB system configuration.
 * Uses a {@link java.util.Properties} object for the internal storage of information.
 */
public class SISOBProperties {

    /**
     * <p>
     * List of reserved keys:
     * </p>
     * <ul>
     * <li>server.name</li>
     * <li>server.port</li>
     * </ul>
     */
    public static final List<String> reserved = Arrays.asList("connection.type, server.message, server.data, server.name", "server.port","components.thirdparty", "slideshow.sources", "slideshow.serverdir", "web.resulturl");
    /*
     * Default values for the respective properties
     */
    private static final String DEFAULT_SERVERPORT = "2525"; // FBA : 2525
    private static final String DEFAULT_SERVERNAME = "localhost";
    private static final String DEFAULT_INTERNALNAME = "workbench";
    private static final String DEFAULT_MANAGERNAMES = "Pajek Manager,Foresighted Graph Layout Manager,AutoMap Manager,ShowResult Manager,Data Uploader Manager,Conceptual Model To CSV Manager,Conceptual Model To CVN Manager,CSV To Conceptual Model Manager,CVN To Conceptual Model Manager,R Wrapper Manager,Crawler Manager,Gate Data Extractor Manager,Slideshow Manager,CVN 2 Pajek .net Manager,Pajek Script Builder Manager,Draw Network Manager, Core-Extractor Manager" ;
    private static final String DEFAULT_COMPONENTNAMES = "Pajek,ForesightedGraphLayout,AutoMap,ShowResult,Data Uploader,Conceptual Model To CSV,Conceptual Model To CVN,CSV To Conceptual Model,CVN To Conceptual Model,R-Analysis,Crawler,Gate Data Extractor,Slideshow,CVN 2 Pajek .net,Pajek Script Builder,Draw Network,Core-Extractor";
//    private static final String DEFAULT_RESULTSDIR = System.getProperty("user.dir") + File.separator + "results"; // FBA : this was privious addressing
    private static final String DEFAULT_RESULTSDIR = System.getProperty("user.dir") + File.separator + "results" + File.separator + "/";
    /** everything that is not equal to "true" is {@link Boolean#FALSE} */
    private static final String DEFAULTDEBUGMODE = "no"; 
    /**
     * File name of the configuration file to read (and write)
     */
    private static final String configFileName = "sisob.conf";

    /**
     * Complete path of the configuration file.<br/>
     * Is based on the current working directory + {@link #configFileName}.
     */
    private static final String configFilePath = System.getProperty("user.dir") + File.separator + configFileName;
    
    /**
     * Path that will contain the third party libs for example pajek, automap etc, etc.
     * 
     */
    private static final String analysisResultPath = System.getProperty("user.dir") + File.separator + "thirdparty" + File.separator+ "showresult" ;
    /**
     * Path that will contain the third party libs for example pajek, automap etc, etc.
     * 
     */
    private static final String thirdPartyPath = System.getProperty("user.dir") + File.separator + "thirdparty" + File.separator ;

    /**
     * {@link java.util.Properties} object which holds the actual configuration.
     */
    private static Properties properties;

    static {
    	if (properties == null) {

            initSISOBProperties();
        }
    }
    
    /**
     * Private constructor which is only to be used by {@link #initSISOBProperties()}}.<br/>
     * Tries to load an existing configuration from {@link #configFilePath}. If no existing
     * configuration file can be found defaults will be loaded (see {@link #()}
     * <ul>
     * <li>server.name = localhost</li>
     * <li>server.port = 2525</li>
     * </ul>
     */
    private static synchronized void initSISOBProperties() {
        properties = new Properties();
        System.out.print(configFilePath);
        // first try to read an existing config
        try {
            properties.load(new FileInputStream(configFilePath));
        } catch (FileNotFoundException e) {
            // this just means could not read anything from disk
            // we will use default values, so it is no problem
        } catch (IOException e) {
            // this just means could not read anything from disk
            // we will use default values, so it is no problem
        }
    }

    public static String getConnectionType() {
        return getProperty("connection.type", ConnectionType.SINGLE.name());
    }

    public static String getMessageBackend() {
        return getProperty("server.message");
    }

    public static String getMessageBackendUsername() {
        return getProperty("server.message.username");
    }

    public static String getMessageBackendPassword() {
        return  getProperty("server.message.password");
    }


    public static String getDataBackend() {
        System.out.print(properties);
        return getProperty("server.data");
    }

    public static String getDataBackendUsername() {
        return getProperty("server.data.username");
    }
    
    public static String getDataBackendPassword() {
        return getProperty("server.data.password");
    }
    
    public static String getDataServerName() {
        return getProperty("server.data.name");
    }

    public static void setDataServerName(String serverName) {
        properties.put("server.data.name", serverName);
    }

    public static int getDataPort() {
        return Integer.parseInt(getProperty("server.data.port"));
    }

    public static void setDataPort(int port) {
        properties.put("server.data.port", Integer.toString(port));
    }
    
    public static String getDataBackendInternalName() {
    	return getProperty("server.data.internalname", DEFAULT_INTERNALNAME);
    }
    
    public static void setDataBackendInternalName(String internalname) {
    	properties.put("server.data.internalname", internalname);
    }
    
    public static String getServerName() {
        return getProperty("server.message.name",DEFAULT_SERVERNAME);
    }

    public static void setServerName(String serverName) {
        properties.put("server.message.name", serverName);
    }
    
    public static String getMessageBackendInternalName() {
    	return getProperty("server.message.internalname", DEFAULT_INTERNALNAME);
    }

    public static void setMessageBackendInternalName(String internalname) {
    	setProperty("server.message.internalname", internalname);
    }

    public static int getServerPort() {
        return Integer.parseInt(getProperty("server.message.port",DEFAULT_SERVERPORT));
    }

    public static void setServerPort(int serverPort) {
        properties.put("server.message.port", Integer.toString(serverPort));
    }
    
    public static void setThirdPartyPath(String thirdPartyPath) {
        properties.put("components.thirdparty", thirdPartyPath);
    }
    
    public static String getThirdPartyPath() {
        return getProperty("components.thirdparty",thirdPartyPath);
    }
    
    public static void setAnalysisResultPath(String resultPath) {
        properties.put("components.analysisresult", resultPath);
    }
    
    public static String getAnalysisResultPath() {
        return getProperty("components.analysisresult",analysisResultPath);
    }

    /**
     * Delegates the call to the internal properties object.<br/>
     * set properties &gt; SystemProperty &gt; hardcoded defaultValue &gt; null
     * @see Properties#getProperty(String)
     */
    public static String getProperty(String key) {
        return properties.getProperty(key, System.getProperty(key));
    }
    /**
     * Delegates the call to the internal properties object.<br/>
     * set properties &gt; SystemProperty &gt; hardcoded defaultValue &gt; null
     * @see Properties#getProperty(String)
     */
    public static String getProperty(String key,String defaultValue) {
        return properties.getProperty(key,System.getProperty(key, defaultValue));
    }

    /**
     * Delegates the call to the internal properties object.
     * 
     * @see Properties#setProperty(String, String)
     * @throws IllegalArgumentException
     *             if key is in the reserved keys list {@link #reserved}.
     */
    public static void setProperty(String key, String value) {
        if (reserved.contains(key)) {
            throw new IllegalArgumentException("The key " + key + " is a reserved key, it may not be accessed directly");
        } else {
            properties.setProperty(key, value);
        }
    }

    public static List<String> getComponentNames() {
        ArrayList<String> names = new ArrayList<String>();
        String componentNames =  getProperty("components.names", DEFAULT_COMPONENTNAMES);
        StringTokenizer tokenizer = new StringTokenizer(componentNames, ",");
        while (tokenizer.hasMoreTokens()) {
            names.add(tokenizer.nextToken());
        }
        return names;
    }
    
    public static List<String> getComponentManagers() {
        ArrayList<String> managers = new ArrayList<String>();
        String managerNames =  getProperty("components.managers", DEFAULT_MANAGERNAMES);
        StringTokenizer tokenizer = new StringTokenizer(managerNames, ",");
        while (tokenizer.hasMoreTokens()) {
            managers.add(tokenizer.nextToken());
        }
        return managers;
    }
    
    public static void storeProperties() throws FileNotFoundException, IOException {
        properties.store(new FileOutputStream(configFilePath), "Config of the SISOB system");
    }
    
    public static String getSlideshowSourcePath() {
        return getProperty("slideshow.sources");
    }
    
    public static void setSlideshowSourcePath(String directoryName) {
        properties.setProperty("slideshow.sources", directoryName);
    }
    
    public static String getSlideshowServerPath() {
        return getProperty("slideshow.serverdir",DEFAULT_RESULTSDIR);
    }
    
    public static void setSlideshowServerPath(String directoryName) {
        properties.setProperty("slideshow.serverdir", directoryName);
    }
    
    public static String getResultUrl() {
        return getProperty("web.resulturl","https://"+SISOBProperties.getServerName()+"/results/");//FBA orginal was http
    }
    
    public static void setResultUrl(String resultUrl) {
    	properties.setProperty("web.resulturl", resultUrl);
    }
    
    public static String getResultLocation() {
        return getProperty("results.filelocation",DEFAULT_RESULTSDIR);
    }
    
    public static void setResultLocation(String resultLocation) {
    	properties.setProperty("results.filelocation", resultLocation);
    }
    /**
     * Defaults to {@link Boolean#FALSE}, if the config file does not contain 
     * <pre>components.<{@linkplain Class#getSimpleName()}>.debugmode=true</pre>
     * @param simpleClass
     * @return
     */
    public static boolean isDebugMode( Class simpleClass){
    		return Boolean.parseBoolean(getProperty("components."+simpleClass.getSimpleName()+".debugmode",DEFAULTDEBUGMODE));
    }
    
}
