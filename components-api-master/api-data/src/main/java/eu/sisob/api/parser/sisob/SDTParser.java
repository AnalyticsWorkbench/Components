package eu.sisob.api.parser.sisob;

import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import eu.sisob.api.parser.DataTableParser;
import eu.sisob.api.visualization.format.datatable.Dataset;
import eu.sisob.api.visualization.format.datatable.fields.DataField;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.DataLinks;
import eu.sisob.api.visualization.format.metadata.fields.Properties;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;

public class SDTParser extends DataTableParser {

	@Override
	@SuppressWarnings("rawtypes")
	public void parse() throws IllegalContentTypeException {
		String rawData = super.getTabledata().getStringContent();

		// extract metadata
		Properties properties = extractProperties(rawData);

		DataLinks datalinks = extractDataLinks(rawData);
		String description = extractNetworkDescription(rawData);
		HashMap<String, Object> extraValues = extractAdditionalInfo(rawData);

		if (super.dataIndex == -1) {
			super.dataIndex = timeIndicator;
			super.timeIndicator++;
		}

		super.metadata = new Metadata(super.getTabledata().getFileName());
		super.metadata.setDataIndex(super.dataIndex);

		metadata.setDatalinks(datalinks);

		metadata.setProperties(properties);

		metadata.setNetworkDescription(description);

		Iterator<String> metaiterator = extraValues.keySet().iterator();
		while (metaiterator.hasNext()) {
			String key = metaiterator.next().toString();
			Object value = extraValues.get(key);
			metadata.addNetworkInfo(key, value);
		}
		// extract data set
		super.dataset = new Dataset();
		JSONObject network = (JSONObject) JSONValue.parse(rawData);
		JSONArray jArray = (JSONArray) network.get("data");

		for (Object abstractField : jArray) {
			JSONObject jdataField = (JSONObject) abstractField;
			String id = jdataField.get("id").toString();
			String label = jdataField.get("label").toString();

			DataField datafield = new DataField(id, label);

			if (jdataField.get("timeappearance") != null) {
				Object time = jdataField.get("timeappearance");
				if (time instanceof JSONArray) {
					JSONArray timedata = (JSONArray) jdataField.get("timeappearance");
					for (Object timeappearance : timedata) {
						datafield.addTimeAppearance(timeappearance.toString());
					}
				} else if (time instanceof JSONObject) {
					datafield.setTimeAppearance((JSONObject) time);
				}
			}

			Iterator attributes = jdataField.keySet().iterator();

			while (attributes.hasNext()) {
				Object key = attributes.next();
				Object data = jdataField.get(key);
				if (key != null && data != null && !key.toString().equals("id") && !key.toString().equals("label")
						&& !key.toString().equals("timeappearance"))
					datafield.addProperty(key.toString().trim(), data.toString());

			}
			dataset.addDataField(datafield);
		}

	}

	@Override
	public String encode() {
		Metadata metadata = super.getParsedMetadata();
		Dataset dataset = super.getParsedDataSet();

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
		return ".stf";
	}

	@Override
	public String requestFileType() {
		return "stf";
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

	@SuppressWarnings("unchecked")
	private Properties extractProperties(String rawData) {
		Properties properties = new Properties();
		JSONObject network = (JSONObject) JSONValue.parse(rawData);
		JSONArray jproperties = (JSONArray) ((JSONObject) network.get("metadata")).get("properties");
		if (jproperties != null) {
			for (Object object : jproperties) {

				JSONObject jProperty = (JSONObject) object;
				Property property = new Property();
				String sProperty = (String) jProperty.get("property");
				if (sProperty != null) {
					property.setPropertyKey(sProperty.trim());
				}
				String title = (String) jProperty.get("title");
				if (title != null) {
					property.setTitle(title.trim());
				}
				String parsingType = (String) jProperty.get("parsingtype");
				if (parsingType != null) {
					property.setParsingType(parsingType.trim());
				}
				String description = (String) jProperty.get("description");
				if (description != null) {
					property.setDescription(description.trim());
				}
				String specificType = (String) jProperty.get("specifictype");
				if (specificType != null) {
					property.setSpecificType(specificType.trim());
				}
				properties.add(property);
			}
		}

		return properties;
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
