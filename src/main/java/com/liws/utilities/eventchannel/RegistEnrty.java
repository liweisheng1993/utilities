package com.liws.utilities.eventchannel;

import java.lang.reflect.Method;

/**
 * Created by liweisheng on 16/8/20.
 */
public class RegistEnrty {
    Method method;
    Object target;

    public RegistEnrty(Method m, Object o){
        this.method = m;
        this.target = o;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }
}
