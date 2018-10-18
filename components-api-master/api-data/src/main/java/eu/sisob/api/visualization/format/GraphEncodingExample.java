//package eu.sisob.api.visualization.format;
//import java.util.Iterator;
//import java.util.Vector;
//
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.JSONValue;
//
//import eu.sisob.api.visualization.format.graph.Dataset;
//import eu.sisob.api.visualization.format.graph.fields.Edge;
//import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
//import eu.sisob.api.visualization.format.graph.fields.Node;
//import eu.sisob.api.visualization.format.graph.fields.NodeSet;
//import eu.sisob.api.visualization.format.metadata.Metadata;
//import eu.sisob.api.visualization.format.metadata.fields.DataLinks;
//import eu.sisob.api.visualization.format.metadata.fields.Dimension;
//import eu.sisob.api.visualization.format.metadata.fields.Dimensions;
//import eu.sisob.api.visualization.format.metadata.fields.EdgeProperties;
//import eu.sisob.api.visualization.format.metadata.fields.Measure;
//import eu.sisob.api.visualization.format.metadata.fields.Measures;
//import eu.sisob.api.visualization.format.metadata.fields.NodeProperties;
//import eu.sisob.api.visualization.format.metadata.fields.Property;
//
//
//public class GraphEncodingExample {
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
//		//creating node properties
//		NodeProperties nodeProperties = new NodeProperties();		
//		
////		Property nodeproperty = new Property();		
////		nodeproperty.setPropertyName("entities");
////		nodeproperty.addValue("Person");
////		nodeproperty.addValue("Activity");
////		nodeProperties.addProperty(nodeproperty);
//		
//		
//		
//		Property nodeproperty2 = new Property();	
//		nodeproperty2.setPropertyName("clusters");
//		nodeproperty2.addValue("array");
//		
//		nodeProperties.addProperty(nodeproperty2);
//		
////		Creating edge properties
//		EdgeProperties edgeProperties = new EdgeProperties();
////		Property edgeProperty = new Property();
//		
////		Vector<String>edgeEntities = new Vector<String>();
////		edgeEntities.add("Paper co-authorship");
////		edgeEntities.add("Conference Attendance");
////		edgeEntities.add("Project Collaborator");
////		
////		edgeProperty.setPropertyName("entities");
////		edgeProperty.setValues(edgeEntities);
//		
////		edgeProperties.addProperty(edgeProperty);
//		
//		
//		// puting the meta data together
//		
//		metadata.setDimensions(dimensions);
//		metadata.setMeasures(measures);
//		metadata.setNodeproperties(nodeProperties);
//		metadata.setEdgeproperties(edgeProperties);
////		
//		System.out.println("\"metadata\":"+metadata.getMetadataAsJSON().toJSONString());
//		
//		// creating the data
//		
//		Dataset data = new Dataset();
//		NodeSet nodeset=new NodeSet();
//		EdgeSet edgeset = new EdgeSet();
//		
//		Node test = new Node("7","X");
//		test.setCoordinates("10","10");
//		test.addTimeAppearance("2000");
//		test.addMeasure("CD", "1000");
//		test.addProperty("property", "X");
//		JSONArray array = new JSONArray();
//		array.add("1");
//		array.add("2");
//		array.add("3");
//		array.add("4");
//		
//		test.addProperty("clusters", array);
//		
////		nodeset.addNode(new Node("1","B"));
////		nodeset.addNode(new Node("3","C"));
////		nodeset.addNode(new Node("4","A"));
////		nodeset.addNode(new Node("5","D"));
//		nodeset.addNode(test);
//		
//		edgeset.addEdge(new Edge("1","none","A","B"));
//		Edge testEdge = new Edge("12","none","D","D");
//		
//		
//		data.setNodeSet(nodeset);
//		data.setEdgeSet(edgeset);
//		
//		System.out.println("\"data\""+data.getDataSetAsJSON());
//		
//		
//	}
//}
