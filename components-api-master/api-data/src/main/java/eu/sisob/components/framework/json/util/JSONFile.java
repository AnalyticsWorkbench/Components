package eu.sisob.components.framework.json.util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Vector;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * JSONFile
 * no
 * A JSONFile object is a representation of a file that can be easily serialized
 * into a JSON compatible string format. The class also contains helper methods
 * to deserialize single files from JSON-Strings, actual files and to
 * deserialize file sets from String-encoded JSON arrays
 * 
 * @author ramos, charles
 * 
 */
public class JSONFile {

	public static final String BASE64 = "BASE64";
	public static final String TEXT = "TEXT";

	private String fileName;
	private String fileType;
	private String data;
	private String specialFileType;

	/**
	 * Constructs a JSONFile based on byte array data. The specialfiletype field
	 * will automatically be set to "BASE64"
	 * 
	 * @param filename
	 *            the file name
	 * @param fileType
	 *            the file extension
	 * @param data
	 *            the file contents
	 */
	public JSONFile(String filename, String fileType, byte[] data) {
		this.fileName = filename;
		this.fileType = fileType;
		this.data = DatatypeConverter.printBase64Binary(data);
		this.specialFileType = BASE64;
	}

	/**
	 * Constructs a JSONFile based on String data
	 * 
	 * @param filename
	 *            the file name
	 * @param fileType
	 *            the file extension
	 * @param data
	 *            the plain text string data
	 * @param specialFileType
	 *            the special file type (e.g. "BASE64")
	 */
	public JSONFile(String filename, String fileType, String data, String specialFileType) {
		this.fileName = filename;
		this.fileType = fileType;
		this.data = data;
		this.specialFileType = specialFileType;
	}

	/**
	 * Constructs a JSONFile object from an existing file. <br/> 
	 * Encodes the file content in base64 and sets special file
	 * type accordingly
	 * 
	 * @param file
	 *            the existing input file to read the JSONFile data from
	 */
	public JSONFile(File file) throws IOException {
		this(file, false);
	}
	
	/**
	 * Constructs a JSONFile object from an existing file. <br/> 
	 * Encodes the file content as text or base64 and sets special file
	 * type accordingly depending on the boolean flag.
	 * 
	 * @param file
	 *            the existing input file to read the JSONFile data from
	 * 
	 * @param text
	 * 			boolean for telling if the file should be handled as text (true) or not (false)
	 * 
	 * @throws IOException
	 */
	public JSONFile(File file, boolean text) throws IOException {
		String fullName = file.getName();
		int periodIndex = fullName.lastIndexOf(".");
		if (periodIndex > 0) {
			this.fileType = fullName.substring(periodIndex+1);
		} else {
			this.fileType = null;
		}
		this.fileName = fullName;
		
		/* read contents into a byte array */
		InputStream is = new FileInputStream(file);
		long length = file.length();
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		is.close();

		if (text) {
			this.data = new String(bytes, "UTF-8");
			this.specialFileType = TEXT;
		} else {
			this.data = DatatypeConverter.printBase64Binary(bytes);
			this.specialFileType = BASE64;
		}
	}
	
	/**
	 * Deserializes and constructs one JSONFile object from a String
	 * 
	 * @param jsonString
	 *            the input String
	 */
	public JSONFile(String jsonString) {
		this((JSONObject) JSONValue.parse(jsonString));
	}
	
	/**
	 * Constructs a JSONFile from a JSONObject
	 * @param jsonObject
	 */
	public JSONFile(JSONObject jsonObject) {
		this.fileName = jsonObject.get("filename").toString();
		this.fileType = jsonObject.get("filetype").toString();
		this.data = jsonObject.get("filedata").toString();
		this.specialFileType = jsonObject.get("specialfiletype").toString();
//		this.timeStamp = jsonObject.get("timestamp").toString();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getStringContent() throws IllegalContentTypeException {
		if (TEXT.equals(specialFileType)) {
			return data;
		} else {
			throw new IllegalContentTypeException(TEXT, specialFileType);
		}
	}
	
	public void setTextContent (String textContent) {
		this.data = textContent;
	}
	
	public byte[] getBinaryContent() throws IllegalContentTypeException {
		if (BASE64.equals(specialFileType)) {
			return DatatypeConverter.parseBase64Binary(data); 
		} else {
			throw new IllegalContentTypeException(BASE64, specialFileType);
		}
	}

	public String getSpecialFileType() {
		return specialFileType;
	}

	/**
	 * Serializes this JSONFile object into a String
	 * 
	 * @return the serialized JSONFile as a String
	 */
	public String toJSONString() {
		return this.toJSONObject().toJSONString();
	}

	/**
	 * Turn this JSONFile into a regular JSONObject
	 * 
	 * @return the JSONObject representing the data in this JSONFile
	 */
	@SuppressWarnings("unchecked")
	public JSONObject toJSONObject() {
		JSONObject jObj = new JSONObject();
		jObj.put("filename", this.fileName);
		jObj.put("filetype", this.fileType);
		jObj.put("filedata", this.data);
		jObj.put("specialfiletype", this.specialFileType);
		return jObj;
	}

	/**
	 * Write a collection of JSONFiles out in JSON Array Notation.
	 * 
	 * @param files
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String collectionToString(Collection<JSONFile> files) {
		JSONArray array = new JSONArray();
		for (JSONFile jsf : files) {
			array.add(jsf.toJSONObject());
		}
		return array.toJSONString();
	}

	/**
	 * Deserializes an existing folder into an Vector of JSON files
	 * 
	 * @param folder
	 *            the folder to read the files from
	 * @return a Vector of JSONFile objects
	 */
	public static Vector<JSONFile> readFolder(File folder) throws IOException {
		if (!folder.isDirectory()) {
			System.err.println("readFolderIntoJSONFileSet(): not a folder: "
					+ folder.getName());
			return null;
		}
		File[] fileList = folder.listFiles();
		Vector<JSONFile> result = new Vector<JSONFile>();
		for (File f : fileList) {
			result.add(new JSONFile(f));
		}
		return result;
	}

	/**
	 * Deserialize a Collection of JSONFiles from one JSONArray Object, encoded
	 * as a String
	 * 
	 * @param jsonString
	 *            the input array
	 * @return an actual array of JSONFile objects
	 */
	public static Vector<JSONFile> restoreJSONFileSet(String jsonString) {
		JSONArray jarray = (JSONArray) JSONValue.parse(jsonString);
		Vector<JSONFile> result = new Vector<JSONFile>();
		for (Object entry : jarray) {
			try {
				JSONObject jsonEntry = (JSONObject) entry;
				JSONFile file = new JSONFile(jsonEntry);
				result.add(file);
			} catch (ClassCastException cce) {
				throw new IllegalArgumentException("Could not parse input as set of JSONFiles", cce);
			}
		}
		return result;
	}

	/**
	 * Write a Collection of JSONFiles to disk
	 * 
	 * @param fileSet
	 *            a Collection of JSONFiles to be written
	 * @param path
	 *            the folder path to write the files into
	 */
	public static void writeJSONFileSet(Collection<JSONFile> fileSet, String path) throws IOException {
		for (JSONFile jsonFile : fileSet) {
			jsonFile.write(path);
		}
	}

	/**
	 * Write a JSONFile to disk. The specialfiletype field decides whether it
	 * will be written as a text or decoded from base64 and written as a binary
	 * file
	 * 
	 * @param path
	 *            The path to write the file in. The actual filename will be
	 *            appended
	 */
	public void write(String path) throws IOException {
		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdirs();
			System.out.println("JSONFile.write(): Creating directory" + path);
		}
		File file = new File(path + File.separator +getFileName());
		System.out.println("JSONFile.write(): writing " + file);
		if (!isBase64()) {
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(data);
			bw.flush();
			bw.close();
		} else {
			byte[] bytes = DatatypeConverter.parseBase64Binary(data);
			OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
			output.write(bytes);
			output.close();
		}
	}

	/**
	 * Check if this JSONFile contains base64 encoded data
	 * 
	 * @return if this JSONFile contains base64 encoded data
	 */
	private boolean isBase64() {
		return (specialFileType != null && specialFileType.equals(BASE64));
	}

	@Override
	public String toString() {
		return this.toJSONString();
	}

}
