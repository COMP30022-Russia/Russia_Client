package com.comp30022.team_russia.assist;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class for initialising adjustable configuration.
 */
public class ConfigurationManager {
    /**
     * Instance of configuration class.
     */
    private static ConfigurationManager instance;
    /**
     * Location of properties file.
     */
    static final String CONFIG_FILENAME = "config.properties";

    /**
     * Loaded properties.
     */
    private Properties props;

    private ConfigurationManager(InputStream inputStream) throws IOException {
        props = new Properties();
        props.load(inputStream);
    }

    /**
     * Retrieves a property from the properties file.
     *
     * @param name Name of property.
     * @return Value of property.
     */
    public String getProperty(String name) {
        if (props.containsKey(name)) {
            return props.getProperty(name);
        }
        return null;
    }

    /**
     * Retrieves a property from the properties file.
     * @param name Name of property.
     * @param defaultValue Fallback value if the property does not exist.
     * @return Value of the property.
     */
    public String getProperty(String name, String defaultValue) {
        String tmp = getProperty(name);
        return tmp == null ? defaultValue : tmp;
    }

    /**
     * Retrieves a boolean property from the properties file.
     * Unset (non-existent) properties are treated as false.
     * @param key Name of the property.
     * @return Boolean value of the property.
     */
    public boolean getBooleanPropery(String key) {
        String str = getProperty(key);
        if (str == null) {
            return false;
        }
        if (str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    static void createInstance(InputStream inputStream) throws IOException {
        instance = new ConfigurationManager(inputStream);
    }

    /**
     * Returns the static instance of the configuration class.
     *
     * @return Static instance of configuration class.
     */
    public static ConfigurationManager getInstance() {
        return instance;
    }
}
