/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.workbench.components.timeslicer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.collide.sqlspaces.commons.Tuple;
import info.collide.sqlspaces.commons.TupleSpaceException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;

/**
 *
 * @author hecking
 */
public class TimeSlicerAgent extends Agent {

    private String agentName;
    private int offset;
    private int end;
    private int windowSize;
    private Vector<JSONFile> slices;

    private boolean includeIsolatedNodes = false;

    public TimeSlicerAgent(JsonObject commandMsg) {
        
        super(commandMsg);

        this.offset = Integer.parseInt(this.getFilterParameters().get(TimeSlicerAgentManager.BEGIN).toString());
        this.end = Integer.parseInt(this.getFilterParameters().get(TimeSlicerAgentManager.END).toString());
        this.windowSize = Integer.parseInt(this.getFilterParameters().get(TimeSlicerAgentManager.WINDOW).toString());
        this.includeIsolatedNodes = Boolean.parseBoolean(this.getFilterParameters().get(TimeSlicerAgentManager.INCLUDE_ISOLATES).toString());

        System.out.println("Time slicer: agent created!!!");
    }

    @Override
    public void executeAgent(JsonObject dataMessage) {
        
        String input = new Gson().toJson(dataMessage.get("payload"));
        Vector<JSONFile> files = JSONFile.restoreJSONFileSet(input);
        try {
            this.getTimeslices(files.firstElement());
            this.uploadResults();
        } catch (Exception ex) {
            indicateError("Problems extracting time slices.", ex);
        }
    }

    @Override
    public void executeAgent(List<JsonObject> dataMessages) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    protected void uploadResults() {

        storeData(this.getWorkflowID(), this.getAgentInstanceID() + "." + TimeSlicerAgentManager.OUTLET, JSONFile.collectionToString(this.slices));
        this.indicateDone();
    }

    private void getTimeslices(JSONFile file) {

    	// this.offset: first time point to include
        // this.end: last time point to include
        // this.windowsize: number of time points to put into one graph
        int numGraphs = (int) ((this.end - this.offset) / this.windowSize);

//        System.out.println("Graphs: " + numGraphs + "\nEnde: "+this.end + "\nOffset: "+this.offset + "\nWindow: "+this.windowSize);
        this.slices = new Vector<JSONFile>();

        JSONFile outputFile;
        NetworkParser parser = new SGFParser();
        NodeSet nodes = null;
        EdgeSet edges = null;

        List<Vector<Node>> timeNodes = new ArrayList<Vector<Node>>(numGraphs);
        List<Vector<Edge>> timeEdges = new ArrayList<Vector<Edge>>(numGraphs);
        ArrayList<HashSet<String>> timeNodeIds = new ArrayList<HashSet<String>>(numGraphs);
        ArrayList<HashSet<String>> timeEdgeIds = new ArrayList<HashSet<String>>(numGraphs);

        Node newNode = null;
        Edge newEdge = null;
        int bucket = 0;
        int time = 0;

        for (int i = 0; i < numGraphs; i++) {
            timeNodes.add(new Vector<Node>());
            timeEdges.add(new Vector<Edge>());
            timeNodeIds.add(new HashSet<String>());
            timeEdgeIds.add(new HashSet<String>());
        }

        try {
            parser.setNetwork(file);
            parser.parse();

            nodes = parser.getParsedNodeSet();
            edges = parser.getParsedEdgeSet();
            //for all edges
            for (Edge e : edges.getValues()) {
                //for all time points

                for (Object o : e.getTimeAppearance()) {

                    time = Integer.parseInt(o.toString());
                    assert this.offset > 0 : "minimal offset is 1";
                    //if in defined region: select a graph/window(bucket) for the edge
                    if ((time >= this.offset && time <= this.end)) {
                        bucket = (int) ((time - this.offset) / this.windowSize);
//                        System.out.println(time + " -> " + bucket);
                        // if out of last  (i.e. window = 3, range = 7, time = 6 or 7 => bucket = 2 but Graphs = {0,1} => oob)
                        // so the last bucket is in worst case (window - 1) times bigger, maybe change bucket to float to have more precise windows/buckets...
                        if (bucket > numGraphs - 1) {
                            bucket = numGraphs - 1;
                        }

                        // if actual edge is in window(bucket)
                        if (!timeEdgeIds.get(bucket).contains(e.getId())) {
                            //create a new one in the corresponding graph
                            if (e.getLabel() != null) {

                                newEdge = new Edge(e.getId(), e.getSource(), e.getTarget());
                                newEdge.setLabel(e.getLabel());
                            } else {

                                newEdge = new Edge(e.getId(), e.getSource(), e.getTarget());

                            }
                            for (String key : e.getPropertyKeys()) {

                                newEdge.addProperty(key, e.getProperty(key));
                            }

                            newEdge.addProperty("weight", "1");
                            //  newEdge.setEdgeAsJSON(e.getEdgeAsJSON());
                            newEdge.addTimeAppearance(Integer.toString(time));
                            timeEdges.get(bucket).add(newEdge);
                            timeEdgeIds.get(bucket).add(e.getId());
                            timeNodeIds.get(bucket).add(e.getSource());
                            timeNodeIds.get(bucket).add(e.getTarget());
                        }
                    }
                }
            }

            for (Node n : nodes.getValues()) {
                for (int i = 0; i < timeNodeIds.size(); i++) {

                    // isolated nodes are omitted;
                    if (timeNodeIds.get(i).contains(n.getId()) && !includeIsolatedNodes) {

                        newNode = new Node(n.getId(), n.getLabel());
                        for (String key : n.getPropertyKeys()) {

                            newNode.addProperty(key, n.getProperty(key));
                        }
                        //newNode.setNodeAsJSON(n.getNodeAsJSON());
                        newNode.addTimeAppearance(Integer.toString(time));

                        timeNodes.get(i).add(newNode);
                    }
                    // all nodes are included
                    if (includeIsolatedNodes) {
                        newNode = new Node(n.getId(), n.getLabel());
                        for (String key : n.getPropertyKeys()) {

                            newNode.addProperty(key, n.getProperty(key));
                        }
                        //newNode.setNodeAsJSON(n.getNodeAsJSON());
                        newNode.addTimeAppearance(Integer.toString(time));

                        timeNodes.get(i).add(newNode);
                    }
                }
            }

        } catch (IllegalContentTypeException ex) {

            this.indicateError(this.agentName + ": Cannot read file content. Maybe it is a binary file.");
            Logger.getLogger(TimeSlicerAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < numGraphs; i = i + 1) {

            outputFile = new JSONFile(i + "_" + file.getFileName(), "SGF",
                    this.getNormalizedNetwork(timeNodes.get(i), timeEdges.get(i), parser.getParsedMetadata(), i, parser.getParsedMetadata().getNetworkType()), JSONFile.TEXT);
            this.slices.add(outputFile);
        }
    }

//    private void getTimeslices(JSONFile file) {
//        
//        int numGraphs = (((this.end - this.offset) % this.windowSize) == 0) ? 
//                ((this.end - this.offset) / this.windowSize) 
//                    : (Math.round(((this.end - this.offset) / this.windowSize)) + 1);
//        
//        this.slices = new Vector<JSONFile>();
//        
//        JSONFile outputFile;
//        NetworkParser parser = new SGFParser();
//        NodeSet nodes = null;
//        EdgeSet edges = null;
//        
//        List<Vector<Node>> timeNodes = new ArrayList<Vector<Node>>(numGraphs);
//        List<Vector<Edge>> timeEdges = new ArrayList<Vector<Edge>>(numGraphs);
//        List<Vector<String>> timeNodeIds = new ArrayList<Vector<String>>(numGraphs);
//        List<Vector<String>> timeEdgeIds = new ArrayList<Vector<String>>(numGraphs);
//        Node newNode = null;
//        Edge newEdge = null;
//        int bucket = 0;
//        int time = 0;
//        
//        for (int i = 0; i < numGraphs; i = i + 1) {
//            
//            timeNodes.add(new Vector<Node>());
//            timeEdges.add(new Vector<Edge>());
//            timeNodeIds.add(new Vector<String>());
//            timeEdgeIds.add(new Vector<String>());
//        }
//        
//        try {    
//            parser.setNetwork(file);
//            parser.parse();
//            
//            nodes = parser.getNodeset();
//            edges = parser.getEdgeset();
//            
//            for (Node n : nodes.getNodeSet()) {
//                
//                for (Object o : n.getTimeAppearance()) {
//                    
//                    time = Integer.parseInt(o.toString());
//                    
//                    assert this.offset > 0 : "minimal offset is 1";
//                    if ((time >= this.offset && time <= this.end)) {
//                    
//                        bucket = ((time - this.offset + 1) % this.windowSize) == 0 ? 
//                                ((time - this.offset + 1) / this.windowSize) - 1 :
//                                    (int)((time - this.offset + 1) / this.windowSize);
//                        
//                        if (!timeNodeIds.get(bucket).contains(n.getId())) {
//                            newNode = new Node(n.getId(), n.getLabel());
//                            newNode.setNodeAsJSON(n.getNodeAsJSON());
//                            newNode.addTimeAppearance(Integer.toString(time));
//
//                            timeNodes.get(bucket).add(newNode);
//                            timeNodeIds.get(bucket).add(n.getId());
//                        }
//                    }
//                }   
//            }
//            for (Edge e : edges.getEdgeSet()) {
//                
//                for (Object o : e.getTimeAppearance()) {
//                    
//                    time = Integer.parseInt(o.toString());
//                    
//                    assert this.offset > 0 : "minimal offset is 1";
//                    if ((time >= this.offset && time <= this.end)) {
//                    
//                        bucket = ((time - this.offset + 1) % this.windowSize) == 0 ? 
//                                ((time - this.offset + 1) / this.windowSize) - 1 :
//                                    (int)((time - this.offset + 1) / this.windowSize);
//                        
//                        
//                        if (timeEdges.get(bucket) != null) {
//                            
//                            timeEdges.add(new Vector<Edge>());  
//                        }
//                        
//                        if (!timeEdgeIds.get(bucket).contains(e.getId())) {
//                            newEdge = new Edge(e.getId(), e.getLabel(), e.getSource(), e.getTarget());
//                            newEdge.setEdgeAsJSON(e.getEdgeAsJSON());
//                            newEdge.addTimeAppearance(Integer.toString(time));
//
//                            timeEdges.get(bucket).add(newEdge);
//                            timeEdgeIds.get(bucket).add(e.getId());
//                        }
//                    }
//                }   
//            }
//            
//        } catch (IllegalContentTypeException ex) {
//            
//            this.indicateError(this.agentName + ": Cannot read file content. Maybe it is a binary file.");
//            Logger.getLogger(TimeSlicerAgent.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        for (int i = 0; i < numGraphs; i = i + 1) {
//            
//            outputFile = new JSONFile(i + "_" + file.getFileName(), "SGF", 
//                    this.getNormalizedNetwork(timeNodes.get(i), timeEdges.get(i), parser.getMetadata(), i), JSONFile.TEXT);
//            this.slices.add(outputFile);
//        }
//    }
    private String getNormalizedNetwork(Vector<Node> nodes, Vector<Edge> edges, Metadata metadata, int timestamp, String type) {

        NetworkParser parser = new SGFParser();

        // TODO: normalization: node ids from 1 to n.
        String nId;
        for (int i = 0; i < nodes.size(); i = i + 1) {

            nId = nodes.get(i).getId();

            nodes.get(i).setId(Integer.toString(i + 1));

            for (int j = 0; j < edges.size(); j = j + 1) {

                if (edges.get(j).getSource().equals(nId)) {

                    edges.get(j).setSource(Integer.toString(i + 1));

                }
                if (edges.get(j).getTarget().equals(nId)) {

                    edges.get(j).setTarget(Integer.toString(i + 1));

                }
            }
        }

        NodeSet ns = new NodeSet();
        ns.setValues(nodes);
        EdgeSet es = new EdgeSet();
        es.setValues(edges);
        
        parser.updateNodeSet(ns);
        parser.updateEdgeSet(es);
        parser.updateMetadata(metadata);
        
        return parser.encode();
    }

    public Vector<JSONFile> testAgent(JSONFile file) {

        this.getTimeslices(file);
        return this.slices;
    }
}
