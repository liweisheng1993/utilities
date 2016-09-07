package com.liws.utilities.test;

import com.liws.utilities.eventchannel.EventChannel;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by liweisheng on 16/9/7.
 */

class Notify implements Runnable{
    private EventChannel eventChannel;
    public Notify(EventChannel eventChannel){
        this.eventChannel = eventChannel;
    }

    @Override
    public void run() {
        for(int i=0;i<100;++i){
            eventChannel.notify("print",i);
        }
    }
}

public class TestSubPrintMsg {
    private PrintMsg printMsg;
    private EventChannel eventChannel;
    @Before
    public void before(){
        printMsg = new PrintMsg();
        eventChannel = new EventChannel();
        try {
            eventChannel.subsribe(printMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    @Test
    public void testPrintMsg(){
        try {
            eventChannel.notify("print", "Hello World");
            for(int i=0;i<100;++i){
                eventChannel.notify("print",i);
            }
            TimeUnit.SECONDS.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPrintMsgMultiThread(){
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Notify notify = new Notify(eventChannel);
        for(int i=0;i<10;++i){
            executorService.execute(notify);
        }
        executorService.shutdown();
        try {
            TimeUnit.SECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
