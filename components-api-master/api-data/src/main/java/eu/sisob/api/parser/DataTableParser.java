package eu.sisob.api.parser;

import eu.sisob.api.visualization.format.datatable.Dataset;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

public abstract class DataTableParser {
	
	// The network data to be parsed
	protected JSONFile tabledata;
	// The network data set
	protected Dataset dataset;
	// The network metadata
	protected Metadata metadata;
	// the internal time indicator
	protected int timeIndicator = 0;
	
	protected int dataIndex = -1;
	
	/**
	 * DataTableParser constructor.
	 */
	public DataTableParser(){
		
	}
	/**
	 * DataTableParser constructor
	 * @param network
	 */
	public DataTableParser(JSONFile network){
		this.tabledata = network;
	}
	
	/**
	 * The parse method of the DataTableParser class.
	 * Depending on the specialized type of parser, this method will transform 
	 * the given network data into the appropriate format
	 */

	public abstract void parse() throws IllegalContentTypeException;	

	
	/**
	 * The abstract method encode.
	 * This method will be called on the specialized parsers
	 * in order to have a JSON representation of the parsed data.
	 * @return the encoded JSON String of the given data.
	 */
	public abstract String encode();	
	
	/**
	 * This method is used for requesting the parser the specific file extension 
	 * it currently supports.
	 * 
	 * @return a string in the format ".extension"
	 */
	public abstract String requestFileTypeExtension();
	
	/**
	 * This method will return a string related to the file type
	 * @return a string containing the file tipe in the format "filetype"
	 */
	public abstract String requestFileType();
	
	/**
	 *  The printDataSet method prints the parsed dataset 
	 */
	public void printDataSet(){
		System.out.println("-----DataSet-----");
		for(int i=0;i<this.dataset.getDataSet().size();i++){
			System.out.println(dataset.getDataSetAsJSON().get(i).toString());
		}
		System.out.println("-----------------");
	}
	
	/**
	 *  The printMetaData method prints the parsed metadata 
	 */	
	public void printMetaData() {
		System.out.println("-----Metadata-----");
		System.out.println("Title: "+metadata.getTitle());
		System.out.println("Datalink index: "+metadata.getDataIndex());
		System.out.println("Is network directed: "+metadata.isDirectedNetwork());
		System.out.println("Is network description: "+metadata.getNetworkDescription());		
		System.out.println("-------------------");
	}

	/**
	 *  The getNetwork method
	 * @return a JSON file containing the network data
	 */
	public JSONFile getTabledata() {
		return tabledata;
	}
	
	/**
	 *  The setNetwork method.
	 *  Sets the network data on the parser
	 *  @param network data
	 */
	public void setTabledata(JSONFile tabledata) {
		this.tabledata = tabledata;
	}

	/**
	 * @return the parsed DataSet object
	 */	
	public Dataset getParsedDataSet(){
		return this.dataset;
	}
	
	/**
	 *  Replaces the current Dataset object from the DataTableParser with the given parameter
	 * @param dataset
	 */
	public void updateDataSet(Dataset dataset){
		this.dataset = dataset;
	}
	
	/**
	 * @return the parset Metadata object
	 */
	public Metadata getParsedMetadata(){
		return this.metadata;
	}
	
	/**
	 * Replaces the current Metadata object from the DataTableParser with the given parameter
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
