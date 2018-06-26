package eu.sisob.components.circularlayout;

import com.google.gson.JsonObject;


import java.io.File;

import eu.sisob.api.visualization.VisualizationAgent;
import eu.sisob.components.framework.SISOBProperties;

public class CLAgent extends VisualizationAgent{

	public CLAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}
	
	@Override
	public String getZipPath() {
		return "eu/sisob/components/cl/technique.zip";
	}	
}
