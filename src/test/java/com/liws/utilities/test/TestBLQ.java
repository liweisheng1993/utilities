package com.liws.utilities.test;

import com.liws.utilities.blockqueue.BlockingLinkedQueue;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by liweisheng on 16/9/7.
 */

class Producer implements Runnable{
    private BlockingLinkedQueue blq;

    public Producer(BlockingLinkedQueue blq){
        this.blq = blq;
    }
    @Override
    public void run() {
        for(int i=0;i<100;++i){
            try {
                TimeUnit.MILLISECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            blq.put(i);
        }
    }
}

class Consumer implements Runnable{
    private BlockingLinkedQueue blq;

    public Consumer(BlockingLinkedQueue blq){
        this.blq = blq;
    }

    @Override
    public void run() {
        for(int i=0;i<10;++i){
            Object v = blq.poll();
            System.out.println(v);
        }

    }
}

public class TestBLQ {
//    @Test
    public void testSingle(){
        BlockingLinkedQueue<Integer> blq = new BlockingLinkedQueue<>();
        for(int i=0;i<0;++i){
            blq.put(i);
        }
        for(int i=0;i<100;++i){
            Integer v = blq.poll();
            System.out.println(v);
        }
    }

    @Test
    public void testMulti(){
        BlockingLinkedQueue<Integer> blq = new BlockingLinkedQueue<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        for(int i=0;i<10;++i){
            executor.execute(new Producer(blq));
            executor.execute(new Consumer(blq));
        }

        try {
            TimeUnit.SECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
