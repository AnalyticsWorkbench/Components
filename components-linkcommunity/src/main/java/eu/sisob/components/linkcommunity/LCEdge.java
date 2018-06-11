package eu.sisob.components.linkcommunity;
import java.util.HashMap;

/** Representing a directed or undirected edge in a network.
 * 
 * @author Evelyn Fricke
 */

public class LCEdge
{
	private String ID;
	private LCNode target;
	private LCNode source;
	private int communityMembership;
	
	public LCEdge(){}
	public LCEdge(LCNode src, LCNode trg, String id)
	{
		this.source = src;
		this.target = trg;
		this.ID = id;
	}
	
	/**
	 * Returns the target LCNode of this edge.
	 * @return target LCNode
	 * @see LCNode
	 */
	public LCNode getTarget(){return target;}
	
	/**
	 * Returns the source LCNode of this edge.
	 * @return source LCNode
	 * @see LCNode
	 */
	public LCNode getSource(){return source;}
	
	/**
	 * Returns identification number of the edge
	 * @return id
	 */
	public String getID(){return ID;}
	
	/**
	 * Sets the number of the community, this edge belongs to.
	 * 0 is reserved for none clustered edges.
	 * @param community number
	 */
	public void setCommunityMembership(int cm){communityMembership = cm;}
	
	/**
	 * Returns the number of the community, that this edge belongs to.
	 * If this edge has not been clustered, 0 is returned.
	 * @return number of community
	 */
	public int getCommuntiyMembership(){return communityMembership;}
}
