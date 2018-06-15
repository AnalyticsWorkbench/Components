package eu.sisob.components.framework;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author Per Verheyen
 * @author Alfredo Ramos
 * 
 */
public class FileHelper {

	public static final String FILE_SEPERATOR = File.separator;
	public static final String THIRDPARTY_HOME = SISOBProperties
			.getThirdPartyPath();

	/**
	 * Copies file from input path to output path.
	 * 
	 * @param inputPath
	 * @param outputPath
	 */
	public static void copyFile(String inputPath, String outputPath) {
		File inputFile = new File(inputPath);
		File outputFile = new File(outputPath);

		try {
			BufferedInputStream input = new BufferedInputStream(
					new FileInputStream(inputFile));

			BufferedOutputStream output = new BufferedOutputStream(
					new FileOutputStream(outputFile));

			int c;

			while ((c = input.read()) != -1) {
				output.write(c);
			}
			input.close();
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	

	/**
	 * Creates ArrayList (byte[]) from files in path
	 * 
	 * @param path
	 *            - source path of files
	 * @return ArrayList<byte[]>
	 */
	public static ArrayList<byte[]> readImagesAsByteArray(String path) {

		ArrayList<byte[]> results = new ArrayList<byte[]>();

		try {
			for (int i = 0; i < new File(path).listFiles().length; i++) {

				File f = new File(path).listFiles()[i];
				byte[] data = new byte[(int) f.length()];
				InputStream inStream = new FileInputStream(f);

				inStream.read(data);
				inStream.close();
				results.add(data);
			}
		} catch (Exception g) {
			g.printStackTrace();
		}

		return results;
	}	
	

	public static ArrayList<HashMap<byte[], String>> readImagesAsMapByteArray(
			String path) {

		ArrayList<HashMap<byte[], String>> results = new ArrayList<HashMap<byte[], String>>();

		try {
		    File[] dirListing = new File(path).listFiles();
	            Arrays.sort(dirListing);
	            for (int i = 0; i < dirListing.length; i++) {

	                File f = dirListing[i];
	                HashMap<byte[], String> dataMap = new HashMap<byte[], String>();
	                byte[] data = new byte[(int) f.length()];
	                String fileName = f.getName();
	                InputStream inStream = new FileInputStream(f);
	                inStream.read(data);
	                inStream.close();
	                dataMap.put(data, fileName);

	                results.add(dataMap);
	            }
		} catch (Exception g) {
			g.printStackTrace();
		}

		return results;
	}
	
	public static String readPajekResultFilesAsText(String path)
	{
		
		File dir = new File(path);
		BufferedReader reader;
		String line;
		StringBuilder fileContent = new StringBuilder();
		
		
		try 
		{
			for (int i = 0; i < dir.listFiles().length; i++)
			{  
				if(dir.listFiles()[i].getName().contains(".bmp") == false)
				{
					reader = new BufferedReader(new FileReader(dir.listFiles()[i]));
					line = null;
					fileContent.append(dir.listFiles()[i].getName());
					fileContent.append("|");
						while ((line = reader.readLine()) != null) 
						{
							fileContent.append(line);
							fileContent.append(System.getProperty("line.separator"));
						}
						fileContent.append("@@");
						reader.close();
				}
			}
		}
		catch (Exception g){ 
			g.printStackTrace();
			return null;
		}
		
		if (fileContent.toString().length()>0)
			return fileContent.toString();
		else return null;
		
	}

	/**
	 * Modifies path information if needed for actual operating system
	 * 
	 * @param path
	 * @return
	 */
	public static String pathConverter(String path) {
		String resultPath = "";

		if (path.contains("/") && !(AgentProperties.SEPERATOR.equals("/")))
			resultPath = path.replace("/", AgentProperties.SEPERATOR);
		else if (path.contains("\\")
				&& !(AgentProperties.SEPERATOR.equals("\\")))
			resultPath = path.replace("\\", AgentProperties.SEPERATOR);
		else
			resultPath = path;

		return resultPath;
	}
	
	public static ArrayList<String> readNetworks(String path){
		ArrayList<String> networks = new ArrayList<String>();
		try {
			for (int i = 0; i < new File(path).listFiles().length; i++) {
				StringBuilder fileContent = new StringBuilder();

				File f = new File(path).listFiles()[i];
				if(f.isFile()){
					BufferedReader reader = new BufferedReader(new FileReader(f));
    				String line = null;
    				
    				while ((line = reader.readLine()) != null) 
    				{
    					fileContent.append(line);
    					if(i==0){
    						fileContent.append(System.getProperty("line.separator"));
    					}
    				}        				
    				reader.close();
    				
    				networks.add(fileContent.toString());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
				
		
		return networks;
	}
	public static void deleteDirectories( File dir )
	  {
	    for ( File file : dir.listFiles() )
	    {
	      if ( file.isDirectory() )
	        deleteDirectories( file );
	      file.delete();
	    }
	    dir.delete();
	  }
	  
	
}
