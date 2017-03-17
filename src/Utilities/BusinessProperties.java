package Utilities;

import java.io.FileInputStream;
import java.util.Properties;


/**
 * Class to retrieved business properties from file
 * Created by WillDeJs on 1/15/2017.
 */
public class BusinessProperties {
    private static Properties properties;
    /**
     * Retrieve a property from file
     * @param property  property key to be retrieved
     * @return  string containing property value
     * @throws Exception 
     */
     static public String getProperty(String property){
        properties = new Properties();
        String ret = null;
        try {
            properties.load(new FileInputStream("custom/properties.txt"));
            ret = properties.getProperty(property);
        } catch(Exception ex) {
            Trace.getTrace().log(BusinessProperties.class, Trace.Levels.ERROR, ex);
        }
        return ret;
    }

}
