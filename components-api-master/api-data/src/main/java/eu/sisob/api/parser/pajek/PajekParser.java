package eu.sisob.api.parser.pajek;

import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;

public class PajekParser extends NetworkParser {

	public void parse() throws IllegalContentTypeException {

		String splitRule = super.splitBy(super.getNetwork().getStringContent());
		String rawData[] = super.getNetwork().getStringContent().split(splitRule);

		int entityLimit = calculateEntityLimit(rawData);

		super.directedNetwork = detectDirectedNetwork(rawData);

		// extract the metadata
		super.metadata = new Metadata(super.getNetwork().getFileName());
		super.metadata.setDirectedNetwork(super.directedNetwork);
		super.metadata.setDataIndex(super.dataIndex);

		// if(isPajekBipartite(rawData)) {
		// NodeProperties properties = new NodeProperties();
		// Property property = new Property();
		// Vector<String> entities = new Vector <String>();
		// property.changePropertyName("entities");
		// entities.add("entity");
		// entities.add("artifact");
		// property.setPropertyValues(entities);
		// properties.addProperty(property);
		// metadata.setNodeproperties(properties);
		// }

		// extract the nodeset
		super.nodeset = new NodeSet();

		int edgeIndex = -1;
		for (int i = 1; i < rawData.length; i++) {
			String line = rawData[i];
			if (isDataLine(line)) {
				if (!isEdgeSetStarting(line)) {
					String nodeID = extractNodeID(line);
					String nodelabel = extractNodeLabel(line);
					if (nodeID != null && nodelabel != null) {
						Vector<String> timeAppearance = extractEntityTimeAppearance(line.trim());
						Node node = new Node(nodeID, nodelabel);
						for (String t : timeAppearance) {
							node.addTimeAppearance(t);
						}
						if (entityLimit > -1 && i <= entityLimit)
							node.addProperty("type", "0");
						else if (entityLimit > -1 && i > entityLimit)
							node.addProperty("type", "1");

						nodeset.addNode(node);

					} else {
						throw new IllegalContentTypeException(
								"unable to extract node information from source file in line " + i);
					}
				} else {
					edgeIndex = i + 1;
					break;
				}
			}
		}

		// extract the edgeset
		super.edgeset = new EdgeSet();
		if (edgeIndex != -1) {
			int edgeID = 0;
			for (int i = edgeIndex; i < rawData.length; i++) {
				String line = rawData[i];
				if (isDataLine(line)) {
					Scanner edgeScanner = new Scanner(line);
					int source = -1;
					int target = -1;
					if (edgeScanner.hasNextInt())
						source = edgeScanner.nextInt();
					if (edgeScanner.hasNextInt())
						target = edgeScanner.nextInt();

					if (source != -1 && target != -1) {
						Edge edge = new Edge("" + edgeID, "" + source, "" + target);
						Vector<String> timeAppearance = extractEntityTimeAppearance(line.trim());
						for (String t : timeAppearance) {
							edge.addTimeAppearance(t);
						}
						edgeset.addEdge(edge);
						edgeID++;
					} else {
						edgeScanner.close();
						throw new IllegalContentTypeException(
								"unable to extract node information from source file in line " + i);
					}
					edgeScanner.close();
				}
			}
		}

	}

	@Override
	public String encode() {
		StringBuilder encoder = new StringBuilder();
		// FIXME reintroduce two mode case
		// if(super.getParsedMetadata().getNetworkType().equals(NetworkParser.TWO_MODE_NETWORK))
		// encoder.append("*Vertices "+super.getParsingNodeSet().size()+"
		// "+extractJSONEntityLimit());
		// else
		encoder.append("*Vertices " + super.getParsedNodeSet().size());

		encoder.append("\n");

		normalizeNetwork(super.getParsedNodeSet(), super.getParsedEdgeSet());

		for (Object object : super.getParsedNodeSet()) {
			Node node = (Node) object;
			encoder.append("" + node.getId() + " " + "\"" + node.getLabel() + "\" ");

			if (node.getTimeAppearance() != null && node.getTimeAppearance().size() > 0) {
				encoder.append("[");

				for (int i = 0; i < node.getTimeAppearance().size(); i++) {
					encoder.append(node.getTimeAppearance().get(i));

					if (i + 1 < node.getTimeAppearance().size())
						encoder.append(",");
				}
				encoder.append("]");
			}
			encoder.append("\n");
		}

		if (super.getParsedMetadata().isDirectedNetwork().equals("true"))
			encoder.append("*Arcs");
		else
			encoder.append("*Edges");

		encoder.append("\n");

		for (Object object : super.getParsedEdgeSet()) {
			Edge edge = (Edge) object;
			encoder.append("" + edge.getSource() + " " + edge.getTarget() + " ");

			// if there is any time information related to a edge
			if (edge.getTimeAppearance() != null && edge.getTimeAppearance().size() > 0) {
				encoder.append("[");

				for (int i = 0; i < edge.getTimeAppearance().size(); i++) {
					encoder.append(edge.getTimeAppearance().get(i));

					if (i + 1 < edge.getTimeAppearance().size())
						encoder.append(",");
				}
				encoder.append("]");
			}

			encoder.append("\n");
		}

		return encoder.toString();
	}

	private void normalizeNetwork(NodeSet parsingNodeSet, EdgeSet parsingEdgeSet) {
		HashMap<String, String> changeTable = new HashMap<String, String>();
		int normalizedID = 1;

		for (int i = 0; i < parsingNodeSet.size(); i++) {
			Node node = (Node) parsingNodeSet.get(i);
			changeTable.put(node.getId(), "" + normalizedID);
			node.setId("" + normalizedID);
			normalizedID++;
		}

		for (Object obj : parsingEdgeSet) {
			Edge edge = (Edge) obj;
			String source = edge.getSource();
			String target = edge.getTarget();
			String normalizedSource = changeTable.get(source);
			String normalizedTarget = changeTable.get(target);
			edge.setSource(normalizedSource);
			edge.setTarget(normalizedTarget);

		}

	}

	@Override
	public String requestFileTypeExtension() {
		return ".net";
	}

	@Override
	public String requestFileType() {
		return "net";
	}

	private int calculateEntityLimit(String[] rawData) throws IllegalContentTypeException {
		String netHeader = rawData[0];

		String headerElements[] = netHeader.split(" ");
		if (headerElements.length == 1)
			throw new IllegalContentTypeException(
					"Unable to calculate the number of entities, error in pajek file header");
		else if (headerElements.length == 2) {
			return Integer.parseInt(netHeader.split(" ")[1].trim());

		} else if (headerElements.length > 2) {
			int a = Integer.parseInt(netHeader.split(" ")[1].trim());
			int b = Integer.parseInt(netHeader.split(" ")[2].trim());
			int limit = (a < b) ? a : b;
			return limit;
		}

		return -1;
	}

	private String detectDirectedNetwork(String[] rawData) {
		for (String line : rawData) {
			if (line.trim().toLowerCase().contains("*arcs"))
				return NetworkParser.DIRECTED_NETWORK;
		}
		return NetworkParser.UNDIRECTED_NETWORK;
	}

//	private boolean isPajekBipartite(String[] rawData) {
//		String netHeader = rawData[0];
//		if (netHeader.split(" ").length == 3)
//			return true;
//		else
//			return false;
//	}

	private boolean isDataLine(String line) {
		if (!line.trim().isEmpty() && !line.trim().toLowerCase().contains("*vertices") && !line.equals("\n")
				&& line.toCharArray()[0] != '%' && line.lastIndexOf("/*") == -1)
			return true;

		return false;
	}

	private boolean isEdgeSetStarting(String line) {
		if (line.trim().toLowerCase().contains("*arcs") || line.trim().toLowerCase().contains("*edges"))
			return true;

		return false;
	}

	private String extractNodeID(String dataLine) {
		Scanner scanner = new Scanner(dataLine);
		StringBuilder builder = new StringBuilder();
		if (scanner.hasNextInt()) {
			int id = scanner.nextInt();
			builder.append("" + id);
			scanner.close();
			return builder.toString().trim();
		} else {
			scanner.close();
			return null;
		}
	}

	private String extractNodeLabel(String line) {
		Scanner scanner = new Scanner(line);
		if (scanner.hasNextInt()) {
			String id = "" + scanner.nextInt();
			String nodeLabel = "";

			int indexEndOfID = (line.indexOf(id) + id.length());
			int indexOfTimeTag = line.indexOf("[");

			if (indexOfTimeTag == -1) {

				int indexEndOfLabel = line.lastIndexOf("\"");
				if (indexEndOfLabel != -1) {
					nodeLabel = line.substring(indexEndOfID, (indexEndOfLabel));
				} else {
					nodeLabel = line.substring(indexEndOfID, line.length());
				}

			} else {
				nodeLabel = line.substring(indexEndOfID, indexOfTimeTag);
			}
			scanner.close();
			return nodeLabel.trim().replaceAll("\"", "");

		} else
			scanner.close();
			return null;

	}

	private Vector<String> extractEntityTimeAppearance(String dataLine) {

		Vector<String> time = new Vector<String>();
		// detect the beggining of the time array
		int beginOfTimeTag = (dataLine.indexOf("[") + 1);
		int endOfTimeTag = dataLine.lastIndexOf("]");

		// check if the time tag index detected some time information
		if (beginOfTimeTag != -1 && endOfTimeTag != -1) {
			// extract the substring containig the time information
			String timeData = dataLine.substring(beginOfTimeTag, endOfTimeTag).trim();
			// using StringTokenizer for recovering the "," separated values
			StringTokenizer tokenizer = new StringTokenizer(timeData, ",");
			// add the required time information to the time vector
			while (tokenizer.hasMoreTokens())
				time.add(tokenizer.nextToken());
		}
		return time;
	}

}
