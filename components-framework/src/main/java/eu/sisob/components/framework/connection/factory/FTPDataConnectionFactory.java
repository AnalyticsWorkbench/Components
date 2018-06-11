package eu.sisob.components.framework.connection.factory;

import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.connection.ftp.FtpDataConnection;
import eu.sisob.components.framework.connection.interfaces.AbstractConnectionFactory;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;

public class FTPDataConnectionFactory implements AbstractConnectionFactory {
    @Override
    public MessageConnection getMessageConnection() {
        return null;
    }

    @Override
    public DataConnection getDataConnection() {
        String username = SISOBProperties.getDataBackendUsername();
        String password = SISOBProperties.getDataBackendPassword();
        String server = SISOBProperties.getDataServerName();
        int port = SISOBProperties.getDataPort();
        return new FtpDataConnection(server, port, username, password);
    }
}
