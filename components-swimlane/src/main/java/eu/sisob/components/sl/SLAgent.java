package eu.sisob.components.sl;

import com.google.gson.JsonObject;


import java.io.File;

import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.VisualizationAgent;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.JSONFile;

public class SLAgent extends VisualizationAgent{

	
	private NetworkParser parser;
	private SwimLaneTechnique technique;
	private String x_axsis_property; 	
	private String y_axsis_property;
	
	public SLAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}

	@Override
	public void createVisualization() throws Exception {
		// TODO Auto-generated method stub
		
		for(int i=0;i<super.getJsonNetworks().size();i++){
			JSONFile network = super.getJsonNetworks().get(i);		
			parser = new SGFParser();
			parser.setNetwork(network);
			parser.parse();		
			
			x_axsis_property = this.getFilterParameters().get(SLManager.X_AXIS_PROP).toString();
			y_axsis_property = this.getFilterParameters().get(SLManager.Y_AXIS_PROP).toString();
			technique  = new SwimLaneTechnique(network,y_axsis_property,x_axsis_property);
			technique.applyLayout();
			parser.updateMetadata(technique.updatedMetadata());
			parser.updateNodeSet(technique.updatedNodeSet());
			network.setTextContent(parser.encode());
		
		}	
		super.saveVisualizationTechnique(super.getJsonNetworks());
	}
	
	@Override
	public String getZipPath() {
		return "eu/sisob/components/sl/technique.zip";
	}

}
