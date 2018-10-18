package eu.sisob.api.parser.ucinetdl;

import java.util.LinkedList;
import java.util.List;

import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.EdgeProperties;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;

public class UCINETParser extends NetworkParser {
	
	@SuppressWarnings("unchecked")
	public void parse() throws IllegalContentTypeException {
		
		LinkedList<double[]> dataRows = new LinkedList<double[]>();
		LinkedList<String> rowLabels = new LinkedList<String>();
		LinkedList<String> columnLabels = new LinkedList<String>();
		LinkedList<String> labels = new LinkedList<String>();
		int columns = -1;
		String mode = "none";
		
		// split the text no matter what line break is used, empty may will also be "removed"
		String[] networkData = getNetwork().getStringContent().split("[\\r\\n]+");
		for (String line : networkData) {
			if (!line.trim().isEmpty()) {
				// we only treat lines as long as we are sure there is content in it!
				if (line.trim().toLowerCase().startsWith("dl")) {
					// we are at the beginning of the file
					mode = "dl";
					// FIXME number information is ignored currently
				} else if (line.trim().toLowerCase().startsWith("labels")) {
					mode = "labels";
				} else if (line.trim().toLowerCase().startsWith("row labels")) {
					mode = "rlabels";
				} else if (line.trim().toLowerCase().startsWith("column labels")) {
					mode = "clabels";
				} else if (line.trim().toLowerCase().startsWith("data")) {
					mode = "data";
				} else if (mode == "labels") {
					labels.addAll(parseLabels(line));
				} else if (mode == "rlabels") {
					rowLabels.addAll(parseLabels(line));
				} else if (mode == "clabels") {
					columnLabels.addAll(parseLabels(line));
				} else if (mode == "data") {
					String[] lineElements = line.trim().split("\\s+");
					if (lineElements.length > 0) {
						if (columns == -1) {
							columns = lineElements.length;
						} else {
							if (columns != lineElements.length) {
								// FIXME proper error handling
								throw new IllegalContentTypeException(columns + " columns", lineElements.length + " columns available");
							}
						}
						double[] lineEntries = new double[lineElements.length];
						for (int i = 0; i < lineElements.length; i++) {
							try {
								lineEntries[i] = Double.parseDouble(lineElements[i]);
							} catch (NumberFormatException ex) {
								// FIXME proper error handling
								throw new IllegalContentTypeException("double", lineElements[i]);
							}
						}
						dataRows.add(lineEntries);
					}
				}
			}
		}
		int rows = dataRows.size();

		// handle node set
		NodeSet nodeSet = new NodeSet();
		boolean bipartite;
		if (labels.size() > 0) {
			bipartite = false;
			for (int i = 0; i < labels.size(); i++) {
				nodeSet.addNode(new Node(Integer.toString(i+1), labels.get(i)));
			}
		} else if (rowLabels.size() > 0 && columnLabels.size() > 0){
			bipartite = true;
			for (int i = 0; i < rowLabels.size(); i++) {
				Node node = new Node(Integer.toString(i+1), rowLabels.get(i));
				node.addProperty("type", "0");
				nodeSet.addNode(node);
			}
			for (int i = 0; i < columnLabels.size(); i++) {
				Node node = new Node(Integer.toString(rows+i+1), columnLabels.get(i));
				node.addProperty("type", "1");
				nodeSet.addNode(node);
			}
		} else if (rowLabels.size() > 0) {
			bipartite = false;
			for (int i = 0; i < rowLabels.size(); i++) {
				nodeSet.addNode(new Node(Integer.toString(i+1), rowLabels.get(i)));
			}
		} else if (columnLabels.size() > 0) {
			bipartite = false;
			for (int i = 0; i < columnLabels.size(); i++) {
				nodeSet.addNode(new Node(Integer.toString(i+1), columnLabels.get(i)));
			}
		} else if (columns == rows) {
			// we do not have labels, but a symmetric matrix
			// FIXME this could still be bipartite!
			bipartite = false;
			for (int i = 0; i < columns; i++) {
				nodeSet.addNode(new Node(Integer.toString(i+1), Integer.toString(i+1)));
			}
		} else {
			// we do have an asymmetric matrix
			bipartite = true;
			for (int i = 0; i < columns+rows; i++) {
				Node node = new Node(Integer.toString(i+1), Integer.toString(i+1));
				if (i < columns) {
					node.addProperty("type", "0");
				} else {
					node.addProperty("type", "1");
				}
				nodeSet.addNode(node);
			}	
		}
		// FIXME check if number of labels and nodes do match
		this.nodeset = nodeSet;
		
		// FIXME this is up to now a quite crude approximation
		if (bipartite) {
			// FIXME why is this necessary?!
			this.directedNetwork = "false";
		} else {
			this.directedNetwork = "true";
		}
		
		// created metadata info with some basics
		Metadata metaData = new Metadata(getNetwork().getFileName());
		metaData.setDirectedNetwork(getDirectedNetwork());
		EdgeProperties properties = new EdgeProperties();
		properties.add(new Property("weight", "double", "Edge Weight", null, null));
		metaData.setEdgeproperties(properties);
//		if (bipartite) {
//			// TODO this is already not a good idea in the Pajek parser, from where it is... 
//			NodeProperties properties = new NodeProperties();
//			Property property = new Property();
//			Vector<String> entities = new Vector<String>();
//			property.setPropertyName("entities");
//			entities.add("entity");
//			entities.add("artifact");		
//			property.setValues(entities);
//			properties.addProperty(property);
//			metadata.setNodeproperties(properties);
//		}
		this.metadata = metaData;
		
		// handle edge set
		EdgeSet edgeSet = new EdgeSet();
		int edgeID = 0;
		for (int row = 0; row < dataRows.size(); row++) {
			double[] rowArray = dataRows.get(row);
			for (int col = 0; col < columns; col++) {
				double edgeWeight = rowArray[col];
				if (edgeWeight != 0.0) {
					Edge e = null;
					if (bipartite) {
						e = new Edge(Integer.toString(edgeID),Integer.toString(row+1),Integer.toString(rows+col+1));
					} else {
						e = new Edge(Integer.toString(edgeID),Integer.toString(row+1),Integer.toString(col+1));
					}
					e.addMeasure("weight", Double.toString(edgeWeight));
					edgeSet.addEdge(e);
					edgeID++;					
				}
			}
		}
		this.edgeset = edgeSet;
		
		System.out.println("network " + getNetwork().getFileName() + " parsed");
	}
		
	@Override
	public String encode() {
		// FIXME is not as "mighty" as the parser!
		// number of nodes
		int nodes = super.getParsedNodeSet().getValues().size();
		
		StringBuilder ucinetBuilder = new StringBuilder(); 
		ucinetBuilder.append("DL N="+super.getParsedNodeSet().getValues().size());
		ucinetBuilder.append("\n");
		ucinetBuilder.append("FORMAT = FULLMATRIX");
		ucinetBuilder.append("\n");
		ucinetBuilder.append("LABELS:");
		ucinetBuilder.append("\n");
		NodeSet nodeset = super.getParsedNodeSet();
		for(int i=0; i < nodes; i++){
			
			Node node = nodeset.getValues().get(i);			
			if(i != 0){
				ucinetBuilder.append(",");
				ucinetBuilder.append(node.getLabel());
			}else {
				ucinetBuilder.append(node.getLabel());
			}
		}
		
		ucinetBuilder.append("\n");
		ucinetBuilder.append("DATA:");
		ucinetBuilder.append("\n");
		double[][] weightMatrix = new double[nodes][nodes];
		
		EdgeSet edgeset = super.getParsedEdgeSet();
		
		for(Edge edge:edgeset.getValues()){
			int row = Integer.parseInt(edge.getSource().toString());
			int column = Integer.parseInt(edge.getTarget().toString());
			String edgeWeight = edge.getProperty("weight");
			if(edgeWeight!=null){
				double measure = Double.parseDouble(edgeWeight);
				weightMatrix[(row-1)][(column-1)] = measure;	
			}else weightMatrix[(row-1)][(column-1)] = 0; 
			
		}
		
		for(int row = 0; row < nodes; row++){
			for(int column = 0; column < nodes; column++){
				double value =  weightMatrix[row][column];
				ucinetBuilder.append(""+value);
				ucinetBuilder.append(" ");
			}
			ucinetBuilder.append("\n");
		}
		
		return ucinetBuilder.toString();
	}

	@Override
	public String requestFileTypeExtension() {
		// TODO Auto-generated method stub
		return ".txt";
	}

	@Override
	public String requestFileType() {
		// TODO Auto-generated method stub
		return "txt";
	}

	private List<String> parseLabels(String line) {
		LinkedList<String> labels = new LinkedList<String>();
		String[] rawLabels = line.trim().split("\\s*,\\s*");
		for (String label : rawLabels) {
			labels.add(label);
		}
		return labels;
	}
	
//	private String detectNetworkTime() {
//		return null;
//	}
	
//	private String detectDirectedNetwork() {
//		return directedNetwork;
//	}

//	private String detectNetworkType() {
//	return networkType;
//}

}
