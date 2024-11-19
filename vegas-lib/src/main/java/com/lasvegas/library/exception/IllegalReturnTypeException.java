package com.lasvegas.library.exception;

public class IllegalReturnTypeException extends RuntimeException {

    /**
     * @param configName The config name.
     */
    public IllegalReturnTypeException(String configName) {
        super(String.format("Configuration with name '%s' does not exist", configName));
    }
}
