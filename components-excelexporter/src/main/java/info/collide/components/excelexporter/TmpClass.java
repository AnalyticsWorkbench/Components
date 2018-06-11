//package info.collide.components.excelexporter;
//
//import java.io.ByteArrayOutputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.Hashtable;
//
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.xssf.streaming.SXSSFWorkbook;
//import org.json.simple.JSONArray;
//
//import eu.sisob.api.visualization.format.datatable.Dataset;
//import eu.sisob.api.visualization.format.datatable.fields.DataField;
//import eu.sisob.api.visualization.format.metadata.fields.Property;
//
//public class TmpClass {
//
//	public TmpClass() {
//		// TODO Auto-generated constructor stub
//	}
//	
//	public void encode() {
//		// parse information about data content and headers
//		JSONArray propertySet = super.getParsedMetadata().getProperties();
//		Dataset dataset = super.getParsedDataSet();
//		Hashtable<String, String> parsingTypes = new Hashtable<String, String>();
//		String[] keys = new String[propertySet.size()];
//		String[] headers = new String[propertySet.size()];
//		for (int i = 0; i < propertySet.size(); i++) {
//			Property property = (Property) propertySet.get(i);
//			keys[i] = property.getPropertyKey();
//			String entry = property.getTitle();
//			if (entry == null || entry.trim().isEmpty()) {
//				entry = property.getPropertyKey();
//			}
//			headers[i] = entry;
//			String parsingType = property.getParsingType();
//			if (parsingType == null || parsingType.trim().isEmpty()) {
//				parsingType = "String";
//			}
//			parsingTypes.put(property.getPropertyKey(), parsingType);
//		}
//		
//		// create the excel workbook and sheet to store the result into
//		SXSSFWorkbook wb = new SXSSFWorkbook(100); // keep 5 rows in memory
//		Sheet sh = wb.createSheet();
//		// create first row for column headers
//		int rowCounter = 0;
//		Row row = sh.createRow(rowCounter);
//		// fill in the column header cells
//		for (int i = 0; i < headers.length; i++) {
//			Cell cell = row.createCell(i);
//			cell.setCellValue(headers[i]);
//		}
//		
//		for (int i = 0; i < dataset.size(); i++) {
//			DataField df = (DataField) dataset.get(i);
//			rowCounter++;
//			row = sh.createRow(rowCounter);
//			int cellCounter = -1;
//			for (String key : keys) {
//				Cell cell = row.createCell(++cellCounter);
//				try {
//					if (parsingTypes.get(key).equalsIgnoreCase("Integer")) {
//						cell.setCellValue(Integer.parseInt(df.get(key).toString()));
//					} else if (parsingTypes.get(key).equalsIgnoreCase("Integer")) {
//						cell.setCellValue(Double.parseDouble(df.get(key).toString()));
//					} else {
//						cell.setCellValue(df.get(key).toString());
//					}
//				} catch (NumberFormatException ex) {
//					cell.setCellValue(df.get(key).toString());
//				}
//			}
//		}
//		try {
//			// write wb to stream
//			FileOutputStream fos = new FileOutputStream();
//			wb.write(fos);
//			
//			fos.flush();
//			fos.close();
//			
//			// close resources
//			wb.dispose();
//			wb.close();
//		} catch (IOException ex) {
//			// FIXME
//		}
//
//}
