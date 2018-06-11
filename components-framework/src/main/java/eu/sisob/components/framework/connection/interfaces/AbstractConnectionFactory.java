package eu.sisob.components.framework.connection.interfaces;

public interface AbstractConnectionFactory {

    MessageConnection getMessageConnection();

    DataConnection getDataConnection();
}
