package eu.sisob.api.parser;

import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

public abstract class NetworkParser {

	// The network data to be parsed
	protected JSONFile network;
	// The network edge set
	protected EdgeSet edgeset;
	// The network node set
	protected NodeSet nodeset;
	// The network metadata
	protected Metadata metadata;
	// The network type : 2 mode or 1 mode
	// Indicates if the network is directed or not
	protected String directedNetwork;	
	// The internal time indicator of the network parser.
	protected int timeIndicator = 0;
	
	protected int dataIndex = -1;
	
	public static final String DIRECTED_NETWORK = "true";
	
	public static final String UNDIRECTED_NETWORK = "false";
	
	/**
	 * Basic constructor of the network parser class.
	 */
	public NetworkParser(){
		
	}
	/**
	 * Constructor of the network parse class. This one uses network data information as parameter 
	 * @param network network data information
	 */
	public NetworkParser(JSONFile network){
		this.network = network;
	}
	
	/**
	 * The parse method of the NetworkParser class.
	 * Depending on the specialized type of parser, this method will transform 
	 * the given network data into the appropriate format
	 */
	public abstract void parse() throws IllegalContentTypeException;	
	
	/**
	 * The printNodeSet method prints the parsed node set.
	 */
	public void printNodeSet(){
		System.out.println("-----NodeSet-----");
		for(int i=0;i<this.nodeset.getValues().size();i++){
			System.out.println(nodeset.getNodeSetAsJSON().get(i).toString());
		}
		System.out.println("-----------------");
	}
	/**
	 * The printEdgeSet method prints the parsed edge set.
	 */
	public void printEdgeSet(){
		System.out.println("-----EdgeSet-----");
		for(int i=0;i<this.edgeset.getValues().size();i++){
			System.out.println(edgeset.getEdgeSetAsJSON().get(i).toString());
		}
		System.out.println("-----------------");
	}
	/**
	 *  The printMetaData method prints the parsed metadata 
	 */
	public void printMetaData(){
		System.out.println("-----Metadata-----");
		System.out.println("Title: "+metadata.getTitle());
		System.out.println("Datalink index: "+metadata.getDataIndex());
		System.out.println("Is network directed: "+metadata.isDirectedNetwork());
		System.out.println("Is network description: "+metadata.getNetworkDescription());		
		System.out.println("-------------------");
	}	

	/**
	 * The abstract method encode.
	 * This method will be called on the specialized parsers
	 * in order to have a JSON representation of the parsed network.
	 * @return the encoded JSON String of the given network
	 */
	public abstract String encode();	

	public abstract String requestFileTypeExtension();
	
	/**
	 * This method will return a string related to the file type
	 * @return a string containing the file tipe in the format "filetype"
	 */
	public abstract String requestFileType();
	
	/**
	 *  The getNetwork method
	 * @return a JSON file containing the network data
	 */
	public JSONFile getNetwork() {
		return network;
	}
	/**
	 *  The setNetwork method.
	 *  Sets the network data on the parser
	 *  @param network data
	 */
	public void setNetwork(JSONFile network) {
		this.network = network;
	}
	
    /**
     * The getDirectedNetwork method
     * @return true if the network is directed or false if is not.
     */
	public String getDirectedNetwork() {
		return directedNetwork;
	}
	
	
	/**
	 * The SetDirectedNetwork method
	 * sets the network parser the information 
	 * about a network if it is directed or not
	 * @param directedNetwork
	 */
	public void setDirectedNetwork(String directedNetwork) {
		this.directedNetwork = directedNetwork;
	}
	
	/**  
	 * @return the parsed NodeSet object
	 */	
	public NodeSet getParsedNodeSet(){
		return this.nodeset;
	}
	
	/**
	 * Replaces the current NodeSet object from the Network Parser with the given parameter
	 * @param nodeset
	 */
	public void updateNodeSet(NodeSet nodeset){
		this.nodeset = nodeset;
	}
	
	/**
	 * @return the parsed EdgeSet object
	 */
	public EdgeSet getParsedEdgeSet(){
		return this.edgeset;
	}
	
	/**
	 * Replaces the current EdgeSet object from the Network Parser with the given parameter
	 * @param edgeset
	 */
	public void updateEdgeSet(EdgeSet edgeset){
		this.edgeset = edgeset;
	}
	
	/**
	 * @return the Metadata object
	 */
	public Metadata getParsedMetadata(){
		return this.metadata;
	}
	
	/**
	 * Replaces the current Metadata object from the Network Parser with the given parameter
	 * @param metadata
	 */
	public void updateMetadata(Metadata metadata){
		this.metadata = metadata;
	}
	
	public String splitBy(String data){
		
		if(data.indexOf("\r")!=-1)
			return "\r";
		
		return "\n";
	}
	
}
