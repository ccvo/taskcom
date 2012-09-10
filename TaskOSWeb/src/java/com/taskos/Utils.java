package com.taskos;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.rng.CompactSchemaReader;

import org.xml.sax.InputSource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Static utility methods.
 */
public final class Utils {
    
    /**
     * Validate the given task description against the given RelaxNG schema
     *
     * @param xmlFile string identifying url, system resource or file to validate
     * @return Error string or null if no error
     */
    public static boolean validate(String xmlFile, String schema) {
        PropertyMapBuilder properties = new PropertyMapBuilder();
        ValidationDriver driver = new ValidationDriver(properties.toPropertyMap(), CompactSchemaReader.getInstance());
        try {
            try {
                driver.loadSchema(new InputSource(toURL(schema).openStream()));
            } catch (MalformedURLException e) {
                return false;
            }
            
            boolean valid = driver.validate(new InputSource(toURL(xmlFile).openStream()));
            return valid;
        } catch (Exception e) {
            System.out.println("---" + e);
            return false;
        }
    }

    /**
     * Convert the given source string to a URL.  If the given string is not a well-formed
     * URL, then see if it names a system resource; otherwise treat it as a filename.
     */
    private static URL toURL(String source) throws MalformedURLException {
        try {
            return new URL(source);
        } catch (MalformedURLException e) {
            URL url = ClassLoader.getSystemResource(source);
            // sic toURI() first (see usage note in File.toURL)
            return url == null ? new File(source).toURI().toURL() : url;
        }
    }
}