/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.api.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

/**
 *
 * @author hecking
 */
public class ClusterUtilities {
	
	public static final String CLUSTERS_PROPERTY = "clusters";
    
    public static Map<String, Set<Node>> getClusters(JSONFile network) throws IllegalContentTypeException{
        
        Map<String, Set<Node>> clusters = new HashMap<String, Set<Node>>();
        NetworkParser parser = new SGFParser();
        Vector<Node> nodes;
        JSONArray assignedClusters; 
        Set<Node> initialSet;
        
        parser.setNetwork(network);
        
        parser.parse();
        nodes = parser.getParsedNodeSet().getValues();
        
        for (Node node : nodes) {
            
            assignedClusters = (JSONArray)JSONValue.parse(node.getProperty(CLUSTERS_PROPERTY));
            
            for (int i = 0; i < assignedClusters.size(); i = i + 1) {
                
                if (clusters.containsKey(assignedClusters.get(i))) {
                    
                    clusters.get(assignedClusters.get(i)).add(node);
                
                } else {
                    
                    initialSet = new HashSet<Node>();
                    initialSet.add(node);
                    clusters.put(assignedClusters.get(i).toString(), initialSet);
                }
            }
        }
        
        return clusters;
    }
}
