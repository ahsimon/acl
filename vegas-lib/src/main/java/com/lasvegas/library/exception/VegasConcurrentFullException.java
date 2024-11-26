package com.lasvegas.library.exception;

public class VegasConcurrentFullException extends RuntimeException{
    public VegasConcurrentFullException(String  backend ) {
        super(String.format("Cant Acquire VegasListener for backend '%s'",  backend  ));
    }
}
