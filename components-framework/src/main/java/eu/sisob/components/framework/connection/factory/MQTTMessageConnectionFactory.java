package eu.sisob.components.framework.connection.factory;

import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.connection.interfaces.AbstractConnectionFactory;
import eu.sisob.components.framework.connection.interfaces.DataConnection;
import eu.sisob.components.framework.connection.interfaces.MessageConnection;
import eu.sisob.components.framework.connection.mqtt.MqttMessageConnection;

public class MQTTMessageConnectionFactory implements AbstractConnectionFactory {
    @Override
    public MessageConnection getMessageConnection() {
        String server = SISOBProperties.getServerName();
        int port = SISOBProperties.getServerPort();
        return new MqttMessageConnection(server, port);
    }

    @Override
    public DataConnection getDataConnection() {
        return null;
    }
}
