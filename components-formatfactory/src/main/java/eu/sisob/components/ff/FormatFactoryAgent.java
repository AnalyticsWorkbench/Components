package eu.sisob.components.ff;

import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.sisob.api.parser.DataTableParser;
import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.parser.csv.CSVExcelParser;
import eu.sisob.api.parser.csv.CSVParser;
import eu.sisob.api.parser.gml.GMLNetworkParser;
import eu.sisob.api.parser.pajek.PajekParser;
import eu.sisob.api.parser.sisob.SDTParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.parser.ucinetdl.UCINETParser;
import eu.sisob.components.framework.Agent;
import eu.sisob.components.framework.graph.parser.FormatFactory;
import eu.sisob.components.framework.json.util.JSONFile;

public class FormatFactoryAgent extends Agent {

	private Vector<JSONFile> networks;

	public FormatFactoryAgent(JsonObject commandMsg) {
		super(commandMsg);
		JsonObject dataStructure = new JsonObject();

		dataStructure.addProperty("runid", commandMsg.get("runid").getAsString());
		dataStructure.addProperty("pipes", commandMsg.get("pipes").getAsString());

		setDataStructure(dataStructure);
	}

	@Override
	public void executeAgent(JsonObject dataMessage) {		
		String[] parameters = { (String) getFilterParameters().get(FormatFactoryManager.INPUT_PARAMETER),
				(String) getFilterParameters().get(FormatFactoryManager.OUTPUT_PARAMETER) };
		try {
			this.networks = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
			if (isInputBASE64(this.networks)) {
				this.networks = transformToText(this.networks);
			}

			FormatFactory factory = new FormatFactory(networks);

			NetworkParser inputParser = selectNetworkParser(parameters[0]);
			NetworkParser outputParser = selectNetworkParser(parameters[1]);

			String strategy = extractStrategy(parameters[0]);
			if (strategy == null) {
				strategy = extractStrategy(parameters[1]);
			}

			if (inputParser != null && outputParser != null) {
				// network to network transformation
				factory.convertNetworkFormats(inputParser, outputParser);
				this.networks.clear();
				for(int i=0;i<factory.getNetworks().size();i++) {
					this.networks.add(factory.getNetworks().get(i));
				}
				
			} else if (inputParser != null && outputParser == null) {
				// network to data table transformation
				factory.convertNetworkFormatToDataTableFormat(inputParser, selectDataTableParser(parameters[1]), strategy);
				this.networks.clear();
				for(int i=0;i<factory.getNetworks().size();i++) {
					this.networks.add(factory.getNetworks().get(i));
				}
			} else if (inputParser == null && outputParser != null) {
				// data table to network transformation
				factory.convertDataTableFormatToNetworkFormat(selectDataTableParser(parameters[0]), outputParser, strategy);
				for(int i=0;i<factory.getNetworks().size();i++) {
					this.networks.add(factory.getNetworks().get(i));
				}
			} else if (inputParser == null && outputParser == null) {
				// data table to data table
				factory.convertDataTableFormats(selectDataTableParser(parameters[0]), selectDataTableParser(parameters[1]));
				this.networks.clear();
				for(int i=0;i<factory.getNetworks().size();i++) {
					this.networks.add(factory.getNetworks().get(i));
				}
			}

			uploadResults();
		} catch (Exception ex) {
			this.indicateError(this.agentID + ": Cannot parse input data set. Please check the file set content.", ex);
		}
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {

	}

	private String extractStrategy(String string) {

		String[] tokens = string.split("-");
		if (tokens.length > 1)
			return tokens[1].toUpperCase().trim();

		return null;
	}

	private NetworkParser selectNetworkParser(String parserName) {
		NetworkParser p = null;

		if (parserName.equals("Pajek")) {
			p = new PajekParser();
		} else if (parserName.equals("SISOB Graph Format")) {
			p = new SGFParser();
		} else if (parserName.equals("GML")) {
			p = new GMLNetworkParser();
		} else if (parserName.equals("UCINET DL Format")) {
			p = new UCINETParser();
		}

		return p;
	}

	private DataTableParser selectDataTableParser(String parserName) {
		DataTableParser p = null;
		if (parserName.equals("SISOB Data Table Format - Nodes") || parserName.equals("SISOB Data Table Format - Edges")) {
			p = new SDTParser();
		} else if (parserName.equals("CSV Standard")) {
			p = new CSVParser();
		} else if (parserName.equals("CSV Excel")) {
			p = new CSVExcelParser();
		}
		
		return p;
	}

	@Override
	protected void uploadResults() {
		try {
			String uploadData = JSONFile.collectionToString(networks);
			storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", uploadData);
			System.out.println("Task Complete!");

		} catch (Exception e) {
			e.printStackTrace();
		}
		indicateDone();
	}
}
