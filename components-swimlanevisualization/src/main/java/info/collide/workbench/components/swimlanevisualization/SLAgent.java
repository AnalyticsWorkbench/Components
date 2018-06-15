/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.collide.workbench.components.swimlanevisualization;

import com.google.gson.JsonObject;
import eu.sisob.api.parser.NetworkParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.api.visualization.VisualizationAgent;
import eu.sisob.components.framework.SISOBProperties;
import eu.sisob.components.framework.json.util.JSONFile;
import java.io.File;

/**
 *
 * @author hecking
 */
public class SLAgent extends VisualizationAgent {

    private static final String TECHNIQUE_ZIP_PATH = 
            "info/collide/workbench/components/swimlanevisualization/technique.zip";
    private SwimLaneTechnique technique;
    private String path;
    
    private String xParameter;
    private String yParameter;
    
    public static final String SL_HOME = SISOBProperties.getThirdPartyPath() + "sl" + File.separator;

    public SLAgent(JsonObject coordinationMessage) {

        super(coordinationMessage);
        
        this.xParameter = this.getFilterParameters().get(SLAgentManager.X_PARAM).toString();
        this.yParameter = this.getFilterParameters().get(SLAgentManager.Y_PARAM).toString();
    }

    @Override
    public String getZipPath() {
        return TECHNIQUE_ZIP_PATH;
    }

    @Override

    public void createVisualization() throws Exception {

        NetworkParser parser;
        
        for (int i = 0; i < this.getJsonNetworks().size(); i++) {

            JSONFile network = this.getJsonNetworks().get(i);

            parser = new SGFParser();

            parser.setNetwork(network);

            parser.parse();
            
            technique = new SwimLaneTechnique(network, this.yParameter, this.xParameter);

            technique.applyLayout();

            parser.updateMetadata(technique.updatedMetadata());

            parser.updateNodeSet(technique.updatedNodeSet());

            network.setTextContent(parser.encode());

        }

        super.createVisualization();
        
    }

}
