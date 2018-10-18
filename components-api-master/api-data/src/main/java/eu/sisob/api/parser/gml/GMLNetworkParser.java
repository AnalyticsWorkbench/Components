/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.api.parser.gml;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.json.simple.JSONArray;

import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.parser.gml.antlrfiles.GMLLexer;
import eu.sisob.api.parser.gml.antlrfiles.GMLParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.EdgeProperties;
import eu.sisob.api.visualization.format.metadata.fields.NodeProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;

/**
 *
 * @author hecking
 */
public class GMLNetworkParser extends NetworkParser {

	private final static String OLD_ID = "cc769f8e984111e58994feff819cdc9f";

	private GMLParseTreeListener parseListener;

	private int idCounter = 1;
	private Map<String, Integer> nodeIDs;

	private boolean keepIds = false;
	private boolean extendedMetadata = false;

	public GMLNetworkParser() {
		super();
	}

	public GMLNetworkParser(boolean keepIds) {
		this();
		this.keepIds = keepIds;
	}

	public GMLNetworkParser(boolean keepIds, boolean extendedMetadata) {
		this(keepIds);
		this.extendedMetadata = extendedMetadata;
	}

	@Override
	public void parse() throws IllegalContentTypeException {

		GMLLexer lexer = new GMLLexer(new ANTLRInputStream(this.network.getStringContent()));
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		GMLParser parser = new GMLParser(tokenStream);

		ParseTree tree = parser.gml();
		ParseTreeWalker walker = new ParseTreeWalker();

		this.parseListener = new GMLParseTreeListener();
		walker.walk(this.parseListener, tree);

		// extract metadata
                this.metadata = this.parseListener.getMetadata();
		this.directedNetwork = this.metadata.isDirectedNetwork();
		this.dataIndex = this.metadata.getDataIndex();

		if (this.dataIndex == -1) {
			this.dataIndex = timeIndicator;
			this.timeIndicator++;
		}

		if (this.metadata.getTitle() == null) {
			this.metadata.setTitle(this.network.getFileName());
		}

		// extract node set
		this.nodeset = this.parseListener.getNodeSet();
		// extract edgeset
		this.edgeset = this.parseListener.getEdgeSet();

		// extract old ids, if wanted
		if (keepIds) {
			extractOldIds();
			EdgeProperties edgeproperties = metadata.getEdgeproperties();
			if (edgeproperties != null) {
				edgeproperties.removePropertyByKey(OLD_ID);
			}
			NodeProperties nodeproperties = metadata.getNodeproperties();
			if (nodeproperties != null) {
				nodeproperties.removePropertyByKey(OLD_ID);
			}
		}
	}

	private void extractOldIds() {
		// order matters!
		extractOldEdgeIds();
		extractOldNodeIds();
	}

	private void extractOldEdgeIds() {
		for (Object object : super.edgeset) {
			Edge edge = (Edge) object;
			String oldId = (String) edge.get(OLD_ID);
			if (oldId != null && !oldId.isEmpty()) {
				edge.setId(oldId);
				edge.remove(OLD_ID);
				Node sourceNode = getNodeById(edge.getSource());
				Node targetNode = getNodeById(edge.getTarget());
				if (sourceNode != null && targetNode != null) {
					edge.setSource((String) sourceNode.get(OLD_ID));
					edge.setTarget((String) targetNode.get(OLD_ID));
				}
			}
		}
	}

	private Node getNodeById(String id) {
		if (id != null && !id.isEmpty())
			for (Object object : super.nodeset) {
				Node node = (Node) object;
				if (id.equals(node.getId())) {
					return node;
				}
			}
		return null;
	}

	private void extractOldNodeIds() {
		for (Object object : super.nodeset) {
			Node node = (Node) object;
			String oldId = (String) node.get(OLD_ID);
			if (oldId != null && !oldId.isEmpty()) {
				node.setId(oldId);
				node.remove(OLD_ID);
			}
		}
	}

	@Override
	public String requestFileTypeExtension() {
		return ".gml";
	}

	@Override
	public String requestFileType() {
		return "gml";
	}

	@Override
	public String encode() {

		String graph = "Creator \"SISOB Workbench\" \n";
		graph = graph + "graph [ \n";

		graph = graph + this.encodeMetadata(extendedMetadata) + "\n";

		boolean numericalIDs = true;
		for (Node node : this.nodeset.getValues()) {
			try {
				Integer.parseInt(node.getId());
			} catch (NumberFormatException ex) {
				numericalIDs = false;
				break;
			}
		}
		for (Edge edge : this.edgeset.getValues()) {
			try {
				String edgeId = edge.getId();
				if (edgeId != null) {
					Integer.parseInt(edgeId);
				}
			} catch (NumberFormatException ex) {
				numericalIDs = false;
				break;
			}
		}
		if (!numericalIDs) {
			nodeIDs = new Hashtable<String, Integer>();
		}

		for (Node node : this.nodeset.getValues()) {

			graph = graph + this.encodeNode(node, numericalIDs) + "\n";
		}
		for (Edge edge : this.edgeset.getValues()) {

			graph = graph + this.encodeEdge(edge, numericalIDs) + "\n";
		}

		graph = graph + "]";

		return graph;
	}

	private String encodeNode(Node node, boolean numericalIDs) {

		String gmlNode = "\tnode [\n";

		String idString = node.getId();

		// save old ids, if wanted
		if (keepIds) {
			gmlNode = gmlNode + "\t\t" + OLD_ID + " \"" + idString + "\"\n";
		}

		if (!numericalIDs) {
			nodeIDs.put(idString, new Integer(idCounter));
			idString = String.valueOf(idCounter);
			idCounter++;
		}
		gmlNode = gmlNode + "\t\tid " + idString + "\n";

		if (node.getLabel() != null) {

			gmlNode = gmlNode + "\t\tlabel \"" + node.getLabel() + "\"\n";
		} else {

			gmlNode = gmlNode + "\t\tlabel \"" + node.getId() + "\"\n";
		}
		JSONArray times = node.getTimeAppearance();

		if (times != null && times.size() > 0) {

			String timeString = (String) times.get(0);

			for (int i = 1; i < times.size(); i = i + 1) {

				timeString = timeString + "," + times.get(i);
			}

			gmlNode = gmlNode + "\t\ttime \"" + timeString + "\"\n";
		}

		for (String key : node.getPropertyKeys()) {
			if (!key.equals("label") && !key.equals("id") && !key.equals("timeappearance")) {

				try {

					Integer.parseInt(node.getProperty(key));
					gmlNode = gmlNode + "\t\t" + key + " " + node.getProperty(key) + "\n";
				} catch (NumberFormatException e1) {

					try {
						Double.parseDouble(node.getProperty(key));
						gmlNode = gmlNode + "\t\t" + key + " " + node.getProperty(key) + "\n";

					} catch (NumberFormatException e2) {

						gmlNode = gmlNode + "\t\t" + key + " \"" + node.getProperty(key) + "\"\n";
					}
				}
			}
		}
		gmlNode = gmlNode + "\t]";

		return gmlNode;
	}

	private String encodeEdge(Edge edge, boolean numericalIDs) {

		String gmlEdge = "\tedge [\n";

		String sourceString = edge.getSource();
		String targetString = edge.getTarget();
		if (!numericalIDs) {
			sourceString = nodeIDs.get(sourceString).toString();
			targetString = nodeIDs.get(targetString).toString();
		}

		gmlEdge = gmlEdge + "\t\tsource " + sourceString + "\n";
		gmlEdge = gmlEdge + "\t\ttarget " + targetString + "\n";

		String idString = edge.getId();
		if (idString != null) {

			if (keepIds) {
				gmlEdge = gmlEdge + "\t\t" + OLD_ID + " \"" + idString + "\"\n";
			}

			if (!numericalIDs) {
				idString = String.valueOf(idCounter);
				idCounter++;
			}
			gmlEdge = gmlEdge + "\t\tid " + idString + "\n";
		}
		if (edge.getLabel() != null) {

			gmlEdge = gmlEdge + "\t\tlabel \"" + edge.getLabel() + "\"\n";
		}

		JSONArray times = edge.getTimeAppearance();

		if (times != null && times.size() > 0) {

			String timeString = (String) times.get(0);

			for (int i = 1; i < times.size(); i = i + 1) {

				timeString = timeString + "," + times.get(i);
			}

			gmlEdge = gmlEdge + "\t\ttime \"" + timeString + "\"\n";
		}

		for (String key : edge.getPropertyKeys()) {
			if (!key.equals("label") && !key.equals("id") && !key.equals("timeappearance") && !key.equals("source")
					&& !key.equals("target")) {
				String propertyValue = checkAndQuoteIntDouble(edge.getProperty(key));
				gmlEdge = gmlEdge + "\t\t" + key + " " + propertyValue + "\n";
			}
		}
		gmlEdge = gmlEdge + "\t]";

		return gmlEdge;
	}

	private String encodeMetadata() {

		return ((this.metadata.isDirectedNetwork() != null) && this.metadata.isDirectedNetwork().equals("true")) ? "\t directed 1"
				: "\t directed 0";
	}

	@SuppressWarnings("rawtypes")
	private String encodeMetadata(boolean extendedMetadata) {
		StringBuffer sb = new StringBuffer();
		sb.append(encodeMetadata());
		if (!extendedMetadata) {
			return sb.toString();
		}
                
		Iterator iter = this.metadata.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			if (!"directed".equals(key) && !"nodeproperties".equals(key) && !"edgeproperties".equals(key)
					&& entry.getValue() instanceof String) {
				sb.append("\n");
				sb.append("\t " + key + " \"" + entry.getValue() + "\"");
			}
		}
		return sb.toString();
	}

	private String checkAndQuoteIntDouble(String input) {
		String result = input;
		try {
			Long.parseLong(input);
		} catch (NumberFormatException e1) {
			try {
				Double.parseDouble(input);
			} catch (NumberFormatException e2) {
				result = quote(input);
			}
		}
		return result;
	}

	private String quote(String input) {
		return "\"" + input + "\"";
	}

}
