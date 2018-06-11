package eu.sisob.components.barchart;

import com.google.gson.JsonObject;


import eu.sisob.api.visualization.VisualizationAgent;

public class BarChartAgent extends VisualizationAgent {
	
	public BarChartAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}

	@Override
	public String getZipPath() {
		return "eu/sisob/components/bc/technique.zip";
	}

}
