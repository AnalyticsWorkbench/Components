package info.collide.workbench.components.mongoexporter;

import eu.sisob.components.framework.SISOBProperties;

/**
 * @author verheyen
 * 
 */
public class MExporterProps {

    private static final MExporterProps INSTANCE = new MExporterProps();

    public static final int QUERYMETHOD_DB = 0;
    public static final int QUERYMETHOD_REST = 1;

    private MExporterProps() {
    }

    public static MExporterProps getInstance() {
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
