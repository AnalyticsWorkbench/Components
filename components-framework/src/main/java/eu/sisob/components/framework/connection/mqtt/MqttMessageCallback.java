package eu.sisob.components.framework.connection.mqtt;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.sisob.components.framework.util.Command;

/**
 * Mqtt callback for Managers and agents.
 *
 * @author Stefan Remberg
 */
public class MqttMessageCallback implements org.eclipse.paho.client.mqttv3.MqttCallback {

    private JsonParser jsonParser;
    private MqttMessageConnection mqttConnection;
    private static final String AGENTDESCRIPTION_DELETE = "AGENTDESCRIPTION/DELETE";
    private static final String COMMAND_WRITE = "COORDINATION/NEW";
    private static final String DATA_WRITE = "DATA";

    protected static Logger logger = Logger.getLogger(MqttMessageCallback.class.getName());

    {
        boolean found = false;
        for (Handler h : logger.getHandlers()) {
            if (h instanceof ConsoleHandler) {
                found = true;
                h.setLevel(Level.ALL);
            }
        }
        if (!found) {
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(Level.ALL);
            logger.addHandler(ch);
        }
        logger.setLevel(Level.ALL);

    }

    public MqttMessageCallback(MqttMessageConnection mqttConnection) {

        this.jsonParser = new JsonParser();
        this.mqttConnection = mqttConnection;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        logger.log(Level.SEVERE, throwable.getMessage());
        throwable.printStackTrace();
        mqttConnection.reconnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

        // parse msg from mqtt to jsonobject
        JsonObject msg = (JsonObject) jsonParser.parse(mqttMessage.toString());

        if (COMMAND_WRITE.equals(topic))
            mqttConnection.callManager(Command.WRITE, msg);
        else if (AGENTDESCRIPTION_DELETE.equals(topic))
            mqttConnection.callManager(Command.DELETE, msg);
        else if (DATA_WRITE.equals(topic))
            mqttConnection.callAgent(Command.WRITE, msg);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
