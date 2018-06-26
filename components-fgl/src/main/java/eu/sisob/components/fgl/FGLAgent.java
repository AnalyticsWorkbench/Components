package eu.sisob.components.fgl;

import com.google.gson.JsonObject;


import java.io.File;
import java.util.Vector;

import eu.sisob.api.visualization.VisualizationAgent;
import eu.sisob.api.visualization.VisualizationTechnique;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.JSONFile;

public class FGLAgent extends VisualizationAgent{

	private FGLTechnique technique;	
	private Vector <JSONFile>updatedNetworks;
	private String layout;	
	private String stabilization;
	
	
	public static final String FGL_HOME = SISOBProperties.getThirdPartyPath() + "fgl" + File.separator;	 
	
	public FGLAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
		
        this.layout = this.getFilterParameters().get(FGLManager.LAYOUT).toString();
        this.stabilization = this.getFilterParameters().get(FGLManager.STABILIZATION_METHOD).toString();        
	}

	@Override
	public void createVisualization()throws Exception {			
		this.technique = new FGLTechnique(super.getJsonNetworks(),this.layout,this.stabilization);
		this.technique.applyLayout(VisualizationTechnique.defaultWidth, VisualizationTechnique.defaultHeight);
		this.updatedNetworks = this.technique.updatedNetworks();
		super.saveVisualizationTechnique(updatedNetworks);		
	}
	
	@Override
	public String getZipPath() {
		return "eu/sisob/components/fgl/technique.zip";
	}

}
