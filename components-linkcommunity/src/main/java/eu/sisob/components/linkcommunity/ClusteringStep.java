package eu.sisob.components.linkcommunity;

import java.util.HashSet;
import java.util.ArrayList;
/**
 * Clustering Step is an ArrayList of link communities of a graph.
 * It can calculate its on partitioning density and has a identifying number.
 * 
 * @see Graph
 * @author Evelyn Fricke
 */

public class ClusteringStep extends ArrayList<HashSet<LCEdge>>
{
	private int stepNo = 0;
    private float partitioningDensity = -1;
    
    // public String nodesUndEdges = ""; // Just for debugging purposes.
    
    /**
     * The Clustering Step, on which this method is called, is filled with all the values of the given cluster.
     * @param cluster the given clustering step
     */
    public void copyStep(ClusteringStep cluster)
    {
        ClusteringStep makeCopy = cluster;
        this.stepNo = makeCopy.getClusteringStep();
        this.partitioningDensity = makeCopy.getPartitioningDensity();
        this.clear();
        for(HashSet<LCEdge> copySet : makeCopy)
        {
            HashSet<LCEdge> set = new HashSet<LCEdge>();
            for(LCEdge e : copySet)
            {
                set.add(e);
            }
            this.add(set);
        }
    }
    
    /**
     * Calculates the partitioning density of the clustering step.
     * @param edgeCount to calculate the partitioning density, you need to know the total number of the edges.
     */
    public void calculatePartitioningDensity(int edgeCount)
    {
        int allEdges = edgeCount;
        int nodesInSet;
        int edgesInSet;
        float dummyAll;
        float dummyNodes;
        float dummyEdges;
        float setResult = 0.0f;
        float sum = 0.0f;
        HashSet<LCNode> nodes = new HashSet<LCNode>();
        
        for (HashSet<LCEdge> set : this)
        {
            edgesInSet = set.size();
            for(LCEdge edge : set)
            {
                if(!nodes.contains(edge.getSource())){nodes.add(edge.getSource());} // if shouldn't be necessary!
                if(!nodes.contains(edge.getTarget())){nodes.add(edge.getTarget());} // if shouldn't be necessary!
            }
            nodesInSet = nodes.size();
            if(nodesInSet > 2) // nontrivial community, otherwise setresult = 0 anyway
            {
                dummyNodes = nodesInSet;
                dummyEdges = edgesInSet;
                setResult = dummyEdges*((dummyEdges-(dummyNodes-1.0f))/((dummyNodes-2.0f)*(dummyNodes-1.0f)));
                sum = sum + setResult;
            }
            // nodesUndEdges = nodesUndEdges + "\nAnzahl Kanten: "+edgesInSet+", Anzahl Knoten: "+nodesInSet+", set Density: "+setResult;
            nodes.clear();
        }
        dummyAll = allEdges;
        this.partitioningDensity = ((2.0f*sum)/dummyAll);
    }
    
    /**
     * Sets the identifying number of the clustering step.
     * @param no the identifying number
     */
    public void setClusteringStep(int no){this.stepNo = no;}
    
    /**
     * Returns the identifying number of the clustering step.
     * @return number of step
     */
    public int getClusteringStep(){return this.stepNo;}
    
    /**
     * Returns the partitioning density of this clustering step.
     * Returns -1 if the number has not been calculated yet.
     * Partitioning density has values from -2/3 to 1.
     * @return partitioning density of this clustering step.
     */
    public float getPartitioningDensity(){return this.partitioningDensity;}
}
