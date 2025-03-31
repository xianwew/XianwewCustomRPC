package com.xianwei.customrpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * Configuration Utility Class
 *
 * Provides methods to load configuration objects from properties files.
 */
public class ConfigUtils {

    /**
     * Load a configuration object using the default environment.
     *
     * @param tClass The class of the configuration object
     * @param prefix The property prefix to map (e.g., "rpc")
     * @param <T>    Generic type of the configuration object
     * @return An instance of the config class populated from the properties file
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }

    /**
     * Load a configuration object with environment-specific support.
     *
     * Loads properties from `application.properties` or `application-{env}.properties`.
     *
     * @param tClass       The class of the configuration object
     * @param prefix       The property prefix to map (e.g., "rpc")
     * @param environment  Optional environment name (e.g., "dev", "prod")
     * @param <T>          Generic type of the configuration object
     * @return An instance of the config class populated from the appropriate properties file
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".properties");

        // Load the properties file (e.g., application.properties or application-dev.properties)
        Props props = new Props(configFileBuilder.toString());

        // Map the properties with the given prefix to an instance of the target config class
        return props.toBean(tClass, prefix);
    }
}
