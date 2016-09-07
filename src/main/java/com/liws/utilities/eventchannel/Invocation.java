package com.liws.utilities.eventchannel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by liweisheng on 16/9/7.
 */
public class Invocation {
    private Method method;

    private Object target;

    private Object arg;

    public Invocation(Method m, Object t,Object arg){
        this.method = m;
        this.target = t;
        this.arg = arg;
    }

    public void invoke(){
        if(null == method || null == target){
            return;
        }
        try {
            method.invoke(target,arg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
        }
    }
}
