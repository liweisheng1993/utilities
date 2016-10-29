package com.liws.utilities.eventchannel;

/**
 * Created by liweisheng on 16/8/19.
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
