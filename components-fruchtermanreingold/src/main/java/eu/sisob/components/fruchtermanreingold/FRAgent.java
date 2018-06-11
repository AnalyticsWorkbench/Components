package eu.sisob.components.fruchtermanreingold;

import com.google.gson.JsonObject;
import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.VisualizationAgent;
import eu.sisob.api.visualization.VisualizationTechnique;

import eu.sisob.components.framework.json.util.JSONFile;

public class FRAgent extends VisualizationAgent{
	
	private NetworkParser parser;
	private FruchtermanReingoldTechnique technique;
	
	public FRAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}
	
	
	@Override
	public void createVisualization() throws Exception {	

		for(int i=0;i<super.getJsonNetworks().size();i++){
			JSONFile network = super.getJsonNetworks().get(i);
			parser = new SGFParser();
			parser.setNetwork(network);
			parser.parse();
			
			technique = new FruchtermanReingoldTechnique(network);
			technique.applyLayout(VisualizationTechnique.defaultWidth,VisualizationTechnique.defaultHeight);				
			parser.updateNodeSet(technique.updatedNodeSet());
			network.setTextContent(parser.encode());
		
		}
		
		saveVisualizationTechnique(getJsonNetworks());		
	}

	@Override
	public String getZipPath() {
		return "eu/sisob/components/fruchtermanreingold/technique.zip";
	}	

}
