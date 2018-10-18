//package eu.sisob.api.visualization.format;
//
//import eu.sisob.api.visualization.format.datatable.Dataset;
//import eu.sisob.api.visualization.format.datatable.fields.DataField;
//import eu.sisob.api.visualization.format.metadata.Metadata;
//import eu.sisob.api.visualization.format.metadata.fields.DataLinks;
//import eu.sisob.api.visualization.format.metadata.fields.Dimension;
//import eu.sisob.api.visualization.format.metadata.fields.Dimensions;
//import eu.sisob.api.visualization.format.metadata.fields.Measure;
//import eu.sisob.api.visualization.format.metadata.fields.Measures;
//
//public class DataTableEncodingExample {
//
//	public static void main (String args[]){
//		// creating the metadata
////		Metadata metadata = new Metadata("Example", "Dynamic Visualization", "false");
//		Metadata metadata = new Metadata("Example","Dynamic Visualization");
//		metadata.setDirectedNetwork("false");
//		
//		// creating the measures
//		Measures measures = new Measures();
//		measures.addMeasure(new Measure("Degree Centrality", "cDegree", "array", "node", "none"));
//		measures.addMeasure(new Measure("Closeness Centrality", "cCloseness", "array", "node", "none"));
//		
//		// creating the dimensions
//		Dimensions dimensions = new Dimensions();
//		dimensions.addDimension(new Dimension("Year", "year", "timeAppearance", "time dimension"));
//		
//		// creating the data links
//		DataLinks datalinks = new DataLinks();
//		
//	
//		
//		
//		// puting the meta data together
//		
//		metadata.setDimensions(dimensions);
//		metadata.setMeasures(measures);
//		
////		
//		System.out.println("\"metadata\":"+metadata.getMetadataAsJSON().toJSONString());
//		
//		// creating the data
//		
//		Dataset data=new Dataset();
//		DataField df = new DataField();		
//		df.addMeasure("cDegree", "0");
//		df.addMeasure("cCloseness", "1");
//		data.addDataField(df);
//		
//		
//		System.out.println("\"data\""+data.getDataSetAsJSON());
//		
//	}
//	
//}
