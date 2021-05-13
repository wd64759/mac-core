package com.cte4.mac.core.utils;

public class MacInitiatialException extends Exception {
    public MacInitiatialException() {
        super();
    }

    public MacInitiatialException(String error) {
        super(error);
    }

    public MacInitiatialException(String error, Throwable t) {
        super(error, t);
    }

    public MacInitiatialException(Throwable t) {
        super(t);
    }

}
