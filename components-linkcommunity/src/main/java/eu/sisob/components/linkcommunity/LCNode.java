package eu.sisob.components.linkcommunity;


import java.util.HashMap;
import java.util.HashSet;
/**
 * Represents a node in a graph.
 * Node should always have and id and may have a lable and a timestamp.
 * @author Evelyn Fricke
 */

public class LCNode
{
	private String lable;
	private String id;
	private int timeStamp;
	private HashSet<Integer> community = new HashSet<Integer>();
	
	public LCNode(String id, String lable)
	{
		this.id = id;
		this.lable = lable;
	}
	public LCNode(){}
	public LCNode(String id, String lable, int timestamp)
	{
		this.id = id;
		this.lable = lable;
		this.timeStamp = timestamp;
	}
	
	/**
	 * @return id of this node
	 */
	public String getID(){return id;}
	
	/**
	 * @return labla of this node
	 */
	public String getLable(){return lable;}
	
	/**
	 * @return timestamp for this node
	 */
	public int getTimestamp(){return timeStamp;}
	
	public void setCommunityMembership(Integer cm){community.add(cm);}
	
	public HashSet<Integer> getCommunityMemebership(){return community;}
}
