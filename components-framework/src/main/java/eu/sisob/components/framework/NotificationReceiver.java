package eu.sisob.components.framework;

import com.google.gson.JsonObject;

import eu.sisob.components.framework.util.Command;

public interface NotificationReceiver {
	
	public void notifyMessage(Command command, JsonObject callback);

}
