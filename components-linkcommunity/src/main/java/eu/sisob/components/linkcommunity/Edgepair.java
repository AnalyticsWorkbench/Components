package eu.sisob.components.linkcommunity;
/** An edgepair consists of two edges, sharing a common node, the keystone node.
 * It has a similarity value calculated after the Jaquard index.
 * 
 * @author Evelyn Fricke
 */
public class Edgepair implements Comparable<Edgepair>
{
	private float similarity;
    private LCEdge[] pair = new LCEdge[2];
    private LCNode keystone;
    
    public Edgepair(LCEdge one, LCEdge two, float similarity, LCNode keystone)
    {
        if(one==null|| two==null)
        {
            throw new NullPointerException();
        }
        this.pair[0]=one;
        this.pair[1]=two;
        this.similarity=similarity;
        this.keystone=keystone;
    }
    
    /** Implements the compatreTo method of Comparable.
     * NOTE: This comparison will put the pair with the highest similarity first!
     * Reversed natural Ordering!
     */
    public int compareTo(Edgepair cmpPair)
    {
        Float thisSim = new Float(this.similarity);
        Float cmpPairSim = new Float(cmpPair.getSimilarity());
        int cmpResult = cmpPairSim.compareTo(thisSim);
        return cmpResult;
    }
 
    // Setter and Getter
    
    /** @return the two edges making up the pair as an array.
     */
    public LCEdge[] getEdges(){return pair;}
    
    /**The Similarity of the edgepair is calculated after the Jaquard index.
     * It is the intersection of the neighbours of the nodes, which are not the keystone, including the nodes themselves 
     * divieded by their union.
     * @return float similarity of the degepair
     */
    public float getSimilarity(){return this.similarity;}
    
    /** An edgepair always has a common node, called the keystone node.
     * @return Node keystone  node
     */
    public LCNode getKeystoneNode(){return this.keystone;}
}
