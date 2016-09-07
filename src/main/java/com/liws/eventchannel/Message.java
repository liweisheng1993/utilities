package com.liws.eventchannel;

import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 * Created by liweisheng on 16/9/7.
 */
public class Message implements Record{
    private String methodName;
    private Object messageBody;
    private Object target;

    public Message(String methodName, Object messageBody, Object target){
        this.methodName = methodName;
        this.messageBody = messageBody;
        this.target = target;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(Object messageBody) {
        this.messageBody = messageBody;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    @Override
    public void serialize(OutputStream os) {

    }

    @Override
    public Object deserialize(OutputStream os) {
        return null;
    }
}
