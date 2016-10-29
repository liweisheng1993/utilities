package com.liws.utilities.eventchannel.exceptions;

/**
 * Created by liweisheng on 16/8/19.
 */
public class MethodModifierInvalidException extends Exception{
    public MethodModifierInvalidException(String methodName) {
        super("method[" + methodName + "] has invalid method modifier,must be public and non static");
    }
}
