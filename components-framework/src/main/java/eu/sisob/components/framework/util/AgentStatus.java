package eu.sisob.components.framework.util;

import java.util.HashMap;

public enum AgentStatus {

    WAITING(1),
    RUNNING(2),
    DONE(3),
    ERROR(5),
    FINISH(10);

    private int statuscode;

    private static HashMap<Integer, AgentStatus> MAP = new HashMap<Integer, AgentStatus>(10);

    static {
        for (AgentStatus v : AgentStatus.values()) {
            MAP.put(v.getId(), v);
        }
    }

    AgentStatus(int i) {
        this.statuscode = i;

    }

    public Integer getId() {
        return statuscode;
    }

    public static AgentStatus fromId(int i) {
        return MAP.get(i);
    }

}
