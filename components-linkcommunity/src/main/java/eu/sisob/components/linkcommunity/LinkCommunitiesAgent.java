package eu.sisob.components.linkcommunity;

import java.util.HashSet;
import java.util.Vector;
import org.json.simple.JSONArray;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.format.graph.Dataset;
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
import eu.sisob.components.framework.util.AgentStatus;
import java.util.List;

/**
 * This Agent identifies LinkCommunities in a graph of SISOB graph format and
 * adds results as edge and node property clustering to the SISOB graph.
 *
 * @author Evelyn Fricke
 * adapted to workbench-ng: doberstein
 */
public class LinkCommunitiesAgent extends Agent {

	private Vector<JSONFile> datafiles;
	private Vector<JSONFile> resultNetworks;
	private String strResult;
	private Graph undirectedGraph;

	// Varibles needed for old version/Pajek-NET
	//private String netFile;
	//private File resultingNetFile;
	//private String resultingNetFilePath;
	//private String result;
	public LinkCommunitiesAgent(JsonObject coordinationMessage) {
		super(coordinationMessage);
	}

	/*
     * Sets Workingstatus, notifies manager, calls execute method.
	 */
 /*
    public void execAgentManually()
    {
        super.getAgentListener().notifyManager(this);
        executeAgent(fetchedTuple);   
    }*/
	/**
	 * Creates a graph from the given SISOB Graph as JSONFile and calculates its
	 * linkCommunities. Community membership is given as an edge property.
	 * Communities are identified by numbers (int).
	 */
	@Override
	public void executeAgent(JsonObject dataMessage) {
		setAgentWorkingStatus(AgentStatus.RUNNING);

		resultNetworks = JSONFile.restoreJSONFileSet(new Gson().toJson(dataMessage.get("payload")));
		// Check ...
		if (resultNetworks != null) {
			// ... then convert. Hoping, that it really is a JSON-String.
			System.out.println("LCA: Parsing networkfile.");
			Vector<JSONFile> manyFiles = (Vector<JSONFile>) resultNetworks.clone();
			// for each network
			try {
				for (JSONFile networkFile : manyFiles) {
					NetworkParser parser;
					Dataset netData;
					Metadata meta;
					EdgeProperties ep;
					NodeProperties np;
					Boolean edgePropSet = true;
					Boolean nodePropSet = true;
					// networkFile = new JSONFile(stringOfFile.toString());
					parser = new SGFParser();
					parser.setNetwork(networkFile);

					parser.parse();
					System.out.println("Parsing information from netfile.");
					meta = parser.getParsedMetadata();
					//Checking if undirected and one mode graph
					if ((meta.isDirectedNetwork() == null || meta.isDirectedNetwork().isEmpty() || meta.isDirectedNetwork().equalsIgnoreCase("false")) && (meta.getNetworkType() == null || meta.getNetworkType().isEmpty() || meta.getNetworkType().equals("undefined") || (meta.getNetworkType().equalsIgnoreCase("1 mode network") || meta.getNetworkType().equalsIgnoreCase("1-mode network")))) {
						System.out.println("Undirected/1 mode network.");
						netData = new Dataset(parser.getParsedNodeSet(), parser.getParsedEdgeSet());
						undirectedGraph = Graph.createGraphFromDataSet(netData);

						// Look for Link communities
						System.out.println("LCA: Looking for link communities");
						undirectedGraph.findLinkCommunities();

						//Transfer result back into SISOB graph format
						// Add new edgeproperty to the metadata section
						System.out.println("LCA: Adding community property.");
						ep = meta.getEdgeproperties();
						//Vector<String> propertyTypes = new Vector<String>();
						//propertyTypes.add("array");

						String clusterString = "clusters";
						Property clusterProp = new Property(clusterString);
						String propType = "array";
						if (!ep.getEdgePropertySet().contains(clusterProp)) {
							ep.addProperty(new Property(clusterString, propType));
							edgePropSet = false;
						}
						// do the same for nodes
						np = meta.getNodeproperties();
						if (!np.getNodePropertySet().contains(new Property(clusterString))) {
							np.addProperty(new Property(clusterString, propType));
							nodePropSet = false;
						}

						// Add value of property to every edge
						Vector<Edge> es = parser.getParsedEdgeSet().getValues();
						for (LCEdge e : undirectedGraph.getEdgeTable()) {
							String id = e.getID();
							for (Edge oe : es) {
								if (id.equals(oe.getId())) {
									JSONArray clusterValues = new JSONArray();
									if (e.getCommuntiyMembership() != 0) {
										clusterValues.add(new Integer(e.getCommuntiyMembership()));
									}
									if (edgePropSet) {
										oe.removeProperty(clusterString);
									}
									oe.addProperty(clusterString, clusterValues);

									System.out.println("LCA: Edge property: " + clusterString + " , Edge " + e.getID() + " : " + "[" + String.valueOf(e.getCommuntiyMembership()) + "] new.");
								}
							}
						}

						//Add value of property to every node
						Vector<Node> ns = parser.getParsedNodeSet().getValues();
						for (LCNode n : undirectedGraph.getNodeTable()) {
							String id = n.getID();
							for (Node on : ns) {
								if (id.equals(on.getId())) {
									JSONArray clusterValues = new JSONArray();
									HashSet<Integer> communityList = n.getCommunityMemebership();
									if (communityList.size() > 0) {
										for (Integer i : communityList) {
											if (i != 0) {
												clusterValues.add(new Integer(i));
											}
										}
									}
									if (nodePropSet) {
										on.removeProperty(clusterString);
									}
									on.addProperty(clusterString, clusterValues);
									System.out.println("LCA: Node  property: " + clusterString + " , Node " + n.getID() + " : " + clusterValues + " new.");
								}
							}
						}

						// setting the new information
						EdgeSet etempSet = new EdgeSet();
						etempSet.setValues(es);
						parser.updateEdgeSet(etempSet);
						NodeSet ntempSet = new NodeSet();
						ntempSet.setValues(ns);
						parser.updateNodeSet(ntempSet);
						meta.setEdgeproperties(ep);
						meta.setNodeproperties(np);
						meta.setDirectedNetwork(NetworkParser.UNDIRECTED_NETWORK);
						meta.setNetworkType("1 mode network");
						parser.updateMetadata(meta);
						//making JSONFile
						//strResult = parser.encode();
						//parser.printEdgeSet();
						String netname = "graph" + System.currentTimeMillis() + ".sgf";
						resultNetworks.clear();
						resultNetworks.add(new JSONFile(netname, "sgf", parser.encode(), JSONFile.TEXT));
						System.out.println("LCA: Communities found and written to data.");
					} else {
//	        				System.out.println("LCA: Network has already been clustered. Sending original data.");
//		        			strResult = fetchedTuple.getField(3).getValue().toString();
						System.out.println("LCA: Could not interpret input.");
					}
				}
				// Converting Files back to JSONString.
				if (!resultNetworks.isEmpty()) {
					System.out.println("LCA: Number of networks: " + resultNetworks.size());
					strResult = JSONFile.collectionToString(resultNetworks);
					uploadResults();
					indicateDone();
				} else {
					indicateError("No results. Network should be SISOB-graph format, one mode and undirected.");
				}
			} catch (IllegalContentTypeException icte) {
				icte.printStackTrace();
				indicateError("Network not of the expected type ...", icte);
			}
		} else {
			indicateError("LCA: No data or not of the expected type.");
		}

		/*
		 * Old version for Pajec-Net.
		 * 
    	if(stringOfFile instanceof String && !((String) stringOfFile).isEmpty())
		{
			netFile = (String)stringOfFile;
			System.out.println("LinkCommunityAgent: File recieved.");
			undirectedGraph = Graph.createGraphFromNetFile(netFile);
			if(undirectedGraph != null)
			{
				undirectedGraph.findLinkCommunities();
				uploadResults();
			}
			else
			{
				System.err.println("Expecting file of format .net");
			}
		}
		else
		{
			System.err.println("No data");
		}
		 */
	}

	/*
    @Override
    protected void uploadResults() 
    {
		//System.out.println("Writing out results. " + strResult);
    	try
		{
			Tuple resultingDataTuple = new Tuple(this.getWorkflowID(), 1, this.getAgentInstanceID() + ".out_1", strResult, "");
            getSisobspace().write(resultingDataTuple);
		}
		catch(TupleSpaceException tse)
		{
			tse.printStackTrace();
			indicateError("Could not write result to TupleSpace ...", tse);
		}
    	indicateDone();
    }*/
	@SuppressWarnings("unchecked")
	@Override
	protected void uploadResults() {
		try {
			System.out.println("Uploading Results...");
			Thread.sleep(2000);

			JSONArray fileSet = new JSONArray();
			for (int i = 0; i < resultNetworks.size(); i++) {
				JSONFile file = resultNetworks.get(i);
				fileSet.add(file);
			}

			storeData(getWorkflowID(), getAgentInstanceID() + ".out_1", fileSet.toJSONString());

		} catch (Exception e) {
			indicateError(e.getMessage());
			e.printStackTrace();
		}
		System.out.println("Task Complete!");
	}

	/*
    @Override
    public void run() 
    {
        execAgentManually();
        super.run();
    }*/
	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		if (dataMessages.size() == 1) {
			executeAgent(dataMessages.get(0));
		} else {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	}

}
