package eu.sisob.components.framework.connection.factory;

import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.connection.interfaces.AbstractConnectionFactory;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.connection.postgresql.PostgresqlDataConnection;

public class PostgresDataConnectionFactory implements AbstractConnectionFactory {
    @Override
    public MessageConnection getMessageConnection() {
        return null;
    }

    @Override
    public DataConnection getDataConnection() {
        String server = SISOBProperties.getDataServerName();
        int port = SISOBProperties.getDataPort();
        String database = SISOBProperties.getDataBackendInternalName();
        return new PostgresqlDataConnection(server, port, database);
    }
}
