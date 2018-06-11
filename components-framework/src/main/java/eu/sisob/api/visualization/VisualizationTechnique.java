package eu.sisob.api.visualization;

import java.util.Vector;

import eu.sisob.components.framework.json.util.JSONFile;


public abstract  class VisualizationTechnique{
	
	private JSONFile network;
	private Vector<JSONFile> networks;
	private Vector<String> timeslices;
	private String layout;
	
	public static final int defaultWidth=1280;	
	public static final int defaultHeight=720;		
	
	
	public VisualizationTechnique(JSONFile network){		
		  this.network = network;	  
	}	
	
	public VisualizationTechnique(Vector<JSONFile>networks){
	    this.networks = networks;
	}
	
	public VisualizationTechnique(Vector<JSONFile>networks,String layout){
	    this.networks = networks;
	    this.setLayout(layout);
	}	
	
	public JSONFile getNetwork() {
		return network;
	}

	public void setNetwork(JSONFile network) {
		this.network = network;
	}

	public Vector<JSONFile> getNetworks() {
		return networks;
	}

	public void setNetworks(Vector<JSONFile> networks) {
		this.networks = networks;
	}

	/**
	 * @return the layout
	 */
	public String getLayout() {
		return layout;
	}

	/**
	 * @param layout the layout to set
	 */
	public void setLayout(String layout) {
		this.layout = layout;
	}

	public Vector<String> getTimeslices() {
		return timeslices;
	}

	public void setTimeslices(Vector<String> timeslices) {
		this.timeslices = timeslices;
	}

	public static int getDefaultwidth() {
		return defaultWidth;
	}

	public static int getDefaultheight() {
		return defaultHeight;
	}
}
