package com.lasvegas.library.exception;
/**
 * configuration not found exception
 */
public class ConfigurationNotFoundException extends RuntimeException {

    /**
     * @param configName The config name.
     */
    public ConfigurationNotFoundException(String configName) {
        super(String.format("Configuration with name '%s' does not exist", configName));
    }
}

