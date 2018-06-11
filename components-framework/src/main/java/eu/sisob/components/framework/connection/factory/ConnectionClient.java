package eu.sisob.components.framework.connection.factory;

import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.connection.ftp.FtpDataConnection;
import eu.sisob.components.framework.connection.interfaces.AbstractConnectionFactory;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.connection.mqtt.MqttMessageConnection;
import eu.sisob.components.framework.connection.postgresql.PostgresqlDataConnection;
import eu.sisob.components.framework.connection.postgresql.PostgresqlMessageConnection;

public class ConnectionClient {

    private MessageConnection globalMessageConnection;

    private DataConnection globalDataConnection;

    private AbstractConnectionFactory abstractMessageConnectionFactory;

    private AbstractConnectionFactory abstractDataConnectionFactory;

    private ConnectionClient() {
        String msgBackend = SISOBProperties.getMessageBackend();
        String dataBackend = SISOBProperties.getDataBackend();

        if (msgBackend.equalsIgnoreCase("mqtt"))
            abstractMessageConnectionFactory = new MQTTMessageConnectionFactory();
        else if (msgBackend.equalsIgnoreCase("postgresql"))
            abstractMessageConnectionFactory = new PostgresMessageConnectionFactory();

        if (dataBackend.equalsIgnoreCase("ftp"))
            abstractDataConnectionFactory = new FTPDataConnectionFactory();
        else if (dataBackend.equalsIgnoreCase("postgresql"))
            abstractDataConnectionFactory = new PostgresDataConnectionFactory();

        if (globalMessageConnection == null) {
            globalMessageConnection = abstractMessageConnectionFactory.getMessageConnection();
            globalMessageConnection.setIsGlobal(true);
            globalMessageConnection.setupConnection();
        }

        if (globalDataConnection == null) {
            globalDataConnection = abstractDataConnectionFactory.getDataConnection();
        }
    }

    public MessageConnection getGlobalMessageConnection() {
        return globalMessageConnection;
    }

    public void setGlobalMessageConnection(MessageConnection globalMessageConnection) {
        this.globalMessageConnection = globalMessageConnection;
    }

    public DataConnection getGlobalDataConnection() {
        return globalDataConnection;
    }

    public void setGlobalDataConnection(DataConnection globalDataConnection) {
        this.globalDataConnection = globalDataConnection;
    }

    private static class LazyHolder {
        public static final ConnectionClient INSTANCE = new ConnectionClient();
    }

    public DataConnection getDataConnection() {
        return abstractDataConnectionFactory.getDataConnection();
    }

    public MessageConnection getMessageConnection() {
        return abstractMessageConnectionFactory.getMessageConnection();
    }

    public static ConnectionClient getInstance() {
        return LazyHolder.INSTANCE;
    }

}
