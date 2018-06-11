package eu.sisob.api.visualization.technique.interfaces;

import java.util.Vector;

import eu.sisob.components.framework.json.util.JSONFile;

public interface NetworkUpdate {

	public Vector<JSONFile>updatedNetworks() throws Exception;

	
}
