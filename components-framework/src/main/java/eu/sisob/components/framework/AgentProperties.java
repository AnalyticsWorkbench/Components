package eu.sisob.components.framework;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Props
 * 
 * @author Verheyen
 * 
 */

// TODO to be continued (config file, etc.)...
public class AgentProperties {

    @Deprecated
    public static String SEPERATOR = System.getProperty("file.separator");

    public static String SEPARATOR = System.getProperty("file.separator");

    public static String PATH_RESOURCES = "Resources" + SEPARATOR;

    public static String ABSOLUTE_PATH_RESOURCES = System.getProperty("user.dir") + SEPARATOR + PATH_RESOURCES;

    public static String DEFAULT_FILEPATH = System.getProperty("user.home");

    protected final static Properties config = new Properties();

    public static void loadConfiguration(String filename, boolean reportErrors) {
        filename = filename.replace('/', File.separatorChar);
        filename = filename.replace('\\', File.separatorChar);
        try {
            config.load(new FileInputStream(filename));
        } catch (Exception e) {
            if (reportErrors) {
                Logger logger = Logger.getLogger("eu.sisob.components");
                logger.fine("Could not load config file: " + filename);
                logger.fine(e.getMessage());
            }
            return;
        }
    }

}
