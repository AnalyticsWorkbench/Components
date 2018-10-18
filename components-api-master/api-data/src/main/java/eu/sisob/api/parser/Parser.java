/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.api.parser;

import eu.sisob.components.framework.json.util.IllegalContentTypeException;

/**
 *
 * @author hecking
 */
public interface Parser {
    
    
    /**
     * The parse method of the NetworkParser class.
     * Depending on the specialized type of parser, this method will transform 
     * the given network data into the appropriate format
     */
    public abstract void parse() throws IllegalContentTypeException;
    
    /**
     * The abstract method encode.
     * This method will be called on the specialized parsers
     * in order to have a JSON representation of the parsed network.
     * @return the encoded JSON String of the given network
     */
    public String encode();	

    public String requestFileTypeExtension();

    /**
     * This method will return a string related to the file type
     * @return a string containing the file tipe in the format "filetype"
     */
    public String requestFileType();
}
