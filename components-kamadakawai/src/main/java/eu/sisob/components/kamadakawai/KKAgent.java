package eu.sisob.components.kamadakawai;

import com.google.gson.JsonObject;


import java.io.File;

import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.VisualizationAgent;
import eu.sisob.api.visualization.VisualizationTechnique;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.JSONFile;

public class KKAgent extends VisualizationAgent{

	private KamadaKawaiTechnique technique;	
	private SGFParser parser;	
	
	public KKAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}

	@Override
	public void createVisualization() throws Exception{
		// TODO Auto-generated method stub
		
				for(int i=0;i<super.getJsonNetworks().size();i++){
					JSONFile network = super.getJsonNetworks().get(i);
					parser = new SGFParser();
					parser.setNetwork(network);
					parser.parse();					
					technique = new KamadaKawaiTechnique(network);
					technique.applyLayout(VisualizationTechnique.defaultWidth,VisualizationTechnique.defaultHeight);				
					parser.updateNodeSet(technique.updatedNodeSet());
					network.setTextContent(parser.encode());			
			
				}	
				super.saveVisualizationTechnique(super.getJsonNetworks());
	}
	
	@Override
	public String getZipPath() {
		return "eu/sisob/components/kamadakawai/technique.zip";
	}

}
