package eu.sisob.components.framework.graph.parser;

import java.util.Iterator;
import java.util.Vector;

import eu.sisob.api.parser.DataTableParser;
import eu.sisob.api.parser.LogParser;
import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.visualization.format.datatable.Dataset;
import eu.sisob.api.visualization.format.datatable.fields.DataField;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.DataLinks;
import eu.sisob.api.visualization.format.metadata.fields.EdgeProperties;
import eu.sisob.api.visualization.format.metadata.fields.NodeProperties;
import eu.sisob.api.visualization.format.metadata.fields.Properties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

public class FormatFactory {

	private Vector<JSONFile> dataFiles;
	public static final String NODE_TRANSFORMATION_STRATEGY = "NODES";
	public static final String EDGE_TRANSFORMATION_STRATEGY = "EDGES";

	public FormatFactory(JSONFile dataFile) {
		this.dataFiles = new Vector<JSONFile>();
		this.dataFiles.add(dataFile);
	}

	public FormatFactory(Vector<JSONFile> dataFiles) {
		this.dataFiles = dataFiles;
	}
 
	public void convertDataTableFormatToNetworkFormat(DataTableParser inputParser, NetworkParser outputParser, String strategy) throws IllegalContentTypeException {
		
		Vector<JSONFile> convertedTables = new Vector<JSONFile>(dataFiles.size());
		
		for (JSONFile table : dataFiles) {

			inputParser.setTabledata(table);
			inputParser.parse();

			Metadata metadata = inputParser.getParsedMetadata();
			metadata.setDatalinks(generateDataLinks());
			metadata.setNetworkDescription("Network:" + metadata.getTitle());

			NodeSet nodeset = null;
			EdgeSet edgeset = null;

			if (strategy.equals(FormatFactory.NODE_TRANSFORMATION_STRATEGY)) {
				nodeset = transformDataSetToNodeSet(inputParser.getParsedDataSet());
				edgeset = new EdgeSet();
			} else if (strategy.equals(FormatFactory.EDGE_TRANSFORMATION_STRATEGY)) {
				edgeset = transformDataSetToEdgeset(inputParser.getParsedDataSet());
				nodeset = generateNodeSetFromEdgeSet(edgeset);
			}

			outputParser.updateMetadata(metadata);
			outputParser.updateNodeSet(nodeset);
			outputParser.updateEdgeSet(edgeset);

			String filename = null;

			if (table.getFileName().lastIndexOf(".") != -1) {
				filename = table.getFileName().substring(0, table.getFileName().lastIndexOf("."));
			} else {
				filename = table.getFileName();
			}
			
			JSONFile newTable = new JSONFile(filename + outputParser.requestFileTypeExtension(), outputParser.requestFileType(), outputParser.encode(), JSONFile.TEXT);
			convertedTables.add(newTable);
//
//			table.setFileName(filename + outputParser.requestFileTypeExtension());
//			table.setData(outputParser.encode());
//			table.setFileType(outputParser.requestFileType());

		}
		
		dataFiles = convertedTables;
		
	}

	@SuppressWarnings("unchecked")
	private NodeSet generateNodeSetFromEdgeSet(EdgeSet edgeset) {
		// TODO Auto-generated method stub
		NodeSet nodeset = new NodeSet();
		for (Edge edge : edgeset.getValues()) {
			Node source = new Node(edge.getSource(), "Node" + edge.getSource());
			Node target = new Node(edge.getTarget(), "Node" + edge.getTarget());

			if (nodeset.indexOf(source) == -1)
				nodeset.add(source);
			if (nodeset.indexOf(target) == -1)
				nodeset.add(target);
		}
		return nodeset;
	}

	public void convertNetworkFormatToDataTableFormat(NetworkParser inputParser, DataTableParser outputParser,
			String strategy) throws IllegalContentTypeException {
		
		Vector<JSONFile> convertedTables = new Vector<JSONFile>(dataFiles.size());

		for (JSONFile network : dataFiles) {
			inputParser.setNetwork(network);
			inputParser.parse();

			Metadata metadata = inputParser.getParsedMetadata();
			metadata.setDatalinks(generateDataLinks());
			metadata.setNetworkDescription("Network:" + metadata.getTitle());

			Dataset dataset = null;

			if (strategy.equals(FormatFactory.NODE_TRANSFORMATION_STRATEGY)) {
				dataset = transformNodeSetToDataSet(inputParser.getParsedNodeSet());
				
				// add nodeproperties as properties for visualitazion 
				NodeProperties nodeproperties = metadata.getNodeproperties();

				Properties properties = metadata.getProperties();
				if(properties == null) {
					properties = new Properties();
				}

				if(!(nodeproperties == null)) {
					for (Object property : nodeproperties.getNodePropertySet()) {
						properties.add(property);
					}
				}

				metadata.setProperties(properties);
			}
			else if (strategy.equals(FormatFactory.EDGE_TRANSFORMATION_STRATEGY)) {
				dataset = transformEdgeSetToDataSet(inputParser.getParsedEdgeSet());
				
				// add edgeproperties as properties for visualization
				EdgeProperties edgeproperties = metadata.getEdgeproperties();

				Properties properties = metadata.getProperties();
				if(properties == null) {
					properties = new Properties();
				}

				if(!(edgeproperties == null)) {
					for (Object property : edgeproperties.getEdgePropertySet()) {
						properties.add(property);
					}
				}

				metadata.setProperties(properties);
			}
			
			outputParser.updateMetadata(metadata);
			outputParser.updateDataSet(dataset);

			String filename = null;

			if (network.getFileName().lastIndexOf(".") != -1) {
				filename = network.getFileName().substring(0, network.getFileName().lastIndexOf("."));
			} else {
				filename = network.getFileName();
			}
			
			JSONFile convertedTable = new JSONFile(filename + outputParser.requestFileTypeExtension(), outputParser.requestFileType(), outputParser.encode(), JSONFile.TEXT);
			convertedTables.add(convertedTable);

//			network.setFileName(filename + outputParser.requestFileTypeExtension());
//			network.setData(outputParser.encode());
//			network.setFileType(outputParser.requestFileType());

		}
		
		dataFiles = convertedTables;
	}

	public void convertNetworkFormats(NetworkParser inputParser, NetworkParser outputParser)
			throws IllegalContentTypeException {
		
		Vector<JSONFile> convertedFiles = new Vector<JSONFile>(dataFiles.size());

		for (JSONFile network : dataFiles) {
			inputParser.setNetwork(network);
			inputParser.parse();

			NodeSet nodeset = inputParser.getParsedNodeSet();
			EdgeSet edgeset = inputParser.getParsedEdgeSet();
			Metadata metadata = inputParser.getParsedMetadata();

			metadata.setDatalinks(generateDataLinks());
			metadata.setNetworkDescription("Network:" + metadata.getTitle());

			outputParser.updateMetadata(metadata);
			outputParser.updateNodeSet(nodeset);
			outputParser.updateEdgeSet(edgeset);
			
			String filename = null;

			if (network.getFileName().lastIndexOf(".") != -1) {
				filename = network.getFileName().substring(0, network.getFileName().lastIndexOf("."));
			} else {
				filename = network.getFileName();
			}
			
			JSONFile convertedFile = new JSONFile(filename + outputParser.requestFileTypeExtension(), outputParser.requestFileType(), outputParser.encode(), JSONFile.TEXT);
			convertedFiles.add(convertedFile);

//			network.setFileName(filename + outputParser.requestFileTypeExtension());
//			network.setData(outputParser.encode());
//			network.setFileType(outputParser.requestFileType());

		}
		
		dataFiles = convertedFiles;

	}

	public void convertDataTableFormats(DataTableParser inputParser, DataTableParser outputParser)
			throws IllegalContentTypeException {
		
		Vector<JSONFile> convertedFiles = new Vector<JSONFile>(dataFiles.size());

		for (JSONFile table : dataFiles) {
			inputParser.setTabledata(table);
			inputParser.parse();

			Dataset dataset = inputParser.getParsedDataSet();
			Metadata metadata = inputParser.getParsedMetadata();

			metadata.setDatalinks(generateDataLinks());
			metadata.setNetworkDescription("Network:" + metadata.getTitle());

			outputParser.updateMetadata(metadata);
			outputParser.updateDataSet(dataset);;

			String filename = null;

			if (table.getFileName().lastIndexOf(".") != -1) {
				filename = table.getFileName().substring(0, table.getFileName().lastIndexOf("."));
			} else {
				filename = table.getFileName();
			}
			
			JSONFile convertedData = new JSONFile(filename + outputParser.requestFileTypeExtension(), outputParser.requestFileType(), outputParser.encode(), JSONFile.TEXT);
			convertedFiles.add(convertedData);

//			table.setFileName(filename + outputParser.requestFileTypeExtension());
//			table.setData(outputParser.encode());
//			table.setFileType(outputParser.requestFileType());

		}
		
		dataFiles = convertedFiles;

	}

	public void convertEventLogsToBipartiteNetwork(LogParser inputParser, NetworkParser outputParser,
			String firstModeKey, String secondModeKey) {
		throw new UnsupportedOperationException("not supported yet");
	}

	public void convertEventLogFormats(LogParser inputParser, LogParser outputParser)
			throws IllegalContentTypeException {

		String filename;
		
		Vector<JSONFile> convertedFiles = new Vector<JSONFile>(dataFiles.size());

		for (JSONFile file : this.dataFiles) {

			inputParser.setLogfile(file);
			inputParser.parse();

			outputParser.setMetadata(inputParser.getMetadata());
			outputParser.setEventLogs(inputParser.getEventLogs());

			if (file.getFileName().lastIndexOf(".") != -1) {
				filename = file.getFileName().substring(0, file.getFileName().lastIndexOf("."));
			} else {
				filename = file.getFileName();
			}
			
			JSONFile convertedData = new JSONFile(filename + outputParser.requestFileTypeExtension(), outputParser.requestFileType(), outputParser.encode(), JSONFile.TEXT);
			convertedFiles.add(convertedData);

//			file.setFileName(filename + outputParser.requestFileTypeExtension());
//			file.setData(outputParser.encode());
//			file.setFileType(outputParser.requestFileType());
		}
		
		dataFiles = convertedFiles;

	}

	@SuppressWarnings("rawtypes")
	private NodeSet transformDataSetToNodeSet(Dataset dataset) {
		NodeSet nodeset = new NodeSet();

		for (DataField datafield : dataset.getDataSet()) {
			Node node = new Node();
			node.setId(datafield.getId());
			node.setLabel(datafield.getLabel());
			node.setTimeAppearance(datafield.getTimeAppearance());

			Iterator additionalAttributes = datafield.keySet().iterator();
			while (additionalAttributes.hasNext()) {
				String key = additionalAttributes.next().toString().trim();
				if (key.equals("label") != true && key.equals("id") != true) {
					node.addProperty(key, datafield.get(key).toString().trim());
				}
			}
			nodeset.addNode(node);
		}
		return nodeset;
	}

	@SuppressWarnings("rawtypes")
	private EdgeSet transformDataSetToEdgeset(Dataset dataset) {
		EdgeSet edgeset = new EdgeSet();
		for (DataField datafield : dataset.getDataSet()) {
			Edge edge = new Edge();
			edge.setId(datafield.getId());
			edge.setTimeAppearance(datafield.getTimeAppearance());

			Iterator additionalAttributes = datafield.keySet().iterator();
			while (additionalAttributes.hasNext()) {
				String key = additionalAttributes.next().toString().trim();
				if (key.equals("label") != true && key.equals("id") != true) {
					edge.addProperty(key, datafield.get(key).toString().trim());
				}
			}
			edgeset.addEdge(edge);
		}

		return edgeset;
	}

	@SuppressWarnings("rawtypes")
	private Dataset transformEdgeSetToDataSet(EdgeSet edgeset) {
		Dataset dataset = new Dataset();

		for (Edge edge : edgeset.getValues()) {
			DataField datafield = new DataField(edge.getId(), edge.getSource() + " - " + edge.getTarget());

			if (edge.getTimeAppearanceVector() != null) {
				datafield.setTimeAppearance(edge.getTimeAppearance());
			}

			Iterator additionalAttributes = edge.getEdgeAsJSON().keySet().iterator();
			while (additionalAttributes.hasNext()) {
				String key = additionalAttributes.next().toString().trim();
				if (key.equals("label") != true && key.equals("id") != true && key.equals("timeappearance") != true) {
					datafield.addProperty(key, edge.getEdgeAsJSON().get(key).toString());
				}
			}
			dataset.addDataField(datafield);
		}

		return dataset;
	}

	@SuppressWarnings("rawtypes")
	private Dataset transformNodeSetToDataSet(NodeSet nodeset) {
		Dataset dataset = new Dataset();

		for (Node node : nodeset.getValues()) {
			DataField datafield = new DataField(node.getId(), node.getLabel());

			if (node.getTimeAppearanceVector() != null) {
				datafield.setTimeAppearance(node.getTimeAppearance());
			}

			Iterator additionalAttributes = node.getNodeAsJSON().keySet().iterator();
			while (additionalAttributes.hasNext()) {
				String key = additionalAttributes.next().toString().trim();
				if (key.equals("label") != true && key.equals("id") != true && key.equals("timeappearance") != true) {
					datafield.addProperty(key, node.getNodeAsJSON().get(key).toString());
				}
			}
			dataset.addDataField(datafield);
		}

		return dataset;
	}

	public DataLinks generateDataLinks() {
		DataLinks datalinks = new DataLinks();
		for (int i = 0; i < dataFiles.size(); i++) {
			datalinks.addDataLink(i + ".json");
		}

		return datalinks;
	}

	public Vector<JSONFile> getNetworks() {
		return dataFiles;
	}

	public void setNetworks(Vector<JSONFile> networks) {
		this.dataFiles = networks;
	}

}
