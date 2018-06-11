package eu.sisob.api.visualization.technique.interfaces;

import java.util.Vector;

import eu.sisob.components.framework.json.util.JSONFile;

public interface DataSetUpdate {

	public Vector<JSONFile> updatedDataSets() throws Exception;

	
}
