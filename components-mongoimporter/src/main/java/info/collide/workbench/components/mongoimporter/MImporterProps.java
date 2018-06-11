package info.collide.workbench.components.mongoimporter;

import eu.sisob.components.framework.SISOBProperties;

/**
 * @author verheyen
 * 
 */
public class MImporterProps {

    private static final MImporterProps INSTANCE = new MImporterProps();

    public static final int QUERYMETHOD_DB = 0;
    public static final int QUERYMETHOD_REST = 1;

    private MImporterProps() {
    }

    public static MImporterProps getInstance() {
    	return INSTANCE;
    }

    public String getMongoServerHost() {
		return SISOBProperties.getProperty("mongoserver.name");
    }

    public int getMongoServerPort() {
    	return Integer.parseInt(SISOBProperties.getProperty("mongoserver.port"));
    }

    public String getMongoDbName() {
    	return SISOBProperties.getProperty("mongoserver.dbName");
    }

    public String getMongoDBCollectionName() {
		return SISOBProperties.getProperty("mongoserver.collectionName");
    }

}
