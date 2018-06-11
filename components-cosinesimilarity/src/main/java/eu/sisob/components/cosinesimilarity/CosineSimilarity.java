package eu.sisob.components.cosinesimilarity;

import java.math.BigDecimal;
import java.util.Vector;

import Jama.Matrix;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.EdgeProperties;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.json.util.JSONFile;

public class CosineSimilarity {
	
	public CosineSimilarity (){
		
	}
	
	public JSONFile calculateCosineSimilarity(JSONFile jsonFile) throws Exception{
		// TODO Auto-generated method stub
		SGFParser parser = new SGFParser();
		parser.setNetwork(jsonFile);
		parser.parse();
		
		Metadata metadata = parser.getParsedMetadata();
		NodeSet nodeset = parser.getParsedNodeSet();
		EdgeSet edgeset = parser.getParsedEdgeSet();
		
		Vector<Node> documents = extractEntities(nodeset, "0");
		Vector<Node> words = extractEntities(nodeset, "1");		
		
		Matrix adjacencyMatrix = createAdjacencyMatrix(words, documents,edgeset);
		Matrix wordWordMatrix = cosineSimilarity(adjacencyMatrix);
				
		nodeset = createNodeSet(words);
		edgeset = createEdgeSet(wordWordMatrix);
		
		metadata = new Metadata("Word Similarity");
		
		EdgeProperties edgeProps = new EdgeProperties();
		Property similarity = new Property("sim", "double", "Word Similarity", "similarity between words", null);
		edgeProps.addProperty(similarity);
		
		metadata.setEdgeproperties(edgeProps);
		
		
		parser.updateMetadata(metadata);
		parser.updateEdgeSet(edgeset);
		parser.updateNodeSet(nodeset);
		
		String data = parser.encode();
		JSONFile simgraph = new JSONFile("Word Similarity.sgf", ".sgf", data, JSONFile.TEXT);
		
		return simgraph;
		
	}	
	

	private EdgeSet createEdgeSet(Matrix wordWordMatrix) {
		// TODO Auto-generated method stub
		EdgeSet edgeset = new EdgeSet();		
		int edgeID = 0;
		for(int i=0;i<wordWordMatrix.getRowDimension();i++){
			for(int j=0;j<wordWordMatrix.getColumnDimension();j++){
				if(i!=j){					
					Edge edge = new Edge(""+edgeID,""+i,""+j);
					edge.addMeasure("sim", ""+wordWordMatrix.get(i, j));
					edgeset.addEdge(edge);
					edgeID++;
				}
			}
		}		
		return edgeset;
	}

	@SuppressWarnings("unchecked")
	private NodeSet createNodeSet(Vector<Node> words) {
		NodeSet wordset = new NodeSet();		
		for(int i=0;i<words.size();i++){
			Node node = new Node(""+i,words.get(i).getLabel());
			node.addProperty("type", "0");
			wordset.add(node);
		}		
		return wordset;
	}
	

	private Matrix cosineSimilarity(Matrix adjacencyMatrix) {
		//word x word matrix
		Matrix wordMatrix = new Matrix(adjacencyMatrix.getRowDimension(),adjacencyMatrix.getRowDimension());
		
		//initialize word matrix		
		for(int i=0;i<wordMatrix.getRowDimension();i++){
			for(int j=0;j<wordMatrix.getColumnDimension();j++){
				if(i!=j)
					wordMatrix.set(i, j,-1.0);							
				else
					wordMatrix.set(i, j,1.0);			
			}
		}		

		for(int i=0;i<wordMatrix.getRowDimension();i++){
			// first word extract the first vector
			double [] vectorA = extractVector(adjacencyMatrix, i);
			for(int j=0;j<wordMatrix.getColumnDimension();j++){
				//if the words are "different"
				if(i!=j && wordMatrix.get(i, j)==-1.0){
					// extracts the second vector
					double [] vectorB = extractVector(adjacencyMatrix, j);					
					//similarity between vectors
					double sim = similarity(vectorA, vectorB);
					wordMatrix.set(i, j,sim);
					wordMatrix.set(j, i,sim);
				}
			}
		}
		
		return wordMatrix;
	}

	private double[] extractVector(Matrix adjacencyMatrix, int row) {
		// TODO Auto-generated method stub
		int numberOfColumns = adjacencyMatrix.getColumnDimension();
		double [] vectorX = new double[adjacencyMatrix.getColumnDimension()];	
		
		for(int column=0;column<numberOfColumns;column++)
			vectorX[column]=adjacencyMatrix.get(row, column);		    	
		
		return vectorX;
	}

	private double similarity(double[] vectorA, double[] vectorB) {
		// TODO Auto-generated method stub
		double similarity = 0.0;
		double sum = 0.0;	// the numerator of the cosine similarity
		double fnorm = 0.0;	// the first part of the denominator of the cosine similarity
		double snorm = 0.0;	// the second part of the denominator of the cosine similarity
		
	    for(int i=0; i<vectorA.length;i++){
	    	double frequencyA = vectorA[i];
	    	double frequencyB = vectorB[i];	    	
	    	sum = sum + frequencyA * frequencyB;
	    }
	    
		fnorm = calculateNorm(vectorA);
		snorm = calculateNorm(vectorB);
		
		similarity = sum / (fnorm * snorm);
		
		BigDecimal bd = new BigDecimal(Double.toString(similarity));
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	private Double calculateNorm(double [] vectorX) {
		// TODO Auto-generated method stub
		double norm = 0.0;
		for(int i=0;i<vectorX.length;i++){
			norm = norm + Math.pow(vectorX[i], 2);
		}		
		return Math.sqrt(norm);
	}

	private Matrix createAdjacencyMatrix(Vector<Node>words, Vector<Node> documents, EdgeSet edgeset) {
		// TODO Auto-generated method stub
		Matrix adjacencyMatrix = new Matrix(words.size(),documents.size());		
		for(Edge edge : edgeset.getValues()){
			double frequency = Double.parseDouble(edge.getMeasure("wf").toString());			
			String source = edge.getSource();
			String target = edge.getTarget();			
			int row;
			int column;			
			if((row = searchNode(source,words))!=-1){ //connection from the set A to the set B
				column = searchNode(target,documents);				
				if(row != -1 && column!=-1){
					adjacencyMatrix.set(row, column, frequency);
				}	
				
			}else{
				row = searchNode(source, documents); // connection from the set B to the set A
				column = searchNode(target,words);
				if(row != -1 && column!=-1){
					adjacencyMatrix.set(column, row, frequency);
				}					
			}			
		}		
		
		return adjacencyMatrix;
	}
	
	private Vector<Node> extractEntities(NodeSet nodeset, String entityType){
		Vector<Node> entities = new Vector<Node>();
		for(int i=0;i<nodeset.size();i++){
			   Node node = (Node)nodeset.get(i);
			   if(node.getProperty("type").toString().equals(entityType)){
				   entities.add(node);
			   }
			}		
		
		return entities;
	}	
	
	private int searchNode(String id, Vector<Node> entities) {
		for(int i=0;i<entities.size();i++){
			Node node = entities.get(i);
			if(node.getId().equals(id)){
				return i;
			}
		}		
		return -1;
	}

}
