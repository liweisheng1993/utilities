package com.liws.utilities.test;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by liweisheng on 16/10/29.
 */

class ParkTask implements Runnable{
    @Override
    public void run() {
        try {
            System.out.println("thread:" + Thread.currentThread() + "sleeping5s");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("thread:" + Thread.currentThread() + " parking,time:" + System.currentTimeMillis());
        LockSupport.park(this);
        System.out.println("thread:" + Thread.currentThread() + " un parked, time:" + System.currentTimeMillis());
    }
}

public class TestPark {
    @Test
    public void testPark(){
        Thread parkThread = new Thread(new ParkTask());
        parkThread.start();
        System.out.println("main thread:" + Thread.currentThread() + "unpark thread:" + parkThread + "time:" + System.currentTimeMillis());
        LockSupport.unpark(parkThread);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
