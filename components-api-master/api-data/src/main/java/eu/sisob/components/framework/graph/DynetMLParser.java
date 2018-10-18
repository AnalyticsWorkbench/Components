package eu.sisob.components.framework.graph;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.sisob.components.framework.graph.SomeNode.NODETYPE;

/**
 * @author verheyen
 * @author ramos
 * @author goehnert
 */
public class DynetMLParser {

    public enum Networktype {
        AgentAgent,
        AgentKnowledge,
        KnowledgeKnowledge,
        ResourceResource,
        ResourceKnowledge,
        AllNetworks
    };

    /**
     * @param networkXML
     *            - source path containing dynetml file
     * @param nwt
     *            - type of network (AgentAgent / AgentKnowledge)
     * @return - ArrayList<SomeNode> containing information about nodes and edges of dynetml file.
     */
    public static List<SomeNode> parseDynetML(String networkXML, Networktype nwt) {
        System.out.println("START!");
        List<SomeNode> nodeList = null;
        try {
            nodeList = dynetMLParser(networkXML, nwt);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return nodeList;
    }

    /**
     * Parses the DyNetML file in the specified path.
     * 
     * @param networkDynetMl
     * @return A list of PersonNodes.
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    private static List<SomeNode> dynetMLParser(String networkDynetMl, Networktype nwt) throws SAXException, IOException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        DefaultHandler handler = new CHandler(nwt);

        // alfred's quick fix
        // String result = new String();
        StringWriter sw = new StringWriter();


        FileInputStream fstream = new FileInputStream(networkDynetMl);
        DataInputStream in = new DataInputStream(fstream);
        // Get the object of DataInputStream
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        // Read File Line By Line

        while ((strLine = br.readLine()) != null) {
            // Print the content on the console
            if (!strLine.trim().isEmpty()) {
                sw.append(strLine);
            }
        }
        String result = sw.toString();
        sw.close();
        br.close();
        StringReader reader = new StringReader(result);
        List<SomeNode> resultList = new LinkedList<SomeNode>();

        saxParser.parse(new InputSource(reader), handler);

        // if (nwt.equals(Networktype.AgentAgent) || nwt.equals(Networktype.AgentKnowledge)) {
        // resultList = ((CHandler) handler).getPersonList();
        // } else if (nwt.equals(Networktype.KnowledgeKnowledge)) {
        // resultList = ((CHandler) handler).getKnowledgeList();
        // }
        // if (nwt.equals(Networktype.ResourceKnowledge) || nwt.equals(Networktype.ResourceResource)) {
        // resultList = ((CHandler) handler).getResourceList();
        // }

        if (nwt.equals(Networktype.AgentAgent) || nwt.equals(Networktype.AgentKnowledge)) {
            resultList.addAll(((CHandler) handler).getPersonList());
        }
        if (nwt.equals(Networktype.KnowledgeKnowledge) || nwt.equals(Networktype.AgentKnowledge) || nwt.equals(Networktype.ResourceKnowledge)) {
            resultList.addAll(((CHandler) handler).getKnowledgeList());
        }
        if (nwt.equals(Networktype.ResourceResource) || nwt.equals(Networktype.ResourceKnowledge)) {
            resultList.addAll(((CHandler) handler).getResourceList());
        }

        return resultList;

    }

    /**
     * Creates list entries (SomeNode).
     * 
     * @author Verheyen
     * 
     */
    private static class CHandler extends DefaultHandler {

        private Networktype nwt;

        private SomeNode currentNode;

        private List<SomeNode> personList;
        private List<SomeNode> knowledgeList;
        private List<SomeNode> resourceList;

        private boolean inSources = false;

        private boolean inNodeActors = false;
        private boolean inNodeKnowledge = false;
        private boolean inNodeResource = false;

        private boolean inAgentAgentNW = false;
        private boolean inAgentKnowledgeNW = false;
        private boolean inKnowledgeKnowledgeNW = false;
        private boolean inResourceResourceNW = false;
        private boolean inResourceKnowledgeNW = false;

        private String time;

        public CHandler(Networktype nwt) {
            this.nwt = nwt;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            personList = new LinkedList<SomeNode>();
            knowledgeList = new LinkedList<SomeNode>();
            resourceList = new LinkedList<SomeNode>();
            time = "";
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            super.startElement(namespaceURI, localName, qName, atts);

            if (qName.equals("sources")) {
                inSources = true;
            }

            else if (qName.equals("nodeclass")) {
                if (nwt.equals(Networktype.AgentAgent) && atts.getValue(0).equals("Agent") && atts.getValue(1).equals("Agent")) {
                    inNodeActors = true;
                } else if (nwt.equals(Networktype.AgentKnowledge) && atts.getValue(0).equals("Agent") && atts.getValue(1).equals("Agent")) {
                    inNodeActors = true;
                } else if (nwt.equals(Networktype.AgentKnowledge) && atts.getValue(0).equals("Knowledge") && atts.getValue(1).equals("Knowledge")) {
                    inNodeKnowledge = true;
                } else if (nwt.equals(Networktype.KnowledgeKnowledge) && atts.getValue(0).equals("Knowledge") && atts.getValue(1).equals("Knowledge")) {
                    inNodeKnowledge = true;
                } else if (nwt.equals(Networktype.ResourceKnowledge) && atts.getValue(0).equals("Knowledge") && atts.getValue(1).equals("Knowledge")) {
                    inNodeKnowledge = true;
                } else if (nwt.equals(Networktype.ResourceResource) && atts.getValue(0).equals("Resource") && atts.getValue(1).equals("Resource")) {
                    inNodeResource = true;
                } else if (nwt.equals(Networktype.ResourceKnowledge) && atts.getValue(0).equals("Resource") && atts.getValue(1).equals("Resource")) {
                    inNodeResource = true;
                }
            }

            else if (qName.equals("node")) {
                if (inNodeActors) {
                    currentNode = new SomeNode(atts.getValue(0), NODETYPE.Agent);
                } else if (inNodeKnowledge) {
                    currentNode = new SomeNode(atts.getValue(0), NODETYPE.Knowledge);
                } else if (inNodeResource) {
                    currentNode = new SomeNode(atts.getValue(0), NODETYPE.Resource);
                }

            }

            else if (qName.equals("source")) {
                if (inSources) {
                    String tempDateString = atts.getValue(0);
                    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                    cal.set(Integer.valueOf(tempDateString.substring(0, 4)), Integer.valueOf(tempDateString.substring(4, 6)) - 1, Integer.valueOf(tempDateString.substring(6, 8)));
                    time = String.valueOf(cal.getTimeInMillis()) + "_000";                  
                }
            }

            else if (qName.equals("network")) {
                if (nwt.equals(Networktype.AgentAgent) && atts.getValue(4).equals("Agent x Agent")) {
                    inAgentAgentNW = true;
                } else if (nwt.equals(Networktype.AgentKnowledge) && atts.getValue(4).equals("Agent x Knowledge") || atts.getValue(4).equals("Knowledge x Agent")) {
                    inAgentKnowledgeNW = true;
                } else if (nwt.equals(Networktype.KnowledgeKnowledge) && atts.getValue(4).equals("Knowledge x Knowledge")) {
                    inKnowledgeKnowledgeNW = true;
                } else if (nwt.equals(Networktype.ResourceResource) && atts.getValue(4).equals("Resource x Resource")) {
                    inResourceResourceNW = true;
                } else if (nwt.equals(Networktype.ResourceKnowledge) && atts.getValue(4).equals("Resource x Knowledge") || atts.getValue(4).equals("Knowledge x Resource")) {
                    inResourceKnowledgeNW = true;
                }

            } else if (qName.equals("link")) {
                
                String sourceId = atts.getValue(0);
                String destId = atts.getValue(1);
                boolean success = false;
                
                if (inAgentAgentNW) {
                    addEdge(personList, personList, sourceId, destId);
                } else if (inAgentKnowledgeNW) {
                    success = addEdge(personList, knowledgeList, sourceId, destId);
                    if (!success) {
                        addEdge(knowledgeList, personList, sourceId, destId);
                    }
                } else if (inKnowledgeKnowledgeNW) {
                    addEdge(knowledgeList, knowledgeList, sourceId, destId);
                } else if (inResourceKnowledgeNW) {
                    success = addEdge(resourceList, knowledgeList, sourceId, destId);
                    if (!success) {
                        addEdge(knowledgeList, resourceList, sourceId, destId);
                    }
                } else if (inResourceResourceNW) {
                    addEdge(resourceList, resourceList, sourceId, destId);
                }                
         
            }

        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            super.endElement(namespaceURI, localName, qName);

            if (qName.equals("sources")) {
                if (inSources) {
                    inSources = false;
                }
            }

            else if (qName.equals("nodeclass")) {
                if (inNodeActors) {
                    inNodeActors = false;
                } else if (inNodeKnowledge) {
                    inNodeKnowledge = false;
                } else if (inNodeResource) {
                    inNodeResource = false;
                }
            }

            else if (qName.equals("node")) {             

                if (inNodeActors) {
                    currentNode.addLiveTimes(time);
                    personList.add(currentNode);
                } else if (inNodeKnowledge) {
                    currentNode.addLiveTimes(time);
                    knowledgeList.add(currentNode);
                } else if (inNodeResource) {
                    currentNode.addLiveTimes(time);
                    resourceList.add(currentNode);
                }
            }

            else if (qName.equals("network")) {
                if (inAgentAgentNW) {
                    inAgentAgentNW = false;
                } else if (inAgentKnowledgeNW) {
                    inAgentKnowledgeNW = false;
                } else if (inKnowledgeKnowledgeNW) {
                    inKnowledgeKnowledgeNW = false;
                } else if (inResourceResourceNW) {
                    inResourceResourceNW = false;
                } else if (inResourceKnowledgeNW) {
                    inResourceKnowledgeNW = false;
                }
            }

        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();

            System.out.println("' pNodes: " + personList.size());
            System.out.println("' kNodes: " + knowledgeList.size());
            System.out.println("' rNodes: " + resourceList.size());
        }

        public List<SomeNode> getPersonList() {
            // for (SomeNode node : personList) {
            // System.out.println("NODE: " + node.getId() + " TYPE: " + node.getNodeType());
            // }

            return personList;
        }

        public List<SomeNode> getKnowledgeList() {
            // for (SomeNode node : knowledgeList) {
            // System.out.println("NODE: " + node.getId() + " TYPE: " + node.getNodeType());
            // }

            return knowledgeList;
        }

        public List<SomeNode> getResourceList() {
            // for (SomeNode node : resourceList) {
            // System.out.println("NODE: " + node.getId() + " TYPE: " + node.getNodeType());
            // }

            return resourceList;
        }
        
        private boolean addEdge(List<SomeNode> sourceList, List<SomeNode> destList, String sourceId, String destId) {
            if (sourceId.equals(destId)!=true) {
                for (SomeNode sourceNode : sourceList) {
                    if (sourceNode.getId().equals(sourceId)) {
                        for (SomeNode destNode : destList) {
                            if (destNode.getId().equals(destId)) {
                                sourceNode.addEdge1(destId, time);
                                System.out.println("Link stablished between: "+ sourceId +" and "+destId);
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
    }
}
