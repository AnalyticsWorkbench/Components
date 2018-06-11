package info.collide.components.spmanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoCMSPAM;
import eu.sisob.api.parser.DataTableParser;
import eu.sisob.api.parser.LogParser;
import eu.sisob.api.parser.activitystreams.ActivityStreamParser;
import eu.sisob.api.parser.sisob.SDTParser;
import eu.sisob.api.visualization.format.datatable.Dataset;
import eu.sisob.api.visualization.format.datatable.fields.DataField;
import eu.sisob.api.visualization.format.eventlog.fields.EventLog;
import eu.sisob.api.visualization.format.metadata.Metadata;
import eu.sisob.api.visualization.format.metadata.fields.NodeProperties;
import eu.sisob.api.visualization.format.metadata.fields.Property;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import info.collide.workbench.components.spmanalysis.algorithms.Algorithm;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class SPMAnalysisAgent extends Agent {

    String[] translationFields;
    String[] groupByFields;
    String splitAtProperty;
    String splitAtValue;
    Algorithm algo = null;
    double minSupport = 0.0;
    boolean continuous = false;
    SequenceDBGenerator generator;
    JSONFile resultFile;
    boolean useExcelOutput = false;
    File tmpDirectory;

    public SPMAnalysisAgent(JsonObject coordinationMsg) {
        super(coordinationMsg);

        
    JSONArray inputEncoding = (JSONArray) this.getFilterParameters().get("input_encoding");
    JSONArray groupBy = (JSONArray) this.getFilterParameters().get("group_by");
       
        // Convert inputEncoding JsonArray to String[] array
        int len = inputEncoding.size();
        this.translationFields = new String[len];
        for (int i = 0; i < len; i++) {
            this.translationFields[i] = (String) inputEncoding.get(i).toString();
        }

        // Convert groupBy JsonArray to String[] array
        int len2 = groupBy.size();
        this.groupByFields = new String[len2];
        for (int i = 0; i < len2; i++) {
            this.groupByFields[i] = (String) groupBy.get(i).toString();
        }

        // TODO: Use constants for parameter names.
        this.minSupport = Double.parseDouble(this.getFilterParameters().get("support").toString());
        this.continuous = Boolean.parseBoolean(this.getFilterParameters().get("continuous").toString());
        this.splitAtProperty = this.getFilterParameters().get("split_at_property").toString();
        this.splitAtValue = this.getFilterParameters().get("split_at_value").toString();

        if (this.getFilterParameters().get("output").toString().equalsIgnoreCase("Excel file")) {
            this.useExcelOutput = true;
        } else {
            this.useExcelOutput = false;
        }
        System.out.println("SPM Agent created ++++");
        
    }

    @Override
    public void executeAgent(JsonObject dataMessage) {
        try {

            System.out.println("Execute SPM Agent ++++");
            Vector<JSONFile> files = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
            for (JSONFile file : files) {
                // parse logfile
                LogParser parser = new ActivityStreamParser();
                parser.setLogfile(file);
                parser.parse();
                List<EventLog> eventlogs = parser.getEventLogs();

                // translate events into algorithm input
                generator = new SequenceDBGenerator();

                String algorithmInput = generator.translateEventLogsToAlgorithmInput(
                        eventlogs,
                        this.translationFields,
                        this.groupByFields,
                        this.splitAtProperty,
                        this.splitAtValue
                );

                // call algorithm
                File algoResult = runCMSPAM(algorithmInput);

                // translate result
                if (useExcelOutput) {
                    resultFile = translateOutputToExcel(algoResult);
                } else {
                    resultFile = translateOutput(algoResult);
                }
            }

            this.uploadResults();
        } catch (IllegalContentTypeException ex) {
            Logger.getLogger(SPMAnalysisAgent.class.getName()).log(Level.SEVERE, null, ex);
            this.indicateError(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(SPMAnalysisAgent.class.getName()).log(Level.SEVERE, null, ex);
            this.indicateError(ex.getMessage());
        } catch (Exception ex) {
            this.indicateError(null, ex);
        }
    }

    @Override
    public void executeAgent(List<JsonObject> dataMessages) {

    }

    @Override
    protected void uploadResults() {
        LinkedList<JSONFile> results = new LinkedList<JSONFile>();
        results.add(resultFile);
        storeData(this.getWorkflowID(), this.getAgentInstanceID() + ".out_1", JSONFile.collectionToString(results));
        indicateDone();
    }

    File runCMSPAM(String input) throws IOException {
        // write input to disk
        File inputFile = File.createTempFile(workflowID + "_" + agentInstanceID + "_input", ".txt");
        tmpDirectory = inputFile.getParentFile();
        File outputFile = new File(tmpDirectory + File.separator + workflowID + "_" + agentInstanceID + "_output.txt");

        System.out.println("Writing algo results to " + outputFile.getAbsolutePath());

        FileWriter inputWriter = new FileWriter(inputFile);
        inputWriter.write(input);
        inputWriter.close();

        // call algorithm
        // Load a sequence database
        String inputPath = inputFile.getAbsoluteFile().toString();
        String outputPath = outputFile.getAbsoluteFile().toString();

        // Create an instance of the algorithm
        AlgoCMSPAM algo = new AlgoCMSPAM();

        // This optional parameter allows to specify the minimum pattern length:
        algo.setMinimumPatternLength(2); // optional

        // This optional parameter allows to specify the maximum pattern length:
        // algo.setMaximumPatternLength(4); // optional
        // This optional parameter allows to specify constraints that some
        // items MUST appear in the patterns found by TKS
        // E.g.: This requires that items 1 and 3 appears in every patterns
        // found
        // algo.setMustAppearItems(new int[] {1, 3});
        // This optional parameter allows to specify the max gap between two
        // itemsets in a pattern. If set to 1, only patterns of contiguous
        // itemsets
        // will be found (no gap).
        if (continuous) {
            algo.setMaxGap(1);
        }

        // if you set the following parameter to true, the sequence ids of the
        // sequences where
        // each pattern appears will be shown in the result
        boolean outputSequenceIdentifiers = true;

        // execute the algorithm
        algo.runAlgorithm(inputPath, outputPath, minSupport, outputSequenceIdentifiers);

        inputFile.delete();
        return outputFile;
    }

    JSONFile translateOutput(File outputFile) throws IOException {
        // parser for generating the data table
        DataTableParser dtparser = new SDTParser();

        // generate meta data and add it to parser
        Metadata metadata = new Metadata("Action Sequences", "");
        NodeProperties properties = new NodeProperties();
        properties.addProperty(new Property("sequence", "String"));
        properties.addProperty(new Property("support", "String"));
        properties.addProperty(new Property("support (relative)", "String"));
        properties.addProperty(new Property("length", "String"));
        properties.addProperty(new Property("users", "String"));
        dtparser.updateMetadata(metadata);

        // generate data and add it to parser
        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
        Dataset dataset = new Dataset();
        while (reader.ready()) {
            String line = reader.readLine();
            if (line != null) {
                DataField df = new DataField();
                // typical line:
                // 1 -1 11 -1 #SUP: 80 #SID: 0 1 2 3 4 5 ....
                // split line into parts
                int supIndex = line.indexOf("#SUP");
                int sidIndex = line.indexOf("#SID");
                String sequence = line.substring(0, supIndex).trim();
                String support = line.substring(supIndex + 5, sidIndex).trim();
                String sids = line.substring(sidIndex + 5).trim();

                // translate sequence
                StringBuilder sequenceBuilder = new StringBuilder();
                boolean first = true;
                int lengthCounter = 0;

                // StringTokenizer does not work with '-1' as delimiter
                // => own method for splitting the sequence
                // List<String> splitSequence = splitSequence(sequence);
                // for (String token : splitSequence) {
                // if (!token.isEmpty()) {
                // if (!first) {
                // sequenceBuilder.append(" \u2192 ");
                // }
                // sequenceBuilder.append(generator.getEvents().get(Integer.valueOf(Integer.parseInt(token))).toString());
                // lengthCounter++;
                // first = false;
                // }
                // }
                StringTokenizer st = new StringTokenizer(sequence, " ");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (!"-2".equals(token)) {
                        if (!first) {
                            sequenceBuilder.append(" \u2192 ");
                        }
                        sequenceBuilder
                                .append(generator.getEvents().get(Integer.valueOf(Integer.parseInt(token))).toString());
                        lengthCounter++;
                        first = false;
                    }
                    if (st.hasMoreTokens()) {
                        st.nextToken();
                    }
                }

                df.addMeasure("sequence", sequenceBuilder.toString());

                // add support and length information
                df.addMeasure("support", support);
                double sup = Double.parseDouble(support);
                sup = sup / generator.getSequenceIdToGroupId().size();
                df.addMeasure("support (relative)", Double.toString(sup));
                df.addMeasure("length", Integer.toString(lengthCounter));

                // add group information
                HashSet<String> groupSet = new HashSet<String>();
                StringTokenizer sidTokenizer = new StringTokenizer(sids, " ");
                while (sidTokenizer.hasMoreTokens()) {
                    String token = sidTokenizer.nextToken().trim();
                    if (!token.isEmpty()) {
                        String group = generator.getSequenceIdToGroupId().get(Integer.parseInt(token));
                        if (group != null && !group.equals("null")) {
                            groupSet.add(group);
                        }
                    }
                }
                LinkedList<String> groupList = new LinkedList<String>(groupSet);
                Collections.sort(groupList);
                StringBuilder groupBuilder = new StringBuilder();
                first = true;
                for (String group : groupList) {
                    if (!first) {
                        groupBuilder.append(", ");
                    }
                    groupBuilder.append(group);
                    first = false;
                }
                df.addMeasure("users", groupBuilder.toString());

                // add row to data set
                dataset.addDataField(df);
            }
        }
        dtparser.updateDataSet(dataset);

        reader.close();
        outputFile.delete();

        // generate JSONFile from data table
        JSONFile dataFile = new JSONFile("datatable" + System.currentTimeMillis() + ".sdt", "sdt", dtparser.encode(),
                JSONFile.TEXT);

        return dataFile;
    }

    JSONFile translateOutputToExcel(File algoOutput) throws IOException {
        // generate data and add it to parser
        BufferedReader reader = new BufferedReader(new FileReader(algoOutput));

        // create the excel workbook and sheet to store the result into
        SXSSFWorkbook wb = new SXSSFWorkbook(5); // keep 5 rows in memory
        Sheet sh = wb.createSheet();
        // create first row for column headers
        int rowCounter = 0;
        Row row = sh.createRow(rowCounter);
        // fill in the column header cells
        String[] headers = {"sequence", "length", "support", "support (relative)", "users"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
        }

        while (reader.ready()) {
            String line = reader.readLine();
            if (line != null) {
                // create table row and prepare cell counter
                System.out.println("Current row: " + rowCounter);
                row = sh.createRow(++rowCounter);
                int cellCounter = 0;

                // typical line:
                // 1 -1 11 -1 #SUP: 80 #SID: 0 1 2 3 4 5 ....
                // split line into parts
                int supIndex = line.indexOf("#SUP");
                int sidIndex = line.indexOf("#SID");
                String sequence = line.substring(0, supIndex).trim();
                String support = line.substring(supIndex + 5, sidIndex).trim();
                String sids = line.substring(sidIndex + 5).trim();

                // translate sequence
                StringBuilder sequenceBuilder = new StringBuilder();
                boolean first = true;
                int lengthCounter = 0;

                // StringTokenizer does not work with '-1' as delimiter
                StringTokenizer st = new StringTokenizer(sequence, " ");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (!"-2".equals(token)) {
                        if (!first) {
                            sequenceBuilder.append(" \u2192 ");
                        }
                        sequenceBuilder
                                .append(generator.getEvents().get(Integer.valueOf(Integer.parseInt(token))).toString());
                        lengthCounter++;
                        first = false;
                    }
                    if (st.hasMoreTokens()) {
                        st.nextToken();
                    }
                }

                Cell cell = row.createCell(cellCounter);
                cell.setCellValue(sequenceBuilder.toString());

                // add length and support information
                cell = row.createCell(++cellCounter);
                cell.setCellValue(lengthCounter);

                double sup = Double.parseDouble(support);
                cell = row.createCell(++cellCounter);
                cell.setCellValue(sup);

                sup = sup / generator.getSequenceIdToGroupId().size();
                cell = row.createCell(++cellCounter);
                cell.setCellValue(sup);

                // add user information
                HashSet<String> groupSet = new HashSet<String>();
                StringTokenizer sidTokenizer = new StringTokenizer(sids, " ");
                while (sidTokenizer.hasMoreTokens()) {
                    String token = sidTokenizer.nextToken().trim();
                    if (!token.isEmpty()) {
                        String group = generator.getSequenceIdToGroupId().get(Integer.parseInt(token));
                        if (group != null && !group.equals("null")) {
                            groupSet.add(group);
                        }
                    }
                }
                LinkedList<String> groupList = new LinkedList<String>(groupSet);
                Collections.sort(groupList);
                StringBuilder groupBuilder = new StringBuilder();
                first = true;
                for (String user : groupList) {
                    if (!first) {
                        groupBuilder.append(", ");
                    }
                    groupBuilder.append(user);
                    first = false;
                }
                cell = row.createCell(++cellCounter);
                cell.setCellValue(groupBuilder.toString());
            }
        }

        reader.close();
        algoOutput.delete();

        // create writer and write workbook
        File outputFile = null;
        if (tmpDirectory != null) {
            outputFile = new File(tmpDirectory + File.separator + workflowID + "_" + agentInstanceID + "_output.xlsx");
        } else {
            outputFile = File.createTempFile(workflowID + "_" + agentInstanceID + "_input", ".xlsx");
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        wb.write(fos);

        // close workbook writer and dispose workbook temp files and close
        // workbook
        fos.close();
        wb.dispose();
        wb.close();

        // generate JSONFile from data table
        JSONFile dataFile = new JSONFile(outputFile, false);

        return dataFile;
    }

    Metadata createTableMetadata() {
        Metadata meta = new Metadata("Title", "Description");
        NodeProperties nodeProperties = new NodeProperties();
        nodeProperties.addProperty(new Property("support", "string"));
        nodeProperties.addProperty(new Property("length", "string"));
        meta.setNodeproperties(nodeProperties);
        return meta;
    }

    List<String> splitSequence(String input) {
        LinkedList<String> result = new LinkedList<String>();
        while (input.indexOf("-1") > -1) {
            String token = input.substring(0, input.indexOf("-1")).trim();
            input = input.substring(input.indexOf("-1") + 2);
            if (!token.equals("-2")) {
                result.add(token);
            }
        }
        return result;
    }

}
