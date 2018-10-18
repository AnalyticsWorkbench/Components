package eu.sisob.api.parser.sisob;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.visualization.format.graph.Dataset;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.DataLinks;
import eu.sisob.api.visualization.format.metadata.fields.EdgeProperties;
import eu.sisob.api.visualization.format.metadata.fields.NodeProperties;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;

public class SGFParser extends NetworkParser {

	@Override
	@SuppressWarnings("rawtypes")
	public void parse() throws IllegalContentTypeException {
		String rawData = super.getNetwork().getStringContent();

		// extract metadata
		NodeProperties nodepropertyset = extractNodeProperties(rawData);
		EdgeProperties edgepropertyset = extractEdgeProperties(rawData);

		DataLinks datalinks = extractDataLinks(rawData);
		String description = extractNetworkDescription(rawData);
		HashMap<String, Object> extraValues = extractAdditionalInfo(rawData);

		super.directedNetwork = detectDirectedNetwork(rawData);
		super.dataIndex = detectDataIndex(rawData);
		if (super.dataIndex == -1) {
			super.dataIndex = timeIndicator;
			super.timeIndicator++;
		}

		super.metadata = new Metadata(super.getNetwork().getFileName());
		super.metadata.setDirectedNetwork(super.directedNetwork);
		super.metadata.setDataIndex(super.dataIndex);

		metadata.setNodeproperties(nodepropertyset);
		metadata.setEdgeproperties(edgepropertyset);
		metadata.setDatalinks(datalinks);
		metadata.setNetworkDescription(description);

		Iterator<String> metaiterator = extraValues.keySet().iterator();
		while (metaiterator.hasNext()) {

			String key = metaiterator.next().toString();
			Object value = extraValues.get(key);
			metadata.addNetworkInfo(key, value);
		}

		// extract nodeset
		super.nodeset = new NodeSet();
		JSONObject network = (JSONObject) JSONValue.parse(rawData);
		JSONArray jArray = (JSONArray) ((JSONObject) network.get("data")).get("nodes");
		if (jArray != null)
			for (Object abstractNode : jArray) {
				JSONObject node = (JSONObject) abstractNode;
				String label = node.get("label").toString().trim();
				String id = node.get("id").toString().trim();

				Node newNode = new Node(id, label);

				Object ta = node.get("timeappearance");
				if (ta != null) {
					if (ta instanceof JSONArray) {
						Vector<String> timeappearance = restoreTimeAppearance(ta);
						for (String time : timeappearance)
							newNode.addTimeAppearance(time);
					} else if (ta instanceof JSONObject){
						newNode.setTimeAppearance((JSONObject) ta);
					}
				}

				Iterator additionalAttributes = node.keySet().iterator();

				while (additionalAttributes.hasNext()) {
					String key = additionalAttributes.next().toString().trim();
					String values = node.get(key).toString().trim();

					if (!key.equals("label") && !key.equals("id") && !key.equals("timeappearance")) {
						if (JSONValue.parse(values) instanceof JSONArray) {
							JSONArray jsonArrayOfValues = (JSONArray) JSONValue.parse(values);
							newNode.addProperty(key, jsonArrayOfValues);
						} else {
							newNode.addProperty(key, values);
						}

					}
				}
				nodeset.addNode(newNode);
			}

		// extract edgeset
		super.edgeset = new EdgeSet();
		network = (JSONObject) JSONValue.parse(rawData);
		jArray = (JSONArray) ((JSONObject) network.get("data")).get("edges");
		if (jArray != null)
			for (Object abstractEdge : jArray) {
				JSONObject edge = (JSONObject) abstractEdge;

				String id = edge.get("id").toString().trim();
				String source = edge.get("source").toString().trim();
				String target = edge.get("target").toString().trim();

				Edge newEdge = new Edge();

				newEdge.setId(id);
				newEdge.setSource(source);
				newEdge.setTarget(target);
				
				Object ta = edge.get("timeappearance");
				if (ta != null) {
					if (ta instanceof JSONArray) {
						Vector<String> timeappearance = restoreTimeAppearance(ta);
						for (String time : timeappearance)
							newEdge.addTimeAppearance(time);
					} else if (ta instanceof JSONObject){
						newEdge.setTimeAppearance((JSONObject) ta);
					}
				}

				Iterator additionalAttributes = edge.keySet().iterator();
				while (additionalAttributes.hasNext()) {
					String key = additionalAttributes.next().toString().trim();
					String values = edge.get(key).toString().trim();

					if (!key.equals("source") && !key.equals("target") && !key.equals("id")
							&& !key.equals("timeappearance")) {
						if (JSONValue.parse(values) instanceof JSONArray) {
							JSONArray jsonArrayOfValues = (JSONArray) JSONValue.parse(values);
							newEdge.addProperty(key, jsonArrayOfValues);
						} else {
							newEdge.addProperty(key, values);
						}

					}
				}
				edgeset.addEdge(newEdge);
			}
	}

	@Override
	public String encode() {
		Metadata metadata = super.getParsedMetadata();
		Dataset dataset = new Dataset();

		dataset.setNodeSet(super.getParsedNodeSet());
		dataset.setEdgeSet(super.getParsedEdgeSet());

		StringBuilder encoder = new StringBuilder();
		char[] metaArray = new String("\"metadata\":" + metadata.toJSONString()).toCharArray();
		char[] dataArray = new String("\"data\":" + dataset.toJSONString()).toCharArray();

		encoder.append("{");
		for (int i = 0; i < metaArray.length; i++) {
			if (metaArray[i] == '{') {
				encoder.append("{");
				encoder.append(System.getProperty("line.separator"));

			} else if (metaArray[i] == '}') {
				encoder.append(System.getProperty("line.separator"));
				encoder.append("}");
				encoder.append(System.getProperty("line.separator"));
			} else {
				encoder.append(metaArray[i]);
			}

		}
		encoder.append(",");
		encoder.append(System.getProperty("line.separator"));

		for (int i = 0; i < dataArray.length; i++) {
			if (dataArray[i] == '{') {
				encoder.append("{");
				encoder.append(System.getProperty("line.separator"));

			} else if (dataArray[i] == '}') {
				encoder.append(System.getProperty("line.separator"));
				encoder.append("}");
				encoder.append(System.getProperty("line.separator"));
			} else {
				encoder.append(dataArray[i]);
			}

		}
		encoder.append("}");

		return encoder.toString();
	}

	@Override
	public String requestFileTypeExtension() {
		return ".sgf";
	}

	@Override
	public String requestFileType() {
		return "sgf";
	}

	@SuppressWarnings("rawtypes")
	private HashMap<String, Object> extractAdditionalInfo(String rawData) {
		HashMap<String, Object> values = new HashMap<String, Object>();
		JSONObject network = (JSONObject) JSONValue.parse(rawData);
		JSONObject metadataObject = ((JSONObject) network.get("metadata"));

		Iterator metaIterator = metadataObject.keySet().iterator();

		while (metaIterator.hasNext()) {
			String key = metaIterator.next().toString();
			if (!key.equals("description") && !key.equals("title") && !key.equals("datalinks")
					&& !key.equals("dimensions") && !key.equals("measures") && !key.equals("nodeproperties")
					&& !key.equals("edgeproperties")) {
				// String value = metadataObject.get(key).toString();
				Object value = metadataObject.get(key);
				values.put(key, value);
			}

		}

		return values;
	}

	private DataLinks extractDataLinks(String rawData) {
		DataLinks datalinks = new DataLinks();
		JSONObject network = (JSONObject) JSONValue.parse(rawData);
		JSONArray jdatalinks = (JSONArray) ((JSONObject) network.get("metadata")).get("datalinks");
		if (jdatalinks != null) {
			for (Object link : jdatalinks) {
				datalinks.addDataLink(link.toString().trim());
			}
		}

		return datalinks;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private EdgeProperties extractEdgeProperties(String rawData) {
		EdgeProperties edgeproperties = new EdgeProperties();
		JSONObject network = (JSONObject) JSONValue.parse(rawData);
		JSONArray eproperties = (JSONArray) ((JSONObject) network.get("metadata")).get("edgeproperties");
		if (eproperties != null) {
			for (Object o : eproperties) {
				JSONObject object = (JSONObject) o;
				Property p = new Property();

				Iterator iterator = object.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next().toString();
					p.put(key, object.get(key));
				}
				
				edgeproperties.addProperty(p);
			}

		}
		return edgeproperties;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private NodeProperties extractNodeProperties(String rawData) {
		NodeProperties nodeproperties = new NodeProperties();
		JSONObject network = (JSONObject) JSONValue.parse(rawData);
		JSONArray nproperties = (JSONArray) ((JSONObject) network.get("metadata")).get("nodeproperties");
		if (nproperties != null) {
			for (Object o : nproperties) {
				Property p = new Property();
				JSONObject object = (JSONObject) o;

				Iterator iterator = object.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next().toString();
					p.put(key, object.get(key));
				}

				nodeproperties.addProperty(p);
			}
		}
		return nodeproperties;
	}

	private Vector<String> restoreTimeAppearance(Object object) {
		Vector<String> timeappearance = new Vector<String>();
		JSONArray timeArray = (JSONArray) object;
		for (Object time : timeArray)
			timeappearance.add(time.toString().replaceAll("\"", ""));

		return timeappearance;
	}

	private int detectDataIndex(String rawData) {
		JSONObject network = (JSONObject) JSONValue.parse(rawData);
		JSONObject object = ((JSONObject) network.get("metadata"));
		String sIndex = (String) object.get("dataindex");
		if (sIndex != null) {
			try {
				return Integer.parseInt(sIndex);
			} catch (NumberFormatException nfe) {
				return -1;
			}
		}
		return -1;
	}

	private String detectDirectedNetwork(String rawData) {
		JSONObject network = (JSONObject) JSONValue.parse(rawData);
		JSONObject object = ((JSONObject) network.get("metadata"));
		Object directed = object.get("directed");
		if (directed != null)
			return directed.toString();

		return "undefined";
	}

	private String extractNetworkDescription(String rawData) {
		JSONObject network = (JSONObject) JSONValue.parse(rawData);
		JSONObject object = ((JSONObject) network.get("metadata"));
		Object description = object.get("description");
		if (description != null)
			return description.toString();

		return "undefined";
	}

}
