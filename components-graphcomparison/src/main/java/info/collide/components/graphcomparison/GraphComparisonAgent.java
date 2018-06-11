/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.components.graphcomparison;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.EdgeProperties;
import eu.sisob.api.visualization.format.metadata.fields.NodeProperties;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.json.simple.JSONObject;

/**
 *
 * @author hecking
 */


public class GraphComparisonAgent extends Agent {

    protected boolean byLabel;
    protected List<JSONFile> results;
    protected static String APPEARS_IN = "appearance";

    public GraphComparisonAgent(JsonObject coordinationMsg) {

        super(coordinationMsg);

        this.results = new ArrayList();
        this.byLabel = Boolean.parseBoolean(this.getFilterParameters().get(GraphComparatorManager.BY_LABEL).toString());
    }

    @Override
    public void executeAgent(JsonObject dataMessage) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void executeAgent(List<JsonObject> dataMessages) {

        String input1 = new Gson().toJson(dataMessages.get(0).get("payload"));
        String input2 = new Gson().toJson(dataMessages.get(1).get("payload"));

        Vector<JSONFile> inFiles1 = JSONFile.restoreJSONFileSet(input1);
        Vector<JSONFile> inFiles2 = JSONFile.restoreJSONFileSet(input2);

        if (inFiles1.size() != inFiles2.size()) {

            this.indicateError("Cannot perform comparison. "
                    + "Different number of left and right input graphs.");
        } else {

            try {

                for (int i = 0; i < inFiles1.size(); i = i + 1) {

                    this.results.add(this.compareGraphs(inFiles1.get(i), inFiles2.get(i)));
                }

                this.uploadResults();

            } catch (IllegalContentTypeException ex) {

                this.indicateError("Cannot read the input.");
            }
        }

    }

    @Override
    protected void uploadResults() {

        if (this.results.isEmpty()) {

            // this.deleteWorkspace(new File(this.workspaceUrl));
            this.indicateError("No result data available.");
        } else {

            this.storeData(this.getWorkflowID(), this.getAgentInstanceID() + ".out_1", JSONFile.collectionToString(results));
            this.indicateDone();
        }
    }

    protected JSONFile compareGraphs(JSONFile g1, JSONFile g2) throws IllegalContentTypeException {

        SGFParser graphParser1 = new SGFParser();
        SGFParser graphParser2 = new SGFParser();
        Metadata metadata;
        Set<Property> nodePropsSet = new HashSet();
        Set<Property> edgePropsSet = new HashSet();
        String g1Postfix = "_in_" + g1.getFileName();
        String g2Postfix = "_in_" + g2.getFileName();
        boolean isDirected;

        graphParser1.setNetwork(g1);
        graphParser1.parse();

        graphParser2.setNetwork(g2);
        graphParser2.parse();

        if (this.byLabel) {
            // Set node labels as node node ids.
            this.preprocessNodesAndEdges(graphParser1.getParsedNodeSet().getValues(),
                    graphParser1.getParsedEdgeSet().getValues());
            this.preprocessNodesAndEdges(graphParser2.getParsedNodeSet().getValues(),
                    graphParser2.getParsedEdgeSet().getValues());
        }

        isDirected = (Boolean.parseBoolean(graphParser1.getParsedMetadata().isDirectedNetwork())
                || Boolean.parseBoolean(graphParser2.getParsedMetadata().isDirectedNetwork()));

        // TODO: The following operations have a lot of code duplications for node and edge merging.
        // The code quality could be improved in the future with clever use of interfaces and abstract classes 
        // generalising nodes and edges to graph elements encapsulating the node and edge properties.
        // Requires modifications of the Data API!
        // Properties class does not overwrite equal and hashcode, which makes it more complicated to 
        // compare properties and to treat them as sets.
        
        nodePropsSet.addAll(graphParser1.getParsedMetadata().getNodeproperties());
        nodePropsSet.addAll(graphParser2.getParsedMetadata().getNodeproperties());
        edgePropsSet.addAll(graphParser1.getParsedMetadata().getEdgeproperties());
        edgePropsSet.addAll(graphParser2.getParsedMetadata().getEdgeproperties());

        JSONObject obj = new JSONObject();
        obj.put("metadata", this.mergeMetadata(graphParser1.getParsedMetadata(),
                graphParser2.getParsedMetadata(), g1Postfix, g2Postfix));
        JSONObject data = new JSONObject();
        data.put("nodes", this.mergeNodes(graphParser1.getParsedNodeSet().getValues(),
                graphParser2.getParsedNodeSet().getValues(), nodePropsSet, g1Postfix, g2Postfix));
        data.put("edges", this.mergeEdges(graphParser1.getParsedEdgeSet().getValues(),
                graphParser2.getParsedEdgeSet().getValues(), edgePropsSet, isDirected, g1Postfix, g2Postfix));
        obj.put("data", data);

        return new JSONFile("merged_graph.sgf", "sgf", obj.toJSONString(),
                JSONFile.TEXT);
    }

    protected int containsEdge(Edge query, Vector<Edge> edges, boolean directed) {

        int found = -1;
        int i = 0;
        Iterator<Edge> it = edges.iterator();
        Edge edge;
        while (it.hasNext() && (found == -1)) {

            edge = it.next();

            if (directed) {

                found = ((edge.getSource().equals(query.getSource()))
                        && (edge.getTarget().equals(query.getTarget()))) ? i : -1;
            } else {
                found = ((edge.getSource().equals(query.getSource()))
                        && (edge.getTarget().equals(query.getTarget()))
                        || (edge.getSource().equals(query.getTarget()))
                        && (edge.getTarget().equals(query.getSource()))) ? i : -1;
            }
            i = i + 1;
        }

        return found;
    }

    protected EdgeSet mergeEdges(Vector<Edge> edges1, Vector<Edge> edges2, Set<Property> edgeProps, boolean directed, String propPostfix1, String propPostfix2) {

        EdgeSet mergedEdgeSet = new EdgeSet();
        Edge matchedEdge;
        int matchedIndex;
        int eId = 0;

        for (Edge edge : edges1) {

            this.transformEdgeProperties(edge, edgeProps, propPostfix1);
            matchedIndex = this.containsEdge(edge, edges2, directed);
            if (matchedIndex >= 0) {

                matchedEdge = edges2.remove(matchedIndex);

                // Transform matchedNode property keys to KEY + propPostfix2
                this.transformEdgeProperties(matchedEdge, edgeProps, propPostfix2);

                // Combine properties
                for (String propKey : matchedEdge.getPropertyKeys()) {

                    edge.addProperty(propKey, matchedEdge.getProperty(propKey));
                }
                edge.addProperty(APPEARS_IN, "both");

            } else {

                edge.addProperty(APPEARS_IN, propPostfix1);
                // Add properties KEY + propPostfix2
                // NOTE: This only works since the original properties have been 
                // transformed before!
                this.transformEdgeProperties(edge, edgeProps, propPostfix2);
            }

            edge.setId(Integer.toString(eId));
            mergedEdgeSet.addEdge(edge);
            eId = eId + 1;
        }

        for (Edge edge : edges2) {

            this.transformEdgeProperties(edge, edgeProps, propPostfix2);
            // NOTE: See previous note on transforming node properties twice.
            this.transformEdgeProperties(edge, edgeProps, propPostfix1);

            edge.addProperty(APPEARS_IN, propPostfix2);

            edge.setId(Integer.toString(eId));
            mergedEdgeSet.addEdge(edge);
            eId = eId + 1;
        }

        return mergedEdgeSet;
    }

    private Metadata mergeMetadata(Metadata meta1, Metadata meta2, String propPostfix1, String propPostfix2) {

        Metadata mergedMetadata = new Metadata("Merged network",
                (Boolean.parseBoolean(meta1.isDirectedNetwork())
                || Boolean.parseBoolean(meta2.isDirectedNetwork())) ? "true" : "false",
                0);

        NodeProperties newNodeProps = new NodeProperties();
        EdgeProperties newEdgeProps = new EdgeProperties();

        Iterator<Property> it;
        Property tempProp;

        // Merge node propeties.
        newNodeProps.addProperty(new Property(APPEARS_IN, "string"));
        for (Object propObject1 : meta1.getNodeproperties()) {

            tempProp = ((Property) propObject1);
            newNodeProps.addProperty(new Property(
                    tempProp.getPropertyKey() + propPostfix1,
                    tempProp.getParsingType(),
                    (tempProp.getTitle() != null) ? tempProp.getTitle() + propPostfix1 : null,
                    tempProp.getDescription(),
                    tempProp.getSpecificType()
            )
            );
            newNodeProps.addProperty(new Property(
                    tempProp.getPropertyKey() + propPostfix2,
                    tempProp.getParsingType(),
                    (tempProp.getTitle() != null) ? tempProp.getTitle() + propPostfix2 : null,
                    tempProp.getDescription(),
                    tempProp.getSpecificType()
            )
            );

            // Check if property is present in second graph. Otherwise add.
            // TODO: Property object should overwrite equals and hashcode.
            it = meta2.getNodeproperties().iterator();
            while (it.hasNext() && !meta2.getNodeproperties().isEmpty()) {

                if (it.next().getPropertyKey().equals(tempProp.getPropertyKey())) {

                    it.remove();
                }
            }
        }
        // Add remaining node properties, if there are any that are only in graph 2.
        for (Object propObject2 : meta2.getNodeproperties()) {

            tempProp = ((Property) propObject2);

            newNodeProps.addProperty(new Property(
                    tempProp.getPropertyKey() + propPostfix1,
                    tempProp.getParsingType(),
                    (tempProp.getTitle() != null) ? tempProp.getTitle() + propPostfix1 : null,
                    tempProp.getDescription(),
                    tempProp.getSpecificType()
            )
            );
            newNodeProps.addProperty(new Property(
                    tempProp.getPropertyKey() + propPostfix2,
                    tempProp.getParsingType(),
                    (tempProp.getTitle() != null) ? tempProp.getTitle() + propPostfix2 : null,
                    tempProp.getDescription(),
                    tempProp.getSpecificType()
            )
            );
        }

        // Merge edge properties.
        newEdgeProps.addProperty(new Property(APPEARS_IN, "string"));
        for (Object propObject1 : meta1.getEdgeproperties()) {

            tempProp = ((Property) propObject1);
            newEdgeProps.addProperty(new Property(
                    tempProp.getPropertyKey() + propPostfix1,
                    tempProp.getParsingType(),
                    (tempProp.getTitle() != null) ? tempProp.getTitle() + propPostfix1 : null,
                    tempProp.getDescription(),
                    tempProp.getSpecificType()
            )
            );
            newEdgeProps.addProperty(new Property(
                    tempProp.getPropertyKey() + propPostfix2,
                    tempProp.getParsingType(),
                    (tempProp.getTitle() != null) ? tempProp.getTitle() + propPostfix2 : null,
                    tempProp.getDescription(),
                    tempProp.getSpecificType()
            )
            );

            // Check if property is present in second graph. Otherwise add.
            // TODO: Property object should overwrite equals and hashcode.
            it = meta2.getEdgeproperties().iterator();
            while (it.hasNext() && !meta2.getEdgeproperties().isEmpty()) {

                if (it.next().getPropertyKey().equals(tempProp.getPropertyKey())) {

                    it.remove();
                }
            }
        }
        // Add remaining node properties, if there are any.
        for (Object propObject2 : meta2.getEdgeproperties()) {

            tempProp = ((Property) propObject2);

            newEdgeProps.addProperty(new Property(
                    tempProp.getPropertyKey() + propPostfix1,
                    tempProp.getParsingType(),
                    (tempProp.getTitle() != null) ? tempProp.getTitle() + propPostfix1 : null,
                    tempProp.getDescription(),
                    tempProp.getSpecificType()
            )
            );
            newEdgeProps.addProperty(new Property(
                    tempProp.getPropertyKey() + propPostfix2,
                    tempProp.getParsingType(),
                    (tempProp.getTitle() != null) ? tempProp.getTitle() + propPostfix2 : null,
                    tempProp.getDescription(),
                    tempProp.getSpecificType()
            )
            );
        }

        mergedMetadata.setNodeproperties(newNodeProps);
        mergedMetadata.setEdgeproperties(newEdgeProps);

        return mergedMetadata;
    }

    private NodeSet mergeNodes(Vector<Node> nodes1, Vector<Node> nodes2, Set<Property> nodeProps, String propPostfix1, String propPostfix2) {

        int matchedIndex;
        //int nId = 0;
        Node matchedNode;
        NodeSet mergedNodeSet = new NodeSet();

        for (Node node : nodes1) {

            // Transform node property keys to KEY + propPostfix1
            this.transformNodeProperties(node, nodeProps, propPostfix1);

            matchedIndex = nodes2.indexOf(node);
            if (matchedIndex >= 0) {
                matchedNode = nodes2.remove(matchedIndex);

                // Transform matchedNode property keys to KEY + propPostfix2
                this.transformNodeProperties(matchedNode, nodeProps, propPostfix2);

                // Combine properties
                for (String propKey : matchedNode.getPropertyKeys()) {

                    node.addProperty(propKey, matchedNode.getProperty(propKey));
                }
                node.addProperty(APPEARS_IN, "both");
            } else {

                node.addProperty(APPEARS_IN, propPostfix1);
                // Add properties KEY + propPostfix2
                // NOTE: This only works since the original properties have been 
                // transformed before!
                this.transformNodeProperties(node, nodeProps, propPostfix2);
            }

            //node.setId(Integer.toString(nId));
            mergedNodeSet.addNode(node);
            //nId = nId + 1;
        }

        // Add remaining nodes.
        for (Node node : nodes2) {

            this.transformNodeProperties(node, nodeProps, propPostfix2);
            // NOTE: See previous note on transforming node properties twice.
            this.transformNodeProperties(node, nodeProps, propPostfix1);

            node.addProperty(APPEARS_IN, propPostfix2);

            //node.setId(Integer.toString(nId));
            mergedNodeSet.addNode(node);
            //nId = nId + 1;
        }

        return mergedNodeSet;
    }

    private void transformNodeProperties(Node node, Set<Property> props, String postfix) {

        Property tempProp;

        for (Object propObj : props) {

            tempProp = (Property) propObj;

            if (node.getProperty(tempProp.getPropertyKey()) != null) {

                node.addProperty(tempProp.getPropertyKey() + postfix,
                        node.getProperty(tempProp.getPropertyKey()));
                node.removeProperty(tempProp.getPropertyKey());
            } else if (tempProp.getParsingType().toLowerCase().equals("string")) {

                node.addProperty(tempProp.getPropertyKey() + postfix, "NA");
            } else {

                node.addProperty(tempProp.getPropertyKey() + postfix, Double.toString(Double.NaN));
            }
        }
    }
    private void transformEdgeProperties(Edge edge, Set<Property> props, String postfix) {

        Property tempProp;

        for (Object propObj : props) {

            tempProp = (Property) propObj;

            if (edge.getProperty(tempProp.getPropertyKey()) != null) {

                edge.addProperty(tempProp.getPropertyKey() + postfix,
                        edge.getProperty(tempProp.getPropertyKey()));
                edge.removeProperty(tempProp.getPropertyKey());
            } else if (tempProp.getParsingType().toLowerCase().equals("string")) {

                edge.addProperty(tempProp.getPropertyKey() + postfix, "NA");
            } else {

                edge.addProperty(tempProp.getPropertyKey() + postfix, Double.toString(Double.NaN));
            }
        }
    }

    private void preprocessNodesAndEdges(Vector<Node> nodes, Vector<Edge> edges) throws IllegalArgumentException {

        Map<String, String> idLabelMapping = new HashMap();

        // Test if nodes have label attribute. 
        if ((nodes.size() > 0) && (nodes.get(0).getProperty("label") == null)) {

            throw new IllegalArgumentException("Cannot merge graphs by label. "
                    + "Nodes do not have a label attribute.");
        }

        for (Node node : nodes) {

            idLabelMapping.put(node.getId(), node.getLabel());
            node.setId(node.getLabel());
        }
        for (Edge edge : edges) {

            edge.setSource(idLabelMapping.get(edge.getSource()));
            edge.setTarget(idLabelMapping.get(edge.getTarget()));
        }
    }
}
