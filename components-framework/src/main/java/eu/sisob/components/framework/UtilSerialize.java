/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.components.framework;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author ajgallardo
 */
public class UtilSerialize {
        
    public static byte[] toByteArray (Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos); 
            oos.writeObject(obj);
            oos.flush(); 
            oos.close(); 
            bos.close();
            bytes = bos.toByteArray ();
        }
        catch (IOException ex) {
            //TODO: Handle the exception
            ex.printStackTrace();
        }      
        return bytes;
    }
    
    private static Object toObject (byte[] bytes)
    {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
            ObjectInputStream ois = new ObjectInputStream (bis);
            obj = ois.readObject();
        }
        catch (IOException ex) {
            //TODO: Handle the exception
        }
        catch (ClassNotFoundException ex) {
            //TODO: Handle the exception
        }
        return obj;
    }
    
    public static Object arrayByteToObject (Object o){    
        Object rObject = null;
        
        if (o instanceof byte[]) {
           byte[] arrayByte = (byte[])o; 
           rObject = toObject(arrayByte);
        }
        
        return rObject;
    }
    
    
}
