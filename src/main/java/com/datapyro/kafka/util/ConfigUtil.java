package com.datapyro.kafka.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigUtil {

    public static Properties getConfig(String configName) {
        try {
            Properties properties = new Properties();
            properties.load(getResource(configName + ".properties"));
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream getResource(String resourceName) {
        return ConfigUtil.class.getClassLoader().getResourceAsStream(resourceName);
    }
    
}
