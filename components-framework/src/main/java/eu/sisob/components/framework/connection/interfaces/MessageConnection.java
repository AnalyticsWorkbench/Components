package eu.sisob.components.framework.connection.interfaces;

import com.google.gson.JsonObject;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentManager;
import eu.sisob.components.framework.util.Command;
import eu.sisob.components.framework.util.DataType;

/**
 * @author Stefan Remberg
 * Masterarbeit 15
 * Interface for the connection
 */
public interface MessageConnection {

    boolean setupConnection();

    void register(Command command, JsonObject event, Agent agent);

    void register(Command command, JsonObject event, AgentManager manager);

    boolean isConnected();

    void callManager(Command command, JsonObject callback);

    void callAgent(Command command, JsonObject callback);

    int writeDescription(JsonObject description);

    int update(DataType dataType, JsonObject object);

    int write(DataType type, JsonObject object);

    void shutdown();

    void unregister(JsonObject event);

    boolean isGlobal();

    void setIsGlobal(boolean global);
}
