package eu.sisob.api.parser.csv;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.json.simple.JSONObject;

import au.com.bytecode.opencsv.CSVReader;
import eu.sisob.api.parser.DataTableParser;
import eu.sisob.api.visualization.format.datatable.Dataset;
import eu.sisob.api.visualization.format.datatable.fields.DataField;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.Properties;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;

public class CSVExcelParser extends DataTableParser {

	private String[] ignoreMeasureList = { "id", "year", "timeappearance" };

	@Override
	@SuppressWarnings("unchecked")
	public void parse() throws IllegalContentTypeException {

		CSVReader csvReader = new CSVReader(new StringReader(super.getTabledata().getStringContent().replaceAll(";", ",")));

		try {
			List<String[]> entries = csvReader.readAll();

			if (super.dataIndex == -1) {
				super.dataIndex = timeIndicator;
				super.timeIndicator++;
			}

			Vector<String> headers = extractHeader(entries);
			Vector<Boolean> measures = extractMeasures(entries);

			if (headers.size() == measures.size()) {

				boolean isIDAvailable = false;
				boolean isLabelAvailable = false;

				for (int i = 0; i < headers.size(); i++) {
					String key = headers.get(i);
					if (key.equalsIgnoreCase("id")) {
						isIDAvailable = true;
						headers.set(i, "id");
						measures.set(i, false);
					} else if (key.equalsIgnoreCase("label")) {
						isLabelAvailable = true;
						headers.set(i, "label");
						measures.set(i, false);
					}
				}

				if (!isIDAvailable) {
					headers.add("id");
					measures.add(false);
				}

				if (!isLabelAvailable) {
					headers.add("label");
					measures.add(false);
				}

				measures = adjustTimeProperty(headers, measures);

				// generate the metadata section
				super.metadata = new Metadata(super.getTabledata().getFileName());
				super.metadata.setDataIndex(super.dataIndex);

				Properties properties = new Properties();

				for (int i = 0; i < headers.size(); i++) {
					String key = headers.get(i);
					Boolean isValueAMeasure = measures.get(i);
					if (isValueAMeasure) {
						properties.addProperty(new Property(key, "double"));
					} else {
						properties.addProperty(new Property(key));
					}
				}

				metadata.setProperties(properties);

				// generate the data field section
				super.dataset = new Dataset();

				for (int i = 1; i < entries.size(); i++) {
					String[] tokens = (String[]) entries.get(i);
					DataField datafield = new DataField();
					for (int j = 0; j < headers.size(); j++) {
						String key = headers.get(j);
						if (j < tokens.length) {
							String value = tokens[j];
							datafield.addProperty(key, value);
						} else {
							if (key.equalsIgnoreCase("id")) {
								datafield.addProperty(key, "" + i);
							} else if (key.equalsIgnoreCase("label")) {
								datafield.addProperty(key, "Label " + i);
							}
						}
					}
					super.dataset.add(datafield);
				}

			} else {
				throw new IllegalContentTypeException("Unable to match csv headers with the availble data.");
			}

		} catch (IOException ex) {
			throw new IllegalContentTypeException("IOException in OpenCSVParser: " + ex.getMessage());
		} finally {
			try {
				csvReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private Vector<String> extractHeader(List<String[]> entries) {
		Vector<String> headers = new Vector<String>();
		String dataLine[] = entries.get(0);
		for (String data : dataLine) {
			headers.add(data);
		}
		return headers;
	}

	private Vector<Boolean> extractMeasures(List<String[]> entries) {
		Vector<Boolean> measures = new Vector<Boolean>();
		String dataLine[] = entries.get(1);
		for (String data : dataLine) {
			measures.add(isDataAMeasure(data));
		}
		return measures;
	}

	private Vector<Boolean> adjustTimeProperty(Vector<String> headers, Vector<Boolean> measures) {

		for (int i = 0; i < ignoreMeasureList.length; i++) {
			String header = ignoreMeasureList[i];
			for (int j = 0; j < headers.size(); j++) {
				String possibleTimeNotation = headers.get(j);
				if (header.equalsIgnoreCase(possibleTimeNotation)) {
					measures.set(j, false);
				}
			}
		}
		return measures;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public String encode() {
		StringBuilder encodedCSV = new StringBuilder();
		Vector<String> keys = new Vector<String>();

		// header section
		// String header = "";
		for (Object object : super.getParsedDataSet().getDataSetAsJSON()) {
			JSONObject dataField = (JSONObject) object;
			Iterator keyIterator = dataField.keySet().iterator();
			while (keyIterator.hasNext()) {
				String key = keyIterator.next().toString();
				keys.add(key);
			}
			break;
		}

		for (int i = 0; i < keys.size(); i++) {
			if ((i + 1) < keys.size())
				encodedCSV.append(keys.get(i) + ";");
			else
				encodedCSV.append(keys.get(i));
		}
		encodedCSV.append("\n");

		for (Object object : super.getParsedDataSet().getDataSetAsJSON()) {
			JSONObject jobject = (JSONObject) object;
			for (int i = 0; i < keys.size(); i++) {
				String value = (String) jobject.get(keys.get(i));
				value = value == null ? "" : value;
				if ((i + 1) < keys.size()) {
					if (keys.get(i).equals("label")) {
						encodedCSV.append("\"" + value + "\"" + ";");
					} else {
						if (isNumeric(value)) {
							encodedCSV.append(value.replaceAll("\\.", ",") + ";");
						} else {
							encodedCSV.append(value + ";");
						}
					}

				} else {
					if (keys.get(i).equals("label")) {
						encodedCSV.append("\"" + value + "\"");
					} else {
						if (isNumeric(value)) {
							encodedCSV.append(value.replaceAll("\\.", ","));
						} else {
							encodedCSV.append(value);
						}
					}
				}
			}
			encodedCSV.append("\n");
		}
		return encodedCSV.toString();
	}

	private boolean isNumeric(String str) {
		try {
			Float.parseFloat(str);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	@Override
	public String requestFileTypeExtension() {
		return ".csv";
	}

	@Override
	public String requestFileType() {
		return "csv";
	}

	private boolean isDataAMeasure(String data) {
		try {
			Double.parseDouble(data);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
