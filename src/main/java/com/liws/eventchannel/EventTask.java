package com.liws.eventchannel;

/**
 * Created by liweisheng on 16/9/7.
 */
public class EventTask implements Runnable {
    private Invocation invocation;

    public EventTask(Invocation invocation){
        this.invocation = invocation;
    }

    @Override
    public void run() {
        invocation.invoke();
    }
}
