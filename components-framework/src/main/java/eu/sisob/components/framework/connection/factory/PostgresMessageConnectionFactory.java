package eu.sisob.components.framework.connection.factory;

import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.connection.interfaces.AbstractConnectionFactory;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.connection.postgresql.PostgresqlMessageConnection;

public class PostgresMessageConnectionFactory implements AbstractConnectionFactory {

    @Override
    public MessageConnection getMessageConnection() {
        String server = SISOBProperties.getServerName();
        int port = SISOBProperties.getServerPort();
        String database = SISOBProperties.getMessageBackendInternalName();
        return new PostgresqlMessageConnection(server, port, database);

    }

    @Override
    public DataConnection getDataConnection() {
        return null;
    }
}
