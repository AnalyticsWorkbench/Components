package eu.sisob.components.framework.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Representation used for storing information of DynetML/CoNaVi file.
 * 
 * @author Per Verheyen
 */

public class SomeNode implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 2131048421576599355L;

	public enum NODETYPE {
        Agent,
        Knowledge, 
        Resource,
        Partition
    }

    private String id;

    private NODETYPE nodeType;

    private ArrayList<String> liveTime;

    private HashMap<String,String> edges;
    
//    private ArrayList<ArrayList<String>> edges;
//
//    private ArrayList<String> toId;
//
//    private ArrayList<String> time;

    /**
     * @param id
     *            - name
     */
    public SomeNode(String id, NODETYPE nType) {
        this.id = id;
        this.nodeType = nType;
//        edges = new ArrayList<ArrayList<String>>();
//        toId = new ArrayList<String>();
//        time = new ArrayList<String>();
        edges = new HashMap<String, String>();
//        edges.add(0, toId);
//        edges.add(1, time);
        liveTime = new ArrayList<String>();

    }

    /**
     * Adds edge to PersonNode
     * 
     * @param id
     *            - name
     * @param time
     *            - timestamp
     */
    public void addEdge1(String id, String time) {
//        edges.get(0).add(id);
//        edges.get(1).add(time);
        edges.put(id, time);
    }

    /**
     * Adds timestamps to PersonNode
     * 
     * @param lt
     */
    public void addLiveTimes(String lt) {
        liveTime.add(lt);
    }

    /**
     * Returns name.
     * 
     * @return id
     */
    public String getId() {
        return id;
    }

//    /**
//     * Returns edges connected to PersonNode.
//     * 
//     * @return edges
//     */
//    public ArrayList<ArrayList<String>> getEdges() {
//        return edges;
//    }
    public HashMap<String, String> getEdges(){
        return edges;
    }

    /**
     * Returns time stamps for PersonNode.
     * 
     * @return liveTime
     */
    public ArrayList<String> getLiveTimes() {
        return liveTime;
    }

    public NODETYPE getNodeType() {
        return nodeType;
    }

    public boolean compareToOtherNode(SomeNode otherNode) {
        if (this.id.equals(otherNode.getId())) {
            return true;
        } else {
            return false;
        }
    }

}
