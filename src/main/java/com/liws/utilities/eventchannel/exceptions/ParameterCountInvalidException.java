package com.liws.utilities.eventchannel.exceptions;

/**
 * Created by liweisheng on 16/9/7.
 */
public class ParameterCountInvalidException extends Exception {
    public ParameterCountInvalidException(String methodName) {
        super("method[" + methodName + "] has invalid parameter count,must be 1 arg");
    }
}
