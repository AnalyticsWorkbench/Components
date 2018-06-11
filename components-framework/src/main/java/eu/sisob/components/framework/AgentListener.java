package eu.sisob.components.framework;


/**
 * Listener used to inform the manager about status changes of specific agent 
 * @author Verheyen, Ramos
 */
public interface AgentListener {

    public void notifyManager(Agent agent);

}
