package eu.sisob.components.framework.connection.interfaces;

import com.google.gson.JsonObject;

public interface DataConnection {

    boolean setupConnection();

    void writeData(String path, String payload);

    String readData(String path);

    void removeData(String path);

    void shutdown();

}
