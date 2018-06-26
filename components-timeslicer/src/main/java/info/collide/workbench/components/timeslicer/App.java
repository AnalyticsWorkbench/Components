package info.collide.workbench.components.timeslicer;

import com.google.gson.JsonObject;
import eu.sisob.api.parser.pajek.PajekParser;
import eu.sisob.api.parser.sisob.SGFParser;
import eu.sisob.components.framework.graph.parser.FormatFactory;
import eu.sisob.components.framework.json.util.IllegalContentTypeException;
import eu.sisob.components.framework.json.util.JSONFile;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try {
            JsonObject params = new JsonObject();
            JsonObject msg = new JsonObject();
            Vector<JSONFile> files = new Vector<JSONFile>();
            JSONFile file = new JSONFile(
                    new File("C:\\Users\\hecking\\sonstige Forschung\\moodle_data\\propa_semester.net"), true);
            
            files.add(file);
            
            FormatFactory ff = new FormatFactory(files);
            
            ff.convertNetworkFormats(new PajekParser(), new SGFParser());
            
            params.addProperty(TimeSlicerAgentManager.BEGIN, "94");
            params.addProperty(TimeSlicerAgentManager.END, "199");
            params.addProperty(TimeSlicerAgentManager.WINDOW, "7");
            params.addProperty(TimeSlicerAgentManager.INCLUDE_ISOLATES, false);
            msg.add("parameters", params);
            
            Vector<JSONFile> res = new TimeSlicerAgent(msg).testAgent(ff.getNetworks().firstElement());
            
//            ff = new FormatFactory(res);
//            ff.convertNetworkFormats(new SGFParser(), new GMLNetworkParser());
            
            JSONFile.writeJSONFileSet(res, "C:\\Users\\hecking\\sonstige Forschung\\com_tracing");            
        } catch (IllegalContentTypeException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
