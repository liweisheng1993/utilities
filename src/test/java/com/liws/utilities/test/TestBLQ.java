package com.liws.utilities.test;

import com.liws.utilities.blockqueue.BlockingLinkedQueue;
import org.junit.Test;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by liweisheng on 16/9/7.
 */

class Producer implements Runnable{
    private BlockingLinkedQueue blq;
    private static AtomicLong num = new AtomicLong(0L);

    public Producer(BlockingLinkedQueue blq){
        this.blq = blq;
    }
    @Override
    public void run() {
        for(int i=0;i<10;++i){

            blq.put(num.getAndIncrement());
        }
    }
}

class Consumer implements Runnable{
    public static AtomicLong count = new AtomicLong(0L);
    private BlockingLinkedQueue blq;

    public Consumer(BlockingLinkedQueue blq){
        this.blq = blq;
    }

    @Override
    public void run() {
        for(int i=0;i<10;++i){
            Object v = blq.poll();
            count.incrementAndGet();
            System.out.println(v);
        }

    }
}

public class TestBLQ {
//    @Test
    public void testSingle(){
        BlockingLinkedQueue<Long> blq = new BlockingLinkedQueue<>();
        for(long i=0;i<0;++i){
            blq.put(i);
        }
        for(int i=0;i<1000;++i){
            Long v = blq.poll();
            System.out.println(v);
        }
    }

    @Test
    public void testMulti(){
        BlockingLinkedQueue<Long> blq = new BlockingLinkedQueue<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        Long start = System.currentTimeMillis();
        for(int i=0;i<10;++i){
            executor.execute(new Producer(blq));
        }

        try {
            executor.awaitTermination(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(int i=0;i<10;++i){
            executor.execute(new Consumer(blq));
        }

        System.out.println("time cost: " + (System.currentTimeMillis() - start) + " count:" + Consumer.count);

        try {
            executor.awaitTermination(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
