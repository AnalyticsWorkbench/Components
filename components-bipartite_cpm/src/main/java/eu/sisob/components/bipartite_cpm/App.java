package eu.sisob.components.bipartite_cpm;

import info.collide.util.ClassLoaderUtility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Hello world!
 *
 */
public class App 
{

    public static void main( String[] args ) throws IOException
    {
        InputStream scriptStream = ClassLoaderUtility
                .getClassLoader().getResourceAsStream("bipartite_cpm.R");
        BufferedReader reader = new BufferedReader(new InputStreamReader(scriptStream));
        String line = reader.readLine();
        System.out.println(line);
    }
    
}
