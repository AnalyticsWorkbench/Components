package eu.sisob.components.stackedbarchart;

import com.google.gson.JsonObject;

import eu.sisob.api.visualization.VisualizationAgent;

public class StackedBarChartAgent extends VisualizationAgent {
	
	public StackedBarChartAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}

	@Override
	public String getZipPath() {
		return "eu/sisob/components/stackedbarchart/technique.zip";
	}

}
