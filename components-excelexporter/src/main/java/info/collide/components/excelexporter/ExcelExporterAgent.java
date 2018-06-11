package info.collide.components.excelexporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.simple.JSONArray;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.api.parser.DataTableParser;
import eu.sisob.api.parser.sisob.SDTParser;
import eu.sisob.api.visualization.format.datatable.Dataset;
import eu.sisob.api.visualization.format.datatable.fields.DataField;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

public class ExcelExporterAgent extends Agent {

	Vector<JSONFile> data;

	public ExcelExporterAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {
		// translate to Excel
		data = new Vector<JSONFile>();
		// here we extract the data from a data message
		Vector<JSONFile> inputData = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
		try {
			for (JSONFile input : inputData) {
				data.add(convertTable(input));
			}

			// update results
			this.uploadResults();

			// indicate we are done
			indicateDone();
		} catch (IOException ex) {
			indicateError("Could not convert data", ex);
		} catch (IllegalContentTypeException ex) {
			indicateError("Please check your workflow, input seems not to be in SiSOB Datatable Format", ex);
		}
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		throw new UnsupportedOperationException("This method is not implemented.");
	}

	@Override
	protected void uploadResults() {
		// convert the data into a String
		String uploadData = JSONFile.collectionToString(data);

		// just call storeData from the superclass {@link Agent} with the
		// following parameters and everything is just fine
		// 1. Runid, which can be accessed via getWorkflowID()
		// 2. The pipe in which the data "is put". It is constructed from the
		// agent instance id and the output identifier.
		// 3. The data, in this case it is our String uploadData
		storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", uploadData);
	}

	JSONFile convertTable(JSONFile sisobTable) throws IOException, IllegalContentTypeException {
		File f = File.createTempFile("EEA", ".xlsx");
		f.deleteOnExit();

		// parse information about data content and headers
		DataTableParser parser = new SDTParser();
		parser.setTabledata(sisobTable);
		parser.parse();
		
		JSONArray propertySet = parser.getParsedMetadata().getProperties();
		Dataset dataset = parser.getParsedDataSet();
		Hashtable<String, String> parsingTypes = new Hashtable<String, String>();
		String[] keys = new String[propertySet.size()];
		String[] headers = new String[propertySet.size()];
		for (int i = 0; i < propertySet.size(); i++) {
			Property property = (Property) propertySet.get(i);
			keys[i] = property.getPropertyKey();
			String entry = property.getTitle();
			if (entry == null || entry.trim().isEmpty()) {
				entry = property.getPropertyKey();
			}
			headers[i] = entry;
			String parsingType = property.getParsingType();
			if (parsingType == null || parsingType.trim().isEmpty()) {
				parsingType = "String";
			}
			parsingTypes.put(property.getPropertyKey(), parsingType);
		}

		// create the excel workbook and sheet to store the result into
		SXSSFWorkbook wb = new SXSSFWorkbook(5); // keep 5 rows in memory
		Sheet sh = wb.createSheet();
		// create first row for column headers
		int rowCounter = 0;
		Row row = sh.createRow(rowCounter);
		// fill in the column header cells
		for (int i = 0; i < headers.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(headers[i]);
		}

		for (int i = 0; i < dataset.size(); i++) {
			DataField df = (DataField) dataset.get(i);
			rowCounter++;
			row = sh.createRow(rowCounter);
			int cellCounter = -1;
			for (String key : keys) {
				Cell cell = row.createCell(++cellCounter);
				if (df.get(key) != null) {
					try {
						if (parsingTypes.get(key).equalsIgnoreCase("Integer")) {
							cell.setCellValue(Integer.parseInt(df.get(key).toString()));
						} else if (parsingTypes.get(key).equalsIgnoreCase("Double")) {
							cell.setCellValue(Double.parseDouble(df.get(key).toString()));
						} else {
							cell.setCellValue(df.get(key).toString());
						}
					} catch (NumberFormatException ex) {
						cell.setCellValue(df.get(key).toString());
					}
				}
			}
		}
		
		// write wb to stream
		FileOutputStream fos = new FileOutputStream(f);
		wb.write(fos);
		
		// close resources
		fos.flush();
		fos.close();

		wb.dispose();
		wb.close();

		return new JSONFile(f);
	}

}
