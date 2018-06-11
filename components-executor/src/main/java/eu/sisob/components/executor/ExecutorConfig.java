package eu.sisob.components.executor;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class ExecutorConfig extends DefaultHandler {
    
    List<AgentInfo> agentInfos;
    
    public ExecutorConfig() {
        agentInfos = new LinkedList<AgentInfo>();
    }
    
    public ExecutorConfig(String configFileName) throws ParserConfigurationException, SAXException, IOException {
        this();
        parseConfig(configFileName);
    }
    
    public void parseConfig(String fileName) throws ParserConfigurationException, SAXException, IOException {
        this.parseConfig(new File(fileName));
    }
    
    public void parseConfig(File file) throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(file, this);
    }
    
    public List<AgentInfo> getAgentInfos() {
        return agentInfos;
    }
    
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        if (qName.equalsIgnoreCase("AgentInfo")) {
            String filter = atts.getValue("filter");
            String manager = atts.getValue("manager");
            String className = atts.getValue("class");
            String autoStartAtt = atts.getValue("start");
            boolean autoStart = false;
            if (autoStartAtt != null) {
                try {
                    autoStart = Boolean.parseBoolean(autoStartAtt);
                } catch (Exception ex) {
                    autoStart = false;
                }
            }
            AgentInfo info = new AgentInfo(filter, manager, className, autoStart);
            agentInfos.add(info);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Starting Parser");
        try {
            ExecutorConfig config = new ExecutorConfig();
            config.parseConfig("test.xml");
            for (AgentInfo agent : config.getAgentInfos()) {
                System.out.println("Found agent:");
                System.out.println("Class: " + agent.getManagerClassName());
                System.out.println("Manager: " + agent.getManagerName());
                System.out.println("Filter: " + agent.getFilterName());
                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Done");
    }

}
