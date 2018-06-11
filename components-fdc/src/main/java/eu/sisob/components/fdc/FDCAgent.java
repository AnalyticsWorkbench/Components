package eu.sisob.components.fdc;

import com.google.gson.JsonObject;
import info.collide.sqlspaces.commons.Tuple;

import java.io.File;

import eu.sisob.api.visualization.VisualizationAgent;
import eu.sisob.components.framework.SISOBProperties;

public class FDCAgent extends VisualizationAgent{

	public FDCAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}

	@Override
	public String getZipPath() {
		return "eu/sisob/components/fdc/technique.zip";
	}
}
