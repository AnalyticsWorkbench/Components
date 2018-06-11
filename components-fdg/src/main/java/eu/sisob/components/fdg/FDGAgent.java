package eu.sisob.components.fdg;

import com.google.gson.JsonObject;

import eu.sisob.api.visualization.VisualizationAgent;

public class FDGAgent extends VisualizationAgent {	
	
	public FDGAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}

	@Override
	public String getZipPath() {
		return "eu/sisob/components/fdg/technique.zip";
	}

}
