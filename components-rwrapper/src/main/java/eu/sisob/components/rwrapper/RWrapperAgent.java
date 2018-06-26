package eu.sisob.components.rwrapper;

import com.github.rcaller.exception.ExecutionException;
import com.github.rcaller.rStuff.RCaller;
import com.github.rcaller.rStuff.RCode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.sisob.api.parser.csv.CSVParser;
import eu.sisob.api.parser.gml.GMLNetworkParser;
import eu.sisob.api.parser.sisob.SDTParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.EdgeProperties;
import eu.sisob.api.visualization.format.metadata.fields.NodeProperties;
import eu.sisob.api.visualization.format.metadata.fields.Properties;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.AgentProperties;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.graph.parser.FormatFactory;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import eu.sisob.components.framework.util.ClassLoaderUtility;
import java.util.Vector;

/**
 * @author hecking
 */
public class RWrapperAgent extends Agent {

    protected String RScriptExecutable;

    // determines the kind of analysis
    protected String instanceId;
    protected String runId;
    protected String agentName;
    protected boolean isGraphData;
    protected String networkType;

    protected InputStream scriptStream;

    protected String workspaceUrl;
    protected JSONArray resultFiles;
    protected JSONArray decoratedFiles;
    protected List<Property> tablePropertyMetadata;
    protected List<Property> nodePropertyMetadata;
    protected List<Property> edgePropertyMetadata;
    protected List<String> filenames;
    protected String input;

    protected String rawOutlet = ".out_1";
    protected String decoratedOutlet = ".out_2";

    private HashMap<String, Metadata> metadataMapping;

    protected boolean consistentFormat;

    public RWrapperAgent(JsonObject commandMsg) {

        super(commandMsg);

        System.out.println("CommandMsg:");
        System.out.println(commandMsg);
        agentName = getClass().getName();
        int pIndex = agentName.lastIndexOf(".");
        if (pIndex > -1) {
            agentName = agentName.substring(pIndex + 1);
        }

        System.out.println(agentName + ": " + commandMsg.get("agentid").getAsString());

        this.RScriptExecutable = SISOBProperties.getProperty("rwrapper.executable");

        JsonObject dataStructure = new JsonObject();

        dataStructure.addProperty("runid", commandMsg.get("runid").getAsString());
        dataStructure.addProperty("pipes", commandMsg.get("pipes").getAsString());
        setDataStructure(dataStructure);

        // Check if this is the "original" or one of the subclasses
        // which usually do not expect a second parameter indicating what kind
        // of data will be the input
        if (agentName.equals("RWrapperAgent")) {

            String scriptName = (String) getFilterParameters().get(RWrapperManager.PARAMETER_SCRIPT);

            this.scriptStream = ClassLoaderUtility.getClassLoader().getResourceAsStream(scriptName);

            if (this.scriptStream == null) {
                // custom R-scripts

                JsonElement parameters = commandMsg.get("parameters");
                String parameterString = parameters.getAsString();
                System.out.println(parameterString);
                JsonObject parameterJson = new Gson().fromJson(parameterString, JsonObject.class);
                JsonElement script = parameterJson.get("script");
                String scriptString = script.getAsString();

                JsonObject scriptJson = new Gson().fromJson(scriptString, JsonObject.class);
                System.out.println(scriptString);

                String scriptId = scriptJson.get("saveid").getAsString();
                String user = scriptJson.get("creator").getAsString();
                String rObject = scriptJson.get("robject").getAsString().replace("\\\\", "");
                JsonArray fileArray = new Gson().fromJson(rObject, JsonArray.class);
                JsonObject file = fileArray.get(0).getAsJsonObject();
                String fileName = file.get("filename").getAsString();
                // find R-script in repository

                try {
                    String sep = RWrapperManager.SEP;
                    String fullFileName = user + sep + scriptId + sep + fileName;
                    String path = SISOBProperties.getProperty("rwrapper.scriptrepository") + File.separator
                            + fullFileName;
                    this.scriptStream = new FileInputStream(path);
                } catch (FileNotFoundException ex) {
                    logger.warning("Cannot find script");
                    Logger.getLogger(RWrapperAgent.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            this.isGraphData = true;
        }

        this.instanceId = commandMsg.get("instanceid").getAsString();
        this.runId = commandMsg.get("runid").getAsString();

        File workspace = new File(this.runId + "_" + this.instanceId + "_workspace");
        workspace.mkdir();
        this.workspaceUrl = workspace.getAbsolutePath();
        this.tablePropertyMetadata = new ArrayList<Property>();
        this.nodePropertyMetadata = new ArrayList<Property>();
        this.edgePropertyMetadata = new ArrayList<Property>();
        this.filenames = new ArrayList<String>();
        logger.fine(agentName + ": agent created!!!");

    }

    @Override
    public void executeAgent(JsonObject dataMessage) {
        logger.fine(agentName + ": agent");

        this.input = new Gson().toJson(dataMessage.get("payload"));
        if (this.prepareAgentWorkspace(this.input)) {

            if (this.executeRScript(null)) {
                this.uploadResults();
            }
        }
    }

    @Override
    public void executeAgent(List<JsonObject> dataMessages) {

    }

    protected void uploadData(JSONArray fileSet, String outlet) {

        if (fileSet.size() > 0) {
            storeData(this.getWorkflowID(), this.getAgentInstanceID() + outlet, fileSet.toJSONString());
        }
    }

    @Override
    protected void uploadResults() {
        logger.fine(agentName + ": uploading results");

        if (this.resultFiles.size() == 0) {

            // this.deleteWorkspace(new File(this.workspaceUrl));
            this.indicateError("No result data available.");
        } else {

            this.uploadData(this.resultFiles, this.rawOutlet);

            if ((this.decoratedFiles == null) || (this.decoratedFiles.size() == 0)) {

                this.uploadData((JSONArray) JSONValue.parse(this.input), this.decoratedOutlet);
            } else {

                this.uploadData(this.decoratedFiles, this.decoratedOutlet);
            }
            this.deleteWorkspace(new File(this.workspaceUrl));
            logger.fine(agentName + ": kill agent");

            this.indicateDone();
        }

    }

    protected void deleteWorkspace(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    this.deleteWorkspace(files[i]); // delete subdirectories
                } else {
                    files[i].delete(); // delete file
                }
            }
            dir.delete(); // delete empty workspace
        }
    }

    /**
     *
     * @param uploadedData
     * @param keepIds This parameter only matters if the ids of the nodes or
     * edges are not numerical. If true, the ids of the nodes and edges are
     * saved before transforming into gml, and restored after the R script has
     * finished. If false, all nodes and edges get new ids
     * @param extendedMetadata if true, keeps String attributes in metadata
     * @return
     */
    protected boolean prepareAgentWorkspace(String uploadedData, boolean keepIds, boolean extendedMetadata) {
        boolean success = true;
        Vector<JSONFile> files = JSONFile.restoreJSONFileSet(uploadedData);
        FormatFactory transformer = new FormatFactory(files);

        String filename = "";

        // check if all files have the same format (all sgf or all stf)
        consistentFormat = true;
        ArrayList<String> types = new ArrayList<String>();
        for (JSONFile jsonFile : files) {
            types.add(jsonFile.getFileType());
        }
        String firstType = types.get(0);
        for (String type : types) {
            if (!type.equalsIgnoreCase(firstType)) {
                consistentFormat = false;
            }
        }

        if (consistentFormat) {
            // It is assumed that either all files are graphs or all files are data
            // tables.
            String filetype = files.isEmpty() ? "" : files.get(0).getFileType();
            try {
                if (filetype.toLowerCase().equals("sgf") || filetype.toLowerCase().equals(".sgf")) {

                    SGFParser input = new SGFParser();
                    this.isGraphData = true;

                    // TODO save metadata
                    saveMetadata(files, input);

                    transformer.convertNetworkFormats(input, new GMLNetworkParser(keepIds, extendedMetadata));
                    files = transformer.getNetworks();

                    if (input.getParsedMetadata() != null) {
                        if (input.getParsedMetadata().getNetworkType() != null) {

                            this.networkType = input.getParsedMetadata().getNetworkType();
                        }
                        NodeProperties nodeProperties = input.getParsedMetadata().getNodeproperties();
                        if (nodeProperties != null) {
                            for (Object obj : nodeProperties) {
                                //nodeProperties JSONObject are in the form {"parsingtype":"string","text":["string"]}
                                //for parsingtype we need to get the value for the other we need the key as title for our property
                                System.out.println(obj);
                                JSONObject nodePropertyJSON = (JSONObject) obj;
                                String parsingType = nodePropertyJSON.get(Property.KEY_PARSINGTYPE).toString();
                                String property = "NotFound";
                                for (Object key : nodePropertyJSON.keySet()) {
                                    String keyString = key.toString();
                                    if (!keyString.equals(Property.KEY_PARSINGTYPE)) {
                                        property = keyString;
                                    }
                                }
                                Property prop = new Property(property, parsingType, null, null, null);
                                System.out.println(prop);
                                this.nodePropertyMetadata.add(prop);

                            }
                        }
                        EdgeProperties edgeProperties = input.getParsedMetadata().getEdgeproperties();
                        if (edgeProperties != null) {
                            for (Object obj : edgeProperties) {
                                //edgeProperties JSONObject are in the form {"parsingtype":"string","weight":["string"]}
                                //for parsingtype we need to get the value for the other we need the key as title for our property
                                System.out.println(obj);
                                JSONObject nodePropertyJSON = (JSONObject) obj;
                                String parsingType = nodePropertyJSON.get(Property.KEY_PARSINGTYPE).toString();
                                String property = "NotFound";
                                for (Object key : nodePropertyJSON.keySet()) {
                                    String keyString = key.toString();
                                    if (!keyString.equals(Property.KEY_PARSINGTYPE)) {
                                        property = keyString;
                                    }
                                }
                                Property prop = new Property(property, parsingType, null, null, null);
                                System.out.println(prop);
                                this.edgePropertyMetadata.add(prop);

                            }
                        }
                        // if (input.getParsedMetadata().getProperties() != null) {
                        // for (Object obj :
                        // input.getParsedMetadata().getProperties()) {
                        // this.tablePropertyMetadata.add(new Property((JSONObject)
                        // obj));
                        // }
                        // }
                    }
                    for (JSONFile jsonFile : files) {

                        filename = jsonFile.getFileName().replace(jsonFile.getFileType(), "gml");
                        jsonFile.setFileName(filename);
                        jsonFile.setFileType("gml");

                        this.filenames.add(filename);
                        jsonFile.write(this.workspaceUrl);
                    }

                } else if (filetype.toLowerCase().equals("stf") || filetype.toLowerCase().equals(".stf")) {

                    SDTParser input = new SDTParser();
                    this.isGraphData = false;
                    // TODO: Datatables should be assumed to be in the SISOB
                    // datatable format and parsed e.g. to csv.
                    transformer.convertDataTableFormats(input, new CSVParser());

                    // should be renamed in getData or something similar.
                    files = transformer.getNetworks();

                    /////////////////
                    for (JSONFile jsonFile : files) {
                        filename = jsonFile.getFileName().replace(jsonFile.getFileType(), "csv");
                        jsonFile.setFileName(filename);
                        jsonFile.setFileType("csv");

                        this.filenames.add(filename);
                        jsonFile.write(this.workspaceUrl);
                    }
                } else {
                    success = false;
                    this.indicateError(this.agentID
                            + ": Cannot read the input. Maybe it is in the wrong format. Acceptable formats are .sdt or .sgf");
                }
            } catch (IllegalContentTypeException ex) {

                success = false;
                this.indicateError(this.agentID + ": Cannot read the input. The files seems to be binary.", ex);
                // logger.log(Level.SEVERE, this.agentID + ": Cannot read the input.
                // The files seems to be binary.", ex);
            } catch (IOException ex) {
                this.indicateError(this.agentID + ": Cannot read the input.", ex);
            } catch (RuntimeException ex) {
                success = false;
                this.indicateError(this.agentID + ": Cannot read the input. Maybe it is in the wrong format.", ex);
            }
        } else {
            // handle different formats

            for (JSONFile file : files) {
                System.out.println(file.toString());
                try {
                    if (file.getFileType().toLowerCase().equals("sgf")
                            || file.getFileType().toLowerCase().equals(".sgf")) {

                        SGFParser input = new SGFParser();
                        this.isGraphData = true;

                        Vector<JSONFile> fileVector = new Vector<JSONFile>();
                        fileVector.add(file);

                        // TODO save metadata
                        saveMetadata(fileVector, input);

                        transformer.setNetworks(fileVector);

                        transformer.convertNetworkFormats(input, new GMLNetworkParser(keepIds, extendedMetadata));
                        fileVector = transformer.getNetworks();

                        if (input.getParsedMetadata() != null) {
                            if (input.getParsedMetadata().getNetworkType() != null) {

                                this.networkType = input.getParsedMetadata().getNetworkType();
                            }
                            if (input.getParsedMetadata().getNodeproperties() != null) {
                                for (Object obj : input.getParsedMetadata().getNodeproperties()) {
                                    this.nodePropertyMetadata.add(new Property((JSONObject) obj));
                                }
                            }
                            if (input.getParsedMetadata().getEdgeproperties() != null) {
                                for (Object obj : input.getParsedMetadata().getEdgeproperties()) {
                                    this.edgePropertyMetadata.add(new Property((JSONObject) obj));
                                }
                            }
                            // if (input.getParsedMetadata().getProperties() !=
                            // null) {
                            // for (Object obj :
                            // input.getParsedMetadata().getProperties()) {
                            // this.tablePropertyMetadata.add(new
                            // Property((JSONObject)
                            // obj));
                            // }
                            // }
                        }
                        for (JSONFile jsonFile : fileVector) {

                            filename = jsonFile.getFileName().replace(jsonFile.getFileType(), "gml");
                            jsonFile.setFileName(filename);
                            jsonFile.setFileType("gml");

                            this.filenames.add(filename);
                            jsonFile.write(this.workspaceUrl);
                        }

                    } else if (file.getFileType().toLowerCase().equals("stf")
                            || file.getFileType().toLowerCase().equals(".stf")) {

                        SDTParser input = new SDTParser();
                        this.isGraphData = false;

                        Vector<JSONFile> fileVector = new Vector<JSONFile>();
                        fileVector.add(file);

                        // TODO: Datatables should be assumed to be in the SISOB
                        // datatable format and parsed e.g. to csv.
                        transformer.setNetworks(fileVector);

                        transformer.convertDataTableFormats(input, new CSVParser());

                        // should be renamed in getData or something similar.
                        fileVector = transformer.getNetworks();

                        /////////////////
                        for (JSONFile jsonFile : fileVector) {
                            filename = jsonFile.getFileName().replace(jsonFile.getFileType(), "csv");
                            jsonFile.setFileName(filename);
                            jsonFile.setFileType("csv");

                            this.filenames.add(filename);
                            jsonFile.write(this.workspaceUrl);
                        }
                    } else {
                        success = false;
                        this.indicateError(this.agentID
                                + ": Cannot read the input. Maybe it is in the wrong format. Acceptable formats are .stf or .sgf");
                    }
                } catch (IllegalContentTypeException ex) {

                    success = false;
                    this.indicateError(this.agentID + ": Cannot read the input. The files seems to be binary.", ex);
                    // logger.log(Level.SEVERE, this.agentID + ": Cannot read
                    // the input.
                    // The files seems to be binary.", ex);
                } catch (IOException ex) {
                    this.indicateError(this.agentID + ": Cannot read the input.", ex);
                } catch (RuntimeException ex) {
                    success = false;
                    this.indicateError(this.agentID + ": Cannot read the input. Maybe it is in the wrong format.", ex);
                }
            }
        }
        return success;
    }

    /**
     * Deprecated, use prepareAgentWorkspace(String uploadedData, boolean
     * keepIds) instead. This method calls prepareAgentWorkspace(String
     * uploadedData, boolean keepIds) with false as second parameter.
     *
     * @param uploadedData
     * @return
     */
    @Deprecated
    protected boolean prepareAgentWorkspace(String uploadedData) {
        return prepareAgentWorkspace(uploadedData, false, false);
    }

    private void saveMetadata(Vector<JSONFile> files, SGFParser parser) throws IllegalContentTypeException {
        metadataMapping = new HashMap<>();
        for (JSONFile file : files) {
            parser.setNetwork(file);
            parser.parse();
            metadataMapping.put(file.getFileName().replace(file.getFileType(), ""), parser.getParsedMetadata());
        }
    }

    protected String getType(String dataUrl) {
        String type = "";
        if (dataUrl.endsWith(".gml")) {
            type = "gml";
        } else if (dataUrl.endsWith(".csv")) {
            type = "csv";
        }

        return type;
    }

    /**
     * Deprecated, use executeRScript(String dataUrl, boolean restoreOldIds)
     * instead. This method calls executeRScript(String dataUrl, boolean
     * restoreOldIds) with false as second parameter.
     *
     * @param dataUrl
     * @return
     */
    @Deprecated
    protected boolean executeRScript(String dataUrl) {
        return this.executeRScript(dataUrl, false);
    }

    @SuppressWarnings("unchecked")
    protected boolean executeRScript(String dataUrl, boolean restoreOldIds) {

        String resDataUrls = null;
        // String decDataUrls = null;
        String metadata = null;

        boolean success = true;
        String suffix = "";
        RCaller caller = new RCaller();
        caller.setRscriptExecutable(RScriptExecutable);

        RCode code = new RCode();
        code.clear();
        // Set R working directory

        code.addRCode("setwd('" + this.workspaceUrl.replace(AgentProperties.SEPARATOR, "/") + "')");
        String label = "";

        // execute multiple graph script
        code.addRCode("library('igraph')");
        // File folder = new File(this.workspaceUrl);
        suffix = "";
        String file = "";

        code.addRCode("graphs <- list()");
        code.addRCode("tables <- list()");
        code.addRCode("labels <- c()");

        for (int i = 0; i < this.filenames.size(); i = i + 1) {
            file = this.filenames.get(i);
            suffix = this.getType(file);

            label = file.substring(0, file.indexOf("." + suffix));

            code.addRCode("labels[" + (i + 1) + "] <- '" + label + "'");

            if (this.isGraphData) {
                code.addRCode("g <- read.graph('"
                        + (this.workspaceUrl + AgentProperties.SEPARATOR + file).replace(AgentProperties.SEPARATOR, "/")
                        + "', 'gml')");

                code.addRCode("graphs[['" + label + "']] <- g");

            } else if (suffix.equals("csv")) {

                code.addRCode("data <- read.csv('" + (this.workspaceUrl + AgentProperties.SEPARATOR + file)
                        .replace(AgentProperties.SEPARATOR, "/") + "', header=TRUE)");// "',
                // row.names=1)");
                code.addRCode("tables[['" + label + "']] <- data");
            } else {

                code.addRCode("data <- read.table('" + (this.workspaceUrl + AgentProperties.SEPARATOR + file)
                        .replace(AgentProperties.SEPARATOR, "/") + "')");
                code.addRCode("tables[['" + label + "']] <- data");
            }
        }
        // Add extra variables for Single script Agents
        addScriptSpecificVariables(code);

        logger.fine(agentName + ": Read script...");
        code.addRCode(this.readScript(this.scriptStream));

        // Execute Script
        logger.fine(agentName + ": Execute script...");
        try {
            // System.out.println(code);
            caller.setRCode(code);
            caller.runAndReturnResult("resultData");

            resDataUrls = caller.getParser().getAsStringArray("dataUrl")[0];
            // System.out.println(this.resultDataUrl);
            // decDataUrls =
            // caller.getParser().getAsStringArray("decoratedUrl")[0];

            this.resultFiles = new JSONArray();
            // if (resDataUrls != null) {
            // try {
            // this.resultFiles.addAll(this.retrieveResults(resDataUrls));
            // } catch (IllegalContentTypeException ex) {
            // this.indicateError("Cannot read results.", ex);
            // }
            // }

            if (resDataUrls != null) {

                try {
                    metadata = caller.getParser().getAsStringArray("metadata")[0];
                } catch (Exception e) {
                    System.out.println("no metadata.");
                }
                if (metadata != null) {

                    // this.measureMetadata.addAll(this.extractMeasureMetadata(metadata));
                    // this.propertyMetadata.addAll(this.extractPropertiesMetadata(metadata));
                    // TODO this is only about node meta data, what about edge
                    // meta data??
                    this.nodePropertyMetadata.addAll(this.extractPropertiesMetadata(metadata));
                }
                try {
                    this.resultFiles.addAll(this.retrieveResults(resDataUrls, restoreOldIds));
                } catch (IllegalContentTypeException ex) {
                    success = false;
                    this.indicateError("Cannot read results.", ex);
                }
            }

        } catch (ExecutionException ex) {

            this.indicateError(agentName + ": Failure in R script!!!", ex);
            success = false;
        } catch (IOException ex) {

            this.indicateError(agentName + ": No results.", ex);
            success = false; // FIXME: Is this the right behavior
        }

        return success;
    }

    /**
     *
     * @param urls
     * @param restoreOldIds If true, the old Ids are restored (see
     * {@link prepareAgentWorkspace})
     * @return
     * @throws IOException
     * @throws IllegalContentTypeException
     */
    @SuppressWarnings("unchecked")
    protected JSONArray retrieveResults(String urls, boolean restoreOldIds)
            throws IOException, IllegalContentTypeException {
        StringTokenizer tokenizer;
        String filename;
        String suffix;
        JSONArray resFileSet = new JSONArray();
        GMLNetworkParser graphParser;
        SGFParser outputParser;

        Vector<JSONFile> graphs;
        Vector<JSONFile> tables;

        resFileSet = new JSONArray();
        graphs = new Vector<JSONFile>();
        tables = new Vector<JSONFile>();

        tokenizer = new StringTokenizer(urls, ",");

        while (tokenizer.hasMoreTokens()) {

            filename = tokenizer.nextToken();

            suffix = this.getType(filename);

            if (suffix.equals("gml")) {

                graphs.add(new JSONFile(new File(this.workspaceUrl + AgentProperties.SEPARATOR + filename), true));

            } else if (suffix.equals("csv")) {

                tables.add(new JSONFile(new File(this.workspaceUrl + AgentProperties.SEPARATOR + filename), true));
            }
        }

        if (graphs.size() > 0) {

            graphParser = new GMLNetworkParser(restoreOldIds);
            outputParser = new SGFParser();
            for (JSONFile graph : graphs) {

                graphParser.setNetwork(graph);
                graphParser.parse();

                if (graphParser.getParsedMetadata() == null) {

                    graphParser.updateMetadata(new Metadata());
                }

                if (this.nodePropertyMetadata != null) {
                    Metadata meta = graphParser.getParsedMetadata();
                    if (meta.getNodeproperties() == null) {
                        meta.setNodeproperties(new NodeProperties());
                    }
                    for (Property p : this.nodePropertyMetadata) {
                        if (meta.getNodeproperties().containsPropertyByKey(p.getPropertyKey())) {
                            List<Property> oldProperties = meta.getNodeproperties()
                                    .getPropertyByKey(p.getPropertyKey());
                            meta.getNodeproperties().removePropertyByKey(p.getPropertyKey());
                            Property tmp = p;
                            for (Property oldP : oldProperties) {
                                if (oldP.size() > tmp.size()) {
                                    tmp = oldP;
                                }
                            }
                            meta.getNodeproperties().add(tmp);
                        } else {
                            meta.getNodeproperties().add(p);
                        }
                    }
                }

                if (this.edgePropertyMetadata != null) {
                    Metadata meta = graphParser.getParsedMetadata();
                    if (meta.getEdgeproperties() == null) {
                        meta.setEdgeproperties(new EdgeProperties());
                    }
                    for (Property p : this.edgePropertyMetadata) {
                        if (meta.getEdgeproperties().containsPropertyByKey(p.getPropertyKey())) {
                            List<Property> oldProperties = meta.getEdgeproperties()
                                    .getPropertyByKey(p.getPropertyKey());
                            meta.getEdgeproperties().removePropertyByKey(p.getPropertyKey());
                            Property tmp = p;
                            for (Property oldP : oldProperties) {
                                if (oldP.size() > tmp.size()) {
                                    tmp = oldP;
                                }
                            }
                            meta.getEdgeproperties().add(tmp);
                        } else {
                            meta.getEdgeproperties().add(p);
                        }
                    }

                    // if (graphParser.getParsedMetadata().getEdgeproperties()
                    // == null) {
                    // graphParser.getParsedMetadata().setEdgeproperties(new
                    // EdgeProperties());
                    // }
                    // for (Property p : this.edgePropertyMetadata) {
                    // graphParser.getParsedMetadata().getEdgeproperties().add(p);
                    // }
                } else {
                    System.out.println("edge property metadata is null");
                }

                if (this.networkType != null) {

                    graphParser.getParsedMetadata().setNetworkType(this.networkType);
                }

                // TODO merge with old metadata
                Metadata mergedMetadata = mergeMetadata(graph.getFileName().replace(graph.getFileType(), ""),
                        graphParser.getParsedMetadata());

                // outputParser.setMetadata(graphParser.getMetadata());
                outputParser.updateMetadata(mergedMetadata);
                outputParser.updateNodeSet(graphParser.getParsedNodeSet());
                outputParser.updateEdgeSet(graphParser.getParsedEdgeSet());

                String newFileName = graph.getFileName().substring(0, graph.getFileName().lastIndexOf("."));
                resFileSet.add(new JSONFile((newFileName + outputParser.requestFileTypeExtension()),
                        outputParser.requestFileType(), outputParser.encode(), JSONFile.TEXT));
            }

        }
        if (tables.size() > 0) {

            CSVParser csvparser = new CSVParser();
            SDTParser sdtparser = new SDTParser();

            for (JSONFile table : tables) {
                csvparser.setTabledata(table);
                csvparser.parse();

                if (csvparser.getParsedMetadata() == null) {

                    csvparser.updateMetadata(new Metadata());
                }
                if (this.tablePropertyMetadata != null) {

                    if (csvparser.getParsedMetadata().getProperties() == null) {

                        csvparser.getParsedMetadata().setProperties(new Properties());
                    }
                    for (Property p : this.tablePropertyMetadata) {

                        csvparser.getParsedMetadata().getProperties().add(p);
                    }
                }

                sdtparser.updateMetadata(csvparser.getParsedMetadata());
                sdtparser.updateDataSet(csvparser.getParsedDataSet());

                String newFileName = table.getFileName().substring(0, table.getFileName().lastIndexOf("."));
                resFileSet.add(new JSONFile((newFileName + sdtparser.requestFileTypeExtension()),
                        sdtparser.requestFileType(), sdtparser.encode(), JSONFile.TEXT));
            }

            /*
			 * tableConverter = new FormatFactory(tables);
			 * 
			 * tableConverter.convertDataTableFormats(new CSVParser(), new
			 * SDTParser());
			 * 
			 * resFileSet.addAll(tables);
             */
        }

        return resFileSet;
    }

    @Deprecated
    protected JSONArray retrieveResults(String urls) throws IOException, IllegalContentTypeException {
        return retrieveResults(urls, false);
    }

    protected Metadata mergeMetadata(String filename, Metadata newMetadata) {
        Metadata oldMetadata = metadataMapping.get(filename);
        if (oldMetadata == null) {
            return newMetadata;
        }
        // actual merge is missing - this is just a replacement of old
        // properties with new ones
        oldMetadata.setNodeproperties(newMetadata.getNodeproperties());
        oldMetadata.setEdgeproperties(newMetadata.getEdgeproperties());
        return oldMetadata;
    }

    protected String readScript(InputStream stream) {

        String code = "";
        String line;
        String subscript = "";
        try {
            System.out.println("read script " + stream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            line = reader.readLine();

            while (line != null) {
                if (line.matches("source\\(\\S*\\)(\\S|s)*")) {
                    subscript = line.substring(line.indexOf("(") + 2, line.indexOf(")") - 1);
                    System.out.println(agentName + ": read subscript " + subscript);
                    code = code
                            + readScript(ClassLoaderUtility.getClassLoader().getResourceAsStream(subscript))
                            + System.getProperty("line.separator");
                } else {

                    code = code + line + System.getProperty("line.separator");
                }
                line = reader.readLine();
            }

        } catch (FileNotFoundException e) {

            this.indicateError(this.agentName + ": R Script not found.", e);
        } catch (IOException e) {

            this.indicateError(this.agentName + ": R Script is corrupted.", e);
        }

        return code;
    }

    protected List<Property> extractPropertiesMetadata(String metadata) {

        List<Property> properties = new ArrayList<Property>();
        Property property;
        StringTokenizer tokenizer = new StringTokenizer(metadata, ",");
        List<String> tokens = new ArrayList<String>();

        while (tokenizer.hasMoreTokens()) {

            tokens.add(tokenizer.nextToken());
        }

        if (tokens.size() < 5) {

            this.indicateError("Metadata not specified correctly in R script.");
        } else {

            for (int i = 0; i < tokens.size() / 5; i = i + 1) {
                property = new Property();
                for (int j = 0; j < 5; j = j + 1) {

                    if ((i * 5 + j) < tokens.size()) {
                        switch (j) {

                            case 0:
                                // property.setPropertyKey(tokens.get((i * 5 + j)));
                                property.setTitle(tokens.get((i * 5 + j)));
                                break;
                            case 1:
                                // property.setParsingType(tokens.get((i * 5 + j)));
                                property.setPropertyKey(tokens.get((i * 5 + j)));
                                break;
                            case 2:
                                // property.setTitle(tokens.get((i * 5 + j)));
                                property.setParsingType(tokens.get((i * 5 + j)));
                                break;
                            case 3:
                                // property.setDescription(tokens.get((i * 5 + j)));
                                break;
                            case 4:
                                // property.setSpecificType(tokens.get((i * 5 +
                                // j)));
                                break;
                            default:
                        }
                    }
                }
                properties.add(property);
            }
        }
        return properties;
    }

    /**
     * This is a hook for subclasses of RWrapperagent to inject extra R code
     * that declares necessary variables for the executed script
     *
     * Must be overwritten by more specific RWrapperAgents
     * @param code the RCode object that the RwrapperAgent writes the script to
     */
    protected void addScriptSpecificVariables(RCode code) {
       
    }

}
