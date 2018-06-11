package eu.sisob.components.linkcommunity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import eu.sisob.api.visualization.format.graph.Dataset;
import eu.sisob.api.visualization.format.graph.fields.Edge;
import eu.sisob.api.visualization.format.graph.fields.Node;

/**
 * A graph holds an edge table and a LCNode table. It can calculate its on link communities.
 * The class can also create a graph object from an existing pajek graph file .NET
 * @author Evelyn Fricke
 */

public class Graph
{
	private ArrayList<LCNode> LCNodeTable;
	private ArrayList<LCEdge> edgeTable;
	private ClusteringStep maxPD = new ClusteringStep(); // contains the link communities with the highest Partitioning Density
	
	public Graph(ArrayList<LCNode> nt, ArrayList<LCEdge> et)
	{
		this.LCNodeTable = nt;
		this.edgeTable = et;
	}
	
	/** This method creates an undirected, unweighted graph from a pajek .net-file. 
	 * It can only deal with one mode networks.
	 * 
	 * @param String the .NET file.
	 * @return A Graph as described by this file.
	 */
	public static Graph createGraphFromNetFile(String netFile)
    {
    	Graph theGraph;
    	ArrayList<LCNode> tempLCNodeTable = new ArrayList<LCNode>();
    	ArrayList<LCEdge> tempEdgeTable = new ArrayList<LCEdge>();
    	
    	String line;
    	int numberOfVertices = 0;
    	int vertexCount;
    	int edgeID = 1;
    	boolean noProblemSoFar = true;
    	int vertexPropertyNumber = 0;
    	
    	String[] splittedIntoLines = netFile.split("\n");
    	ArrayList<String> lines = new ArrayList<String>();
    // Checking for empty lines.
    	for(String l : splittedIntoLines)
    	{
    		l = l.trim();
    		if(!l.isEmpty())
    		{
    			lines.add(l);
    		}
    	}
		
	// Vertices following
    	line = lines.get(0);
    	System.out.println(line);
		if(line.contains("*vertices") || line.contains("*Vertices")) // Just cheking if this is, what I am expecting
		{
			String[] verticesPrefix = line.split(" ");
			if(verticesPrefix.length > 2) // One mode network
			{
				System.err.println("This Agent only excepts one-mode-networks.");
				noProblemSoFar = false;
			}
			numberOfVertices = Integer.valueOf(verticesPrefix[1]);
			System.out.println("Number of vertices: " + verticesPrefix[1]);
		}
		else
		{
			System.err.println("No *vertices at the beginnig of NET-file.");
			noProblemSoFar = false;
		}
		
	// vertex list: Create LCNodes.
		System.out.println("LCNodes:");
		if(noProblemSoFar)
		{
			for(int i = 1; i < numberOfVertices+1; i++)
			{
				LCNode n;
				line = lines.get(i);
				String[] lable = line.split("\""); // find the lable
				if(lable.length > 1) // if there is a lable
				{
					n = new LCNode(lable[0].trim(), lable[1].trim());
					System.out.println(n.getID());
					tempLCNodeTable.add(n);
				}
				else
				{
					String[] id = line.split(" ");
					n = new LCNode(id[0].trim(), null);
					System.out.println(n.getID());
					tempLCNodeTable.add(n);
				}				
			}
			
	//edges following until end of document
			// TODO: What if it uses edgelists?
			line = lines.get(numberOfVertices+1);
			if(line.startsWith("*edge") || line.startsWith("*Edge") || line.startsWith("*arc") || line.startsWith("*Arc"))
			{
				System.out.println("Starting edges.");
			}
			else
			{
				System.err.println("Unable to create edges.");
				noProblemSoFar = false;
			}
		}
		
	// edges listed as LCNode pairs: Create Edge.
		if(noProblemSoFar)
		{
			for(int i = numberOfVertices + 2; i < lines.size(); i++)
			{
				line = lines.get(i);
				String[] getTargetSourceString = line.split(",");
				line = getTargetSourceString[0].trim();
				String[] edge = line.split(" ");
				boolean sourceFound = false;
				boolean targetFound = false;
				LCNode s = new LCNode();
				LCNode t = new LCNode();
				for(LCNode sourceOrTarget : tempLCNodeTable)
				{
					if(edge[0].equals(sourceOrTarget.getID()) )
					{
						s = sourceOrTarget;
						if(targetFound)
						{
							break;
						}
						else
						{
							sourceFound = true;
						}
					}
					if(edge[1].equals(sourceOrTarget.getID()))
					{
						t = sourceOrTarget;
						if(sourceFound)
						{
							break;
						}
						else
						{
							targetFound = true;
						}
					}
				}
				LCEdge e = new LCEdge(s, t, String.valueOf(edgeID));
				System.out.println("Edge " + e.getID() + " source " + e.getSource().getID() + " target " + e.getTarget().getID());
				tempEdgeTable.add(e);
				edgeID++;
			}
		}
		if(noProblemSoFar)
		{
			theGraph = new Graph(tempLCNodeTable, tempEdgeTable);
		}
		else
		{
			theGraph = null;
		}
    	return theGraph;
    }
	
	/**
	 * Creates a graph from a Dataset of the SISOB workbench.
	 * Since this algorithm is only implemented for undirected networks, make sure to check the net type.
	 * @param data
	 * @return
	 */
	public static Graph createGraphFromDataSet(Dataset data)
	{
		Graph theGraph;
    	ArrayList<LCNode> tempLCNodeTable = new ArrayList<LCNode>();
    	ArrayList<LCEdge> tempEdgeTable = new ArrayList<LCEdge>();
    	
		
    	Vector<Node> tempNodes = data.getNodeSet().getValues();
    	Vector<Edge> tempEdges = data.getEdgeSet().getValues();
			
    	// Fill NodeTable
    	for(Node n: tempNodes)
    	{
    		LCNode newNode = new LCNode(n.getId(), n.getLabel());
    		tempLCNodeTable.add(newNode);
    	}
    	// Fill EdgeTable
    	for(Edge e: tempEdges)
    	{
    		LCNode target = new LCNode();
    		LCNode source = new LCNode();
    		boolean sFound = false;
    		boolean tFound = false;
    		for(LCNode st:tempLCNodeTable)
    		{
    			if(st.getID().equals(e.getSource()))
    			{
    				source = st;
    				sFound = true;
    			}
    			if(st.getID().equals(e.getTarget()))
    			{
    				target = st;
    				tFound = true;
    			}
    			if(sFound && tFound)
    			{
    				break;
    			}
    		}
    		if(source != null && target != null)
    		{
    			LCEdge newEdge = new LCEdge(source, target, e.getId());
    			tempEdgeTable.add(newEdge);
    		}
    		else
    		{
    			System.err.println("LCA: Could not find Target or Sourve Node for all edges of the given graph.");
    			throw new NullPointerException();
    		}
    	}
    	
    	theGraph = new Graph(tempLCNodeTable, tempEdgeTable);
    	return theGraph;
	}
	
	/**Gets all the edges of the graph.
	 * @return ArrayList of all the edges of the graph.
	 */
	public ArrayList<LCEdge> getEdgeTable(){return edgeTable;}
	
	/**Gets all the LCNodes of the graph.
	 * @return ArrayList of all the LCNodes of the graph.
	 */
	public ArrayList<LCNode> getNodeTable(){return LCNodeTable;}
	
	/**Takes a LCNode and returns its adjacent LCNodes in an ArrayList.
	 * 
	 * @param LCNode n
	 * @return ArrayList<LCNode> with neighbours
	 */
	public ArrayList<LCNode> getNeighbors(LCNode n)
	{
		LCNode origin = n;
		ArrayList<LCNode> neighbors = new ArrayList<LCNode>();
		for(LCEdge e : edgeTable)
		{
			if(e.getTarget() == origin)
			{
				neighbors.add(e.getSource());
			}
			if(e.getSource() == origin)
			{
				neighbors.add(e.getTarget());
			}
		}
		return neighbors;
	}
	
	/**Takes this graph and calculates the link communities with the highest partition density.
	 * Edges belonging to a community are assigned a communityMembership as an attribute.
	 * 
	 * @see LCEdge
	 */
	public void findLinkCommunities()
    {
       
		System.out.println("Starting to calculate Link Communities:");
		// Hilfsvariablen findLinkCommunities
		String report = "";
		ArrayList<LCEdge> edges; // Lists all Edges of the visible Graph
	    ArrayList<Edgepair> similarityMatrix = new ArrayList<Edgepair>(); // Lists all Edgepairs
	    
	    Graph currentGraph;

    	currentGraph = this;
       
    // Checking data, getting data.
        int count = currentGraph.getNodeTable().size();
        if(count == 0) // Check, if there is a graph at all
        {
            report = "Your trying to partition an empty graph.";
        }
        else
        {
            edges = currentGraph.getEdgeTable();
            
            int countEdges = edges.size();
            // Debugging
            String showMeEdges = ("Number of edges: " + countEdges + "\n Ids der Kanten: ");


// Calculating similarity for each edgepair and storing it in similarity-matrix
            float similarity = 0.0f;
            LCEdge firstEdge;
            LCEdge secondEdge;
            LCNode keystone = null;
            LCNode firstEdgeLCNode = null;
            LCNode secondEdgeLCNode = null;
            int countFirstNeighbours;
            int countSecondNeighbours;
            int allNeighbours;
            int commonNeighbours = 0;
            boolean triangle = false;
            LCNode source;
            LCNode target;
            LCNode compareSource;
            LCNode compareTarget;
            
            for(int x=0; x<countEdges-1; x++)
            {
                firstEdge = edges.get(x);
                source = firstEdge.getSource();
                target = firstEdge.getTarget();
                for(int y = x+1; y<countEdges; y++)
                {
                    secondEdge = edges.get(y);
                    compareSource = secondEdge.getSource();
                    compareTarget = secondEdge.getTarget();

                    if(source==compareSource || target==compareTarget || source==compareTarget || target==compareSource) // sharing a keystone-LCNode
                    {
                        // Who is who?
                        if(source==compareSource)
                        {
                            keystone = source;
                            firstEdgeLCNode = target;
                            secondEdgeLCNode = compareTarget;
                        }
                        else if(target==compareTarget)
                        {
                            keystone = target;
                            firstEdgeLCNode = source;
                            secondEdgeLCNode = compareSource;
                        }
                        else if(source==compareTarget)
                        {
                            keystone = source;
                            firstEdgeLCNode = target;
                            secondEdgeLCNode = compareSource;
                        }
                        else if(target==compareSource)
                        {
                            keystone = target;
                            firstEdgeLCNode = source;
                            secondEdgeLCNode = compareTarget;
                        }
                        
                        ArrayList<LCNode> firstNeighbours = currentGraph.getNeighbors(firstEdgeLCNode);
                        ArrayList<LCNode> secondNeighbours = currentGraph.getNeighbors(secondEdgeLCNode);
                        countFirstNeighbours = firstNeighbours.size();
                        countSecondNeighbours = secondNeighbours.size();
                        commonNeighbours = 0;
                        //Calculating common Neighbours
                        for(LCNode eOne: firstNeighbours)
                        {
                            if(eOne == secondEdgeLCNode){triangle = true;}
                            for(LCNode eTwo : secondNeighbours)
                            {
                                if(eOne == eTwo) // There should be at least one: The keystoneLCNode!
                                    commonNeighbours++;
                            }
                        }
                        // Calcualting all Neighbours
                        allNeighbours = countFirstNeighbours + countSecondNeighbours - commonNeighbours + 2; // Common Neighbours are double, eOne and eTwo add
                        // It it is a traingle I need to take a look at eOne and eTwo.
                        if(triangle)
                        {
                            commonNeighbours = commonNeighbours+2;
                            allNeighbours = allNeighbours -2;
                        }
                        
                        float dummyCommon = commonNeighbours;
                        float dummyAll = allNeighbours;
                        similarity = dummyCommon/dummyAll;
                        //report = report + "\nCommon Neighbours: " + commonNeighbours + ", all Neighbours: " + allNeighbours + ", similarity: " + similarity;
                        Edgepair tempEdgePair = new Edgepair(firstEdge, secondEdge, similarity, keystone);
                        similarityMatrix.add(tempEdgePair);
                        triangle = false;
                    }
                    // else not a connected pair
                }
            }

// Sorts the List by similarity. NOTE: Highest values first!
            Collections.sort(similarityMatrix);
            
            
            // Just debugging here.
            String ordertest= "Anzahl Edgepairs: "+ similarityMatrix.size() + "\nEdges Ordered by similarity:\n";
            for(Edgepair testpair:similarityMatrix)
            {
                ordertest = ordertest + testpair.getSimilarity()+", ";
            }
            //report = report + ordertest;
            

// Calculating clusteringSteps
            ClusteringStep tempCluster = new ClusteringStep();
            float sim = similarityMatrix.get(0).getSimilarity();
            Edgepair tempPair;
            LCEdge tempEdgeOne;
            LCEdge tempEdgeTwo;
            int tempEdgeOneIndex = -1;
            int tempEdgeTwoIndex = -1;
            int step = 0;
            HashSet<LCEdge> tempCommunity;
            
// Initializing first ClusteringStep/Leafs of the dendogram: Every link has its own community
            tempCluster.setClusteringStep(0);
            for(LCEdge everyEdge: edges)
            {
                HashSet<LCEdge> perEdge = new HashSet<LCEdge>();
                perEdge.add(everyEdge);
                tempCluster.add(perEdge);
            }
            tempCluster.calculatePartitioningDensity(countEdges);
            // report = report + "\nAnzahl Edges: " + countEdges + "\nStarting Partitioning Density: " + tempCluster.getPartitioningDensity();
            float comparePD = tempCluster.getPartitioningDensity(); // First comparable Partitioning Desity value
            
            
            while(!similarityMatrix.isEmpty()) // Do this until all communities are merged.
            {
                tempPair = similarityMatrix.get(0);
                tempEdgeOne = tempPair.getEdges()[0];
                tempEdgeTwo = tempPair.getEdges()[1];

                if(tempPair.getSimilarity() == sim) // then I am still doing the same clustering step
                {
                    // Get communities of the Edges in the Edgepair
                    for(int i=0; i<tempCluster.size(); i++)
                    {
                        tempCommunity = tempCluster.get(i);
                        if(tempCommunity.contains(tempEdgeOne))
                        {
                            tempEdgeOneIndex = i;
                            break;
                        }
                    }
                    for(int i=0; i<tempCluster.size(); i++)
                    {
                        tempCommunity = tempCluster.get(i);
                        if(tempCommunity.contains(tempEdgeTwo))
                        {
                            tempEdgeTwoIndex = i;
                            break;
                        }
                    }
                    
                    if(tempEdgeOneIndex != tempEdgeTwoIndex) // Not already in the same community
                    {
                        // report = report + "\n Indices der gemergten communities: " + tempEdgeOneIndex + ", " + tempEdgeTwoIndex;
                        tempCluster.get(tempEdgeOneIndex).addAll(tempCluster.get(tempEdgeTwoIndex)); // Merge communities
                        tempCluster.remove(tempEdgeTwoIndex); // Delete Merged Community
                        /*
                        for(HashSet<Edge> set : tempCluster) // For debugging purposes only: Works so far!
                        {
                            report = report + "\n";
                            for(Edge e : set)
                            {
                                report = report + e.getId() + ", ";
                            }
                        }
                        */
                    }
                    similarityMatrix.remove(0); // Remove Edgepair from similarityMatrix to look at the next Pair
                }
                else // Clustering Step is done
                {
                    step++;
                    tempCluster.setClusteringStep(step);
                    tempCluster.calculatePartitioningDensity(countEdges);
                    /*
                    report = report + "\nCommunities nach dem Berechnen der PD: \n";
                    for(HashSet<Edge> set : tempCluster) // For debugging purposes only: Works so far!
                        {
                            report = report + "\n";
                            for(Edge e : set)
                            {
                                report = report + e.getId() + ", ";
                            }
                        }
                    */
                    // report = report + "\nStep: " + tempCluster.getClusteringStep() + ", Anzahl der Communities: " + tempCluster.size() + ", Partitioning Density: " + tempCluster.getPartitioningDensity() + "\n"+ tempCluster.LCNodesUndEdges;

                    if(tempCluster.getPartitioningDensity() > comparePD)
                    {
                        comparePD = tempCluster.getPartitioningDensity();
                        maxPD.copyStep(tempCluster);
                    }

                    sim = tempPair.getSimilarity();
                    // Not removing Edgepair, because I haven't used it yet.
                }
            }
            // Calculating lastStep, because I left the loop with an equal sim.
            step++;
            tempCluster.setClusteringStep(step);
            tempCluster.calculatePartitioningDensity(countEdges);
            /*
            report = report + "\nCommunities nach dem Berechnen der PD: \n";
            for(HashSet<Edge> set : tempCluster) // For debugging purposes only: Works so far!
                {
                    report = report + "\n";
                    for(Edge e : set)
                    {
                        report = report + e.getId() + ", ";
                    }
                }
            */

            if(tempCluster.getPartitioningDensity() > comparePD)
            {
                maxPD.copyStep(tempCluster);
            }
            
// Now we know the clustering step with the highest Partitioning density.
            /*
            report = report + "\nLink Communities with the highest Partitioning Density: " + maxPD.getPartitioningDensity() + "\n";
            for(HashSet<Edge> set : maxPD)
            {
                report = report + "\n";
                for (Iterator<Edge> it = set.iterator(); it.hasNext();)
                {
                    Edge e = it.next();
                    report = report + e.getId() + ", ";
                }
            }
            */
            
// New write out results.
            
            // setting a communityMembership for each edge
            int communityNo = 0;
            for(HashSet<LCEdge> community : maxPD)
            {
                if(community.size() > 2) // Just nontrivial communities. For all others default value.
                {
                    communityNo++;
                    for(LCEdge e : community)
                    {
                        e.setCommunityMembership(communityNo);
                        //System.out.println("Edge " + e.getID() + " community " + e.getCommuntiyMembership());
                        
                        // Setting community membership for nodes
                        e.getSource().setCommunityMembership(communityNo);
                        e.getTarget().setCommunityMembership(communityNo);
                    }
                }
            }  
        }
    }
}
