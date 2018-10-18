/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.api.parser.gml;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import eu.sisob.api.parser.gml.antlrfiles.GMLBaseListener;
import eu.sisob.api.parser.gml.antlrfiles.GMLParser;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.EdgeProperties;
import eu.sisob.api.visualization.format.metadata.fields.NodeProperties;
import eu.sisob.api.visualization.format.metadata.fields.Property;

/**
 *
 * @author hecking
 */
public class GMLParseTreeListener extends GMLBaseListener{
    
    // Known keys
    private static final String NODE = "node";
    private static final String EDGE = "edge";
    private static final String DIRECTED = "directed";
    private static final String GRAPHICS = "graphics";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String ID = "id";
    private static final String LABEL = "label";
    private static final String TIME = "time";
    private static final String TYPE = "type";
    
    private NodeSet nodes;
    private EdgeSet edges;
    private Metadata metadata;
    private NodeProperties nodeProperties;
    private EdgeProperties edgeProperties;
    private Node currentNode;
    private Edge currentEdge;
    
    private boolean parseNode;
    private boolean parseEdge;
    
    private int parsedNodes;
    private int parsedEdges;
    
    private StringTokenizer tokenizer;
    
    
    public GMLParseTreeListener() {
        
        this.nodes = new NodeSet();
        this.edges = new EdgeSet();
        this.metadata = new Metadata();
        this.parseNode = false;
        this.parseEdge = false;
        this.parsedNodes = 0;
        this.nodeProperties = new NodeProperties();
        this.edgeProperties = new EdgeProperties();
    }

    @Override 
    public void enterKeyvalue(GMLParser.KeyvalueContext ctx) { 

        if (ctx.KEY().getText().equals(NODE)) {
            
            this.parseNode = true;
            this.parseEdge = false;
            this.currentNode = new Node();
        }
        if (ctx.KEY().getText().equals(EDGE)) {
            
            this.parseEdge = true;
            this.parseNode = false;
            this.currentEdge = new Edge();
        }
        if (!this.parseNode && !this.parseEdge) {
            
            this.parseNode = false;
            this.parseEdge = false;
        }
        
        if (this.parseNode) {
            
            // check for known keys
            if (ctx.KEY().getText().equals(ID)) {
                
                this.currentNode.setId(ctx.value().getText());
                
            } else if (ctx.KEY().getText().equals(LABEL)) {
                
                if (ctx.value().STRING() != null) {
                    
                    // skip ""
                    this.currentNode.setLabel(ctx.value().STRING().getText()
                            .substring(1, (ctx.value().STRING().getText().length() - 1)));
                } else {
                    
                    this.currentNode.setLabel(ctx.value().getText());
                    
                } 
            } else if (ctx.KEY().getText().equals(TIME)) {
                
                // skip ""
                tokenizer = new StringTokenizer(ctx.value().STRING().getText()
                            .substring(1, (ctx.value().STRING().getText().length() - 1)), ",");
                
                while (tokenizer.hasMoreTokens()) {
                    
                    this.currentNode.addTimeAppearance(tokenizer.nextToken());
                }
                
            } else if (ctx.KEY().getText().equals(GRAPHICS)) {
                    
                //////// put graphics attributes into node.  
                        
                        this.currentNode.addProperty(GRAPHICS, ctx.value().getText());
                ///////
                
                
            } else if (!ctx.KEY().getText().equals(NODE)) {
                
                if (ctx.value().STRING() != null) {
                    
                    // skip ""
                    this.currentNode.addProperty(ctx.KEY().getText(), ctx.value().STRING().getText()
                            .substring(1, (ctx.value().STRING().getText().length() - 1)));
                    if (!this.nodeProperties.containsPropertyByKey(ctx.KEY().getText())) {
                    
                        this.nodeProperties.addProperty(new Property(ctx.KEY().getText(), "string"));
                    }
                } else {
                    
                    this.currentNode.addProperty(ctx.KEY().getText(), ctx.value().getText());
                    if (!this.nodeProperties.containsPropertyByKey(ctx.KEY().getText())) {
                    
                        // Type has to be handled as string
                        if (ctx.KEY().getText().equals(TYPE)) {
                            this.nodeProperties.addProperty(new Property(ctx.KEY().getText(), "string"));
                        }
                        this.nodeProperties.addProperty(new Property(ctx.KEY().getText(), "double"));
                    }
                }
            }
        } else if (this.parseEdge) {
            
            if (ctx.KEY().getText().equals(SOURCE)) {
                
                this.currentEdge.setSource(ctx.value().getText());
            } else if (ctx.KEY().getText().equals(TARGET)) {
                
                this.currentEdge.setTarget(ctx.value().getText());
              
            } else if (ctx.KEY().getText().equals(TIME)) {
                
                // skip ""
                tokenizer = new StringTokenizer(ctx.value().STRING().getText()
                            .substring(1, (ctx.value().STRING().getText().length() - 1)), ",");
                
                while (tokenizer.hasMoreTokens()) {
                    
                    this.currentEdge.addTimeAppearance(tokenizer.nextToken());
                }
                
            } else if (!ctx.KEY().getText().equals(EDGE)) {
                
                if (ctx.value().STRING() != null) {
                    
                    // skip ""
                    this.currentEdge.addProperty(ctx.KEY().getText(), ctx.value().STRING().getText()
                            .substring(1, (ctx.value().STRING().getText().length() - 1)));
                    
                    if (!this.edgeProperties.containsPropertyByKey(ctx.KEY().getText())) {
                    
                        this.edgeProperties.addProperty(new Property(ctx.KEY().getText(), "string"));
                    }
                    
                } else {
                    
                    if (!ctx.KEY().getText().equals(ID)) {
                    
                        this.currentEdge.addProperty(ctx.KEY().getText(), ctx.value().getText());
                    
                        if (!this.edgeProperties.containsPropertyByKey(ctx.KEY().getText())) {

                            this.edgeProperties.addProperty(new Property(ctx.KEY().getText(), "double"));
                        }
                    }
                }
            }
        } else {
            
            //parse metadata
            if (ctx.KEY().getText().equals(DIRECTED)) {
                System.out.println(ctx.KEY().getText());
                if (ctx.value().getText().equals("1")) {
                
                    this.metadata.setDirectedNetwork("true");
                } else {
                    
                    this.metadata.setDirectedNetwork("false");
                }
            } else {
                
                if (ctx.value().STRING() != null) {
                
                    this.metadata.addNetworkInfo(ctx.KEY().getText(), ctx.value().STRING().getText()
                            .substring(1, (ctx.value().STRING().getText().length() - 1)));
                } else {
                    
                    this.metadata.addNetworkInfo(ctx.KEY().getText(), ctx.value().getText());
                }
            }
        }
    }

    @Override 
    public void exitKeyvalue(GMLParser.KeyvalueContext ctx) { 
    
        if (ctx.KEY().getText().equals(NODE)) {
            this.parseNode = false;
            this.parsedNodes++;
            if (this.currentNode.getId() == null) {
                
                this.currentNode.setId(Integer.toString(this.parsedNodes));
            }
            if (this.currentNode.getLabel() == null) {
                
                this.currentNode.setLabel(Integer.toString(this.parsedNodes));
            }
            
            this.nodes.addNode(this.currentNode);
        }
        if (ctx.KEY().getText().equals(EDGE)) {
            
            this.parseEdge = false;
            this.parsedEdges++;
            if (this.currentEdge.getId() == null) {
                
                this.currentEdge.setId(Integer.toString(this.parsedEdges));
            }
            this.edges.addEdge(this.currentEdge);
        }
    }
    
    public NodeSet getNodeSet() {
        
        return this.nodes;
    }
    
    public EdgeSet getEdgeSet() {
        
        return this.edges;
    }
    
    public Metadata getMetadata() {
    	
    	this.metadata.setNodeproperties(this.nodeProperties);
        this.metadata.setEdgeproperties(this.edgeProperties);
    	
        return this.metadata;
    }
}
