package org.unicon.lex.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesService {
    
    private Properties properties;
    
    public Properties getProperties() {
        if (properties == null) {
            properties = this.loadProperties();
        }
        return properties;
    }

    public Properties loadProperties() {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = getClass().getClassLoader().getResourceAsStream("lambda.properties");
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }     
        return prop;
    }
}
