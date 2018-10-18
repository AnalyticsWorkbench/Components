package info.collide.workbench.components.bicliquetracingvis;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;

import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.VisualizationAgent;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.EdgeSet;
import eu.sisob.api.visualization.format.graph.fields.Node;
import eu.sisob.api.visualization.format.graph.fields.NodeSet;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import java.util.Collections;

public class BicliqueTracingVisAgent extends VisualizationAgent {

    private static final String DEFAULT_COLOR = "#808080";
    private static final String VIS_DIR_NAME = "bt";
    private Vector<JSONFile> jsonNetworks;

    private HashMap<String, JSONArray> successorMap;
    private HashMap<String, JSONArray> predecessorMap;
    private HashMap<String, List<String>> memberMap;
    private NodeSet nodeset;
    private String path;
    private String similarityMeasure;
    private EdgeSet edgeset;

    public BicliqueTracingVisAgent(JsonObject coordinationMessage) {
        super(coordinationMessage);

        this.similarityMeasure = this.getFilterParameters().get(
                BicliqueTracingVisManager.SIMILARITY_MEASURE_PARAM).toString();

        String tmpPath = SISOBProperties.getSlideshowServerPath();
        if (tmpPath != null && !tmpPath.trim().isEmpty()) {
            path = tmpPath;
        }
    }

    @Override
    public void executeAgent(JsonObject dataMessage) {
        try {
            System.out.println("Biclique Tracing Visualization Agent Has Received Data...");

            System.out.println("Visualization Agent Has Received Data...");
            super.setWorkflowID(dataMessage.get("runid").getAsString());
            this.jsonNetworks = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
            this.jsonNetworks = isInputBASE64(this.jsonNetworks) ? transformToText(this.jsonNetworks) : this.jsonNetworks;

            System.out.println("Preparing Data for Biclique Tracing Visualization Visualization...");
            prepareData();
            this.saveVisualizationTechnique(jsonNetworks);//saveFiles();
            System.out.println("Biclique Tracing Visualization Generated!");
            uploadResults();
        } catch (Exception g) {
            super.indicateError("Error during agent excecution:" + g.getMessage());
            g.printStackTrace();
        }
    }

    /**
     * Writes result and visualization files to result repository
     *
     * @throws IOException
     * @throws IllegalContentTypeException
     */
    @Deprecated
    private void saveFiles() throws IOException, IllegalContentTypeException {
        String jsHomeString = SISOBProperties.getThirdPartyPath() + VIS_DIR_NAME + File.separator;

        
        this.path = this.getOutputPath();
        File outputDirectory = new File(path);
        File jsHome = new File(jsHomeString);
        BufferedWriter writer;
        outputDirectory.mkdirs();

        // write data files
        writer = new BufferedWriter(new FileWriter(new File(path + "data.json")));
        writer.write(jsonNetworks.get(0).getStringContent());
        writer.flush();
        writer.close();

        // write the js code in the output location
        FileUtils.copyDirectory(jsHome, outputDirectory);
    }

    /**
     * Checks if the network is a two-mode network
     *
     * @return true if network is a two-mode network
     */
    private boolean isTwoModeNetwork() {
        ArrayList<String> foundTypes = new ArrayList<String>();
        for (Node node : nodeset.getValues()) {
            String type = (String) node.get("type");
            if (!foundTypes.contains(type)) {
                foundTypes.add(type);
            }
        }
        if (foundTypes.size() != 2) {
            return false;
        }
        return true;
    }

    /**
     * Detects the predecessors and successors, calculates based on that the
     * node colors
     *
     * @throws Exception if an error occurs while parsing the network, or the
     * network is not a two mode network
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    private void prepareData() throws Exception {
        SGFParser parser = new SGFParser();
        parser.setNetwork(jsonNetworks.get(0));
        parser.parse();
        nodeset = parser.getParsedNodeSet();
        edgeset = parser.getParsedEdgeSet();
        successorMap = new HashMap<String, JSONArray>();
        predecessorMap = new HashMap<String, JSONArray>();
        memberMap = new HashMap<String, List<String>>();
        // check network mode
        if (!isTwoModeNetwork()) {
            throw new Exception("input data is not a two-mode network");
        }

        // find predecessors and successors
        JSONArray startNodes = new JSONArray();
        for (Node node : nodeset.getValues()) {
            String nodeType = (String) node.get("type");
            JSONArray predecessors = new JSONArray();
            JSONArray successors = new JSONArray();
            for (Object edgeObject : edgeset) {
                Edge edge = (Edge) edgeObject;
                if (edge.getTarget().equals(node.getId())) {
                    Node predecessorNode = getNodeById(nodeset, edge.getSource());
                    if (nodeType.equals(predecessorNode.get("type"))) {
                        predecessors.add(predecessorNode);
                    }
                } else if (edge.getSource().equals(node.getId())) {
                    Node successorNode = getNodeById(nodeset, edge.getTarget());
                    if (nodeType.equals(successorNode.get("type"))) {
                        successors.add(successorNode);
                    }
                }
            }
            if (successors.size() > 0) {
                startNodes.add(node);
            }
            String color = getSingleNodeColor();
            if (color != null) {
                node.put("color", color);
            }

            String membersString = (String) node.get("members").toString();
            List<String> members = Arrays.asList(membersString.substring(1,
                    membersString.length() - 1).split(","));
            memberMap.put(node.getId(), members);
            successorMap.put(node.getId(), successors);
            predecessorMap.put(node.getId(), predecessors);
        }
        // calculate colors
        calculateStartNodeColors(startNodes);
        colorNodes();
        //jsonNetworks.get(0).setData(parser.encode());
        jsonNetworks.set(0, parser.getNetwork());
    }

    /**
     * Calculates the colors of the nodes
     */
    private void colorNodes() {
        Collections.sort(nodeset.getValues(), new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                int t1 = Integer.parseInt((String) o1.get("timestep"));
                int t2 = Integer.parseInt((String) o2.get("timestep"));
                return t1 - t2;
            }
        });
//        nodeset.getValues().sort(new Comparator<Node>() {
//            @Override
//            public int compare(Node o1, Node o2) {
//                int t1 = Integer.parseInt((String) o1.get("timestep"));
//                int t2 = Integer.parseInt((String) o2.get("timestep"));
//                return t1 - t2;
//            }
//        });
        for (Node node : nodeset.getValues()) {
            JSONArray successors = successorMap.get(node.getId());
            JSONArray predecessors = predecessorMap.get(node.getId());

            if (predecessors.size() > 1) {
                calculateMergedColor(node);
            }

            String nodeType = (String) node.get("type");
            for (Object obj : successors) {
                Node sucNode = (Node) obj;
                if (nodeType.equals((String) sucNode.get("type"))) {
                    calculateInterpolatedColor(node, sucNode);
                }
            }
        }
    }

    /**
     * Calculates the mean color based on the predecessors of the given node
     *
     * @param node the node, to calculate the color for. Puts the calculated
     * color in the "color"-field of the node-object
     */
    @SuppressWarnings("unchecked")
    private void calculateMergedColor(Node node) {
        ArrayList<Integer> hValues = new ArrayList<Integer>();
        float hX = 0.0f;
        float hY = 0.0f;
        for (Object obj : predecessorMap.get(node.getId())) {
            Node n = (Node) obj;
            HSLColor hslColor = new HSLColor(Color.decode((String) n.get("color")));
            hValues.add((int) hslColor.getHue());
        }
        for (Integer h : hValues) {
            hX += Math.cos((h * Math.PI) / 180);
            hY += Math.sin((h * Math.PI) / 180);
        }
        double meanAngle = Math.atan2(hY / hValues.size(), hX / hValues.size());
        HSLColor nodeColor = new HSLColor(Color.decode((String) node.get("color")));
        HSLColor resultColor = new HSLColor(Math.round((meanAngle * 180) / Math.PI),
                nodeColor.getSaturation(), nodeColor.getLuminance());
        node.put("color", colorToHexString(resultColor.getAsColorObject()));
    }

    /**
     * Calculates the color of nodeToColor based on the similarity to baseNode
     *
     * @param baseNode the node to calculate the similarity to (typically the
     * predecessor of the nodeToColor)
     * @param nodeToColor the node, to calculate the color for. Puts the
     * calculated color in the "color"-field of the node-object
     */
    @SuppressWarnings("unchecked")
    private void calculateInterpolatedColor(Node baseNode, Node nodeToColor) {
        HSLColor hslColor = new HSLColor(Color.decode((String) baseNode.get("color")));
        int degree1 = (int) (hslColor.getHue() + 360);
        int degree2 = (int) (hslColor.getHue() + 540);
        int colorDiff = Math.abs(degree1 - degree2);
        int resultColor = 0;

        double sim = 0.0d;
        if (similarityMeasure.equals("Jaccard")) {
            sim = 1.0d - calculateJaccardSimilarity(memberMap.get(baseNode.getId()),
                    memberMap.get(nodeToColor.getId()));
        } else if (similarityMeasure.equals("Inclusion")) {
            sim = 1.0d - calculateInclusionSimilarity(memberMap.get(baseNode.getId()),
                    memberMap.get(nodeToColor.getId()));
        }
        if (degree1 < degree2) {
            resultColor = (int) (degree1 + sim * colorDiff);
        } else {
            resultColor = (int) (degree1 - sim * colorDiff);
        }
        HSLColor hsl = new HSLColor(resultColor, 50, 50);
        nodeToColor.put("color", colorToHexString(hsl.getAsColorObject()));
    }

    /**
     * Calculates the similarity of list1 and list2 based on the
     * jaccard-coefficient
     *
     * @param list1
     * @param list2
     * @return the jaccard-coefficient of the two given lists
     */
    private double calculateJaccardSimilarity(List<String> list1, List<String> list2) {
        if (list1.size() == 0 || list2.size() == 0) {
            return 0.0;
        }
        HashSet<String> union = new HashSet<String>(list1);
        union.addAll(list2);

        HashSet<String> intersection = new HashSet<String>(list1);
        intersection.retainAll(list2);

        return (double) intersection.size() / (double) union.size();
    }

    /**
     * Calculates the inclusion-similarity of list1 and list2
     *
     * @param list1
     * @param list2
     * @return the inclusion similarity of list1 and list2
     */
    private double calculateInclusionSimilarity(List<String> list1, List<String> list2) {
        if (list1.size() == 0 || list2.size() == 0) {
            return 0.0;
        }
        HashSet<String> intersection = new HashSet<String>(list1);
        intersection.retainAll(list2);
        return (double) intersection.size() / (double) Math.min(list1.size(), list2.size());
    }

    /**
     * Calculates random colors for each node and writes them in the
     * "color"-field of each node.
     *
     * @param startNodes An {@link JSONArray} with the node to color
     */
    @SuppressWarnings("unchecked")
    private void calculateStartNodeColors(JSONArray startNodes) {
        Random random = new Random();
        for (Object obj : startNodes) {
            Node node = (Node) obj;
            int randomColor = random.nextInt(361);
            HSLColor hslColor = new HSLColor(randomColor, 50, 50);
            node.put("color", colorToHexString(hslColor.getAsColorObject()));
        }
    }

    /**
     * Calculates a six-digit rgb-hex-string of the given {@link Color} c.
     *
     * @param c The color to get the hex string from
     * @return A six-digit rgb-hex-string of the {@link Color} c witha leading #
     * @throws NullPointerException if the parameter c is null
     */
    private String colorToHexString(Color c) throws NullPointerException {
        String hexColour = Integer.toHexString(c.getRGB() & 0xffffff);
        if (hexColour.length() < 6) {
            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
        }
        return "#" + hexColour;
    }

    /**
     *
     * @return the default color for nodes
     */
    private String getSingleNodeColor() {
        return DEFAULT_COLOR;
    }

    /**
     * Finds the node with the given id in the given nodeset
     *
     * @param nodes a nodeset to search in
     * @param id the id to search for
     * @return a {@link Node} object with the given id or null if such a node
     * does not exist in the given nodeset
     */
    private Node getNodeById(NodeSet nodes, String id) {
        for (Node node : nodes.getValues()) {

            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }

    @Override
    protected void uploadResults() {
        super.indicateDone();
    }

    @Override
    public String getZipPath() {

        return "info/collide/workbench/components/bicliquetracingvis/technique.zip";
    }

}
