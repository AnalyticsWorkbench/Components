/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.workbench.components.swimlanevisualization;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.VisualizationTechnique;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.NodeProperties;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

/**
 *
 * @author hecking
 */
public class SwimLaneTechnique extends VisualizationTechnique {

    private NodeSet nodeset;

    private EdgeSet edgeset;

    private Metadata metadata;

    private String yAxisCriteria;

    private String xAxisCriteria;

    public static final String DEFAULT_X_CRITERIA = "default";

    public static final String DEFAULT_Y_CRITERIA = "default";

    public SwimLaneTechnique(JSONFile network, String yAxisCriteria, String xAxisCriteria) {

        super(network);

        this.yAxisCriteria = yAxisCriteria.trim();

        this.xAxisCriteria = xAxisCriteria.trim();

    }

    public void applyLayout() throws Exception {

        DirectedSparseGraph<SwimLaneNode, Edge> network = convertJSONDataToGraph(super.getNetwork());

        // if there is a criteria to build the x Axis then...
        if (!xAxisCriteria.isEmpty()) {

            //if it is axis the default criteria...
            if (xAxisCriteria.equalsIgnoreCase(SwimLaneTechnique.DEFAULT_X_CRITERIA)) {

                detectNodeNeighbors(network);

                Vector<SwimLaneNode> rootNodes = detectRootNodes(network);

                if (rootNodes.size() > 0) {

                    int scanning = 1;

                    System.out.println("Calculating max distance...");

                    for (SwimLaneNode rootNode : rootNodes) {

                        System.out.println("Scanning root: " + scanning + " of " + rootNodes.size());

                        Iterator<SwimLaneNode> nodeIterator = network.getVertices().iterator();

                        while (nodeIterator.hasNext()) {

                            SwimLaneNode node = nodeIterator.next();

                            if (!node.isRootNode()) {

                                Stack<SwimLaneNode> path = calculatePath(rootNode, node);

                                if (path != null) {

                                    if (node.getLongestPath().size() < path.size()) {

                                        node.setLongestPath(path);

                                    }

                                }

                                clearAllNodes(network);

                            }

                        }

                        scanning++;

                    }

                    Iterator<SwimLaneNode> nodeIterator = network.getVertices().iterator();

                    while (nodeIterator.hasNext()) {

                        SwimLaneNode sln = nodeIterator.next();

                        Node node = sln.getNode();

                        node.addProperty("xvalue", "" + sln.getLongestPath().size());

                    }

                } else {

                    throw new IllegalContentTypeException("Unable to locate the root nodes in the data set");

                }

            } else { // if it is custom criteria then...                                 

                Iterator<SwimLaneNode> nodeIterator = network.getVertices().iterator();

                while (nodeIterator.hasNext()) {

                    SwimLaneNode sln = nodeIterator.next();

                    Node node = sln.getNode();

                    String criteriaValue = node.getProperty(xAxisCriteria);

                    if (criteriaValue != null) {
                        node.addProperty("xvalue", criteriaValue);
                    } else {
                        throw new IllegalContentTypeException("The requested criteria is not available in the data set.");
                    }

                }

            }

        } else {
            throw new IllegalContentTypeException("SwimLane Error: Unable to generate axis with empty criteria.");
        }

        //if there is a criteria to build the Y Axis then...
        if (!yAxisCriteria.isEmpty()) {

            //if it is axis the default criteria...
            if (yAxisCriteria.equalsIgnoreCase(SwimLaneTechnique.DEFAULT_Y_CRITERIA)) {

                Iterator<SwimLaneNode> nodeIterator = network.getVertices().iterator();

                while (nodeIterator.hasNext()) {

                    SwimLaneNode sln = nodeIterator.next();

                    Node node = sln.getNode();

                    node.addProperty("yvalue", node.getLabel());

                }

            } else { // if it is custom criteria then...

                Iterator<SwimLaneNode> nodeIterator = network.getVertices().iterator();

                while (nodeIterator.hasNext()) {

                    SwimLaneNode sln = nodeIterator.next();

                    Node node = sln.getNode();

                    String criteriaValue = node.getProperty(yAxisCriteria);

                    if (criteriaValue != null) {
                        node.addProperty("yvalue", criteriaValue);
                    } else {
                        throw new IllegalContentTypeException("The requested criteria is not available in the data set.");
                    }

                }

            }

        } else {
            throw new IllegalContentTypeException("SwimLane Error: Unable to generate axis with empty criteria.");
        }

    }

    private void detectNodeNeighbors(DirectedSparseGraph<SwimLaneNode, Edge> network) {

        Iterator<Edge> edgeItarator = network.getEdges().iterator();

        while (edgeItarator.hasNext()) {

            Edge edge = edgeItarator.next();

            SwimLaneNode source = detectNode(edge.getSource(), network);

            SwimLaneNode target = detectNode(edge.getTarget(), network);

            source.getNeighbors().add(target);

        }

    }

    private SwimLaneNode detectNode(String requestedNodeID, DirectedSparseGraph<SwimLaneNode, Edge> network) {

        Iterator<SwimLaneNode> iterator = network.getVertices().iterator();

        while (iterator.hasNext()) {

            SwimLaneNode node = iterator.next();

            if (requestedNodeID.trim().equals(node.getNode().getId().trim())) {
                return node;
            }

        }

        return null;

    }

    private Stack<SwimLaneNode> calculatePath(SwimLaneNode root, SwimLaneNode target) {

        Stack<SwimLaneNode> stack = new Stack<SwimLaneNode>();

        //set the root for the exploration
        stack.push(root);

        // set the node as visited
        root.setVisited(true);

        //while the stack is not empty
        while (!stack.isEmpty()) {

            //look the node that is in the top of the stack
            SwimLaneNode node = stack.peek();

            // get child nodes from the last node
            SwimLaneNode child = getUnvisitedNeighbor(node);

            // if the node has a child
            if (child != null) {

                // if we reached the target
                if (child.getNode().getId().trim().equals(target.getNode().getId().trim())) {

                    return stack;

                } else {

                    // mark as visited
                    child.setVisited(true);

                    // insert to stack;
                    stack.push(child);

                }

            } else {

                // remove the node that is in the top of the stack
                stack.pop();

            }

        }

        return null;

    }

    private SwimLaneNode getUnvisitedNeighbor(SwimLaneNode node) {

        for (SwimLaneNode neighbor : node.getNeighbors()) {

            if (!neighbor.isVisited()) {

                return neighbor;

            }

        }

        return null;

    }

    private void clearAllNodes(DirectedSparseGraph<SwimLaneNode, Edge> network) {

        // TODO Auto-generated method stub                
        Iterator<SwimLaneNode> nodeIterator = network.getVertices().iterator();

        while (nodeIterator.hasNext()) {

            SwimLaneNode node = nodeIterator.next();

            node.setVisited(false);

        }

    }

    private Vector<SwimLaneNode> detectRootNodes(DirectedSparseGraph<SwimLaneNode, Edge> network) {

        Iterator<SwimLaneNode> networkIterator = network.getVertices().iterator();

        Vector<SwimLaneNode> rootNodes = new Vector<SwimLaneNode>();

        while (networkIterator.hasNext()) {

            SwimLaneNode node = networkIterator.next();

            int inDegree = network.inDegree(node);

            if (inDegree == 0) {

                node.setRootNode(true);

                rootNodes.add(node);

            }

        }

        return rootNodes;

    }

    private DirectedSparseGraph<SwimLaneNode, Edge> convertJSONDataToGraph(JSONFile network) throws Exception {

        DirectedSparseGraph<SwimLaneNode, Edge> graph = new DirectedSparseGraph<SwimLaneNode, Edge>();

        SGFParser parser = new SGFParser();

        parser.setNetwork(network);

        parser.parse();

        this.nodeset = parser.getParsedNodeSet();

        this.edgeset = parser.getParsedEdgeSet();

        for (Node n : nodeset.getValues()) {

            SwimLaneNode node = new SwimLaneNode(n);

            graph.addVertex(node);

        }

        for (Edge e : edgeset.getValues()) {

            SwimLaneNode source = wrapNode(e.getSource(), graph.getVertices());

            SwimLaneNode target = wrapNode(e.getTarget(), graph.getVertices());

            graph.addEdge(e, source, target);

        }

        this.metadata = parser.getParsedMetadata();

        return graph;

    }

    private SwimLaneNode wrapNode(String id, Collection<SwimLaneNode> collection) {

        // TODO Auto-generated method stub        
        Iterator<SwimLaneNode> nodeIterator = collection.iterator();

        while (nodeIterator.hasNext()) {

            SwimLaneNode node = nodeIterator.next();

            if (id.trim().equals(node.getNode().getId().trim())) {
                return node;
            }

        }

        return null;

    }

    public Metadata updatedMetadata() throws Exception {

        // TODO Auto-generated method stub
        NodeProperties properties = this.metadata.getNodeproperties();

        Property p;

        if (properties == null) {

            properties = new NodeProperties();

        }
        properties.addProperty(new Property("xvalue", "string"));
        properties.addProperty(new Property("yvalue", "string"));

        this.metadata.setNodeproperties(properties);

        return this.metadata;

    }

    public NodeSet updatedNodeSet() throws Exception {

        // TODO Auto-generated method stub
        return this.nodeset;

    }

}
