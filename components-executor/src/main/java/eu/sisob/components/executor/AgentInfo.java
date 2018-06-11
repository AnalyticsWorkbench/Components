package eu.sisob.components.executor;


public class AgentInfo {
    
    String filterName;
    String managerName;
    String managerClassName;
    boolean autostart;
    
    /**
     * Constructor for the AgentInfo class with full configuration options.
     * 
     * @param filter The name of the filter as it is shown in the WireIt UI
     * @param manager The name that is to be displayed for the manager.
     * @param managerClass The fully qualified class name of the manager class.
     * @param autostart Boolean to determine if components should start automatically or not.
     */
    public AgentInfo(String filter, String manager, String managerClass, boolean autostart) {
        this.filterName = filter;
        this.managerName = manager;
        this.managerClassName = managerClass;
        this.autostart = autostart;
    }
    
    public String getFilterName() {
        return filterName;
    }

    
    public String getManagerName() {
        return managerName;
    }

    
    public String getManagerClassName() {
        return managerClassName;
    }
    
    public boolean isAutostart() {
        return autostart;
    }

}
