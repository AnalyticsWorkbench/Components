package eu.sisob.components.supergraph;

import com.google.gson.JsonObject;
import info.collide.sqlspaces.commons.Tuple;

import java.io.File;
import java.util.Vector;

import eu.sisob.api.visualization.VisualizationAgent;
import eu.sisob.api.visualization.VisualizationTechnique;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.JSONFile;

public class SuperGraphAgent extends VisualizationAgent{

	private SuperGraph technique;	
	private Vector <JSONFile>updatedNetworks;
	private String layout;	
	
	public static final String SG_HOME = SISOBProperties.getThirdPartyPath() + "sg" + File.separator;	 

	
	public SuperGraphAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
		     
		this.layout = this.getFilterParameters().get(SuperGraphManager.METHOD).toString();

	}

	@Override
	public void createVisualization() throws Exception {		
			this.technique = new SuperGraph(super.getJsonNetworks(),this.layout);
			this.technique.applyLayout(VisualizationTechnique.defaultWidth, VisualizationTechnique.defaultHeight);
			this.updatedNetworks = this.technique.updatedNetworks();
			super.saveVisualizationTechnique(updatedNetworks);
			
	}
	
	@Override
	public String getZipPath() {
		return "eu/sisob/components/supergraph/technique.zip";
	}	

}
