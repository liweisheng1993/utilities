package com.liws.utilities.distributedlock;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Created by liweisheng on 16/9/9.
 */
public class PrevNodeDeleteWatcher implements Watcher {
    private Thread thread;
    public PrevNodeDeleteWatcher(Thread t ){
        this.thread = t;
    }
    @Override
    public void process(WatchedEvent event) {
        Event.EventType eventType = event.getType();
        Event.KeeperState keeperState = event.getState();
        if(eventType == Event.EventType.NodeDeleted){
            synchronized (thread){
                thread.notify();
            }
        }
    }
}
