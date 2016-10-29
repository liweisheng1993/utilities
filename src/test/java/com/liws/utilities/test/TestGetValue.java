package com.liws.utilities.test;

import com.liws.utilities.blockqueue.BlockingLinkedQueue;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by liweisheng on 16/10/18.
 */


class ProduceValue implements Runnable{
    private BlockingLinkedQueue blq;
    private static AtomicLong num = new AtomicLong(0L);

    public ProduceValue(BlockingLinkedQueue blq){
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
            blq.put(num.getAndIncrement());
        }
    }
}

class GetValue implements Runnable{
    private BlockingLinkedQueue blq;

    public GetValue(BlockingLinkedQueue blq){
        this.blq = blq;
    }
    @Override
    public void run() {
        for(int i=0;i<100;++i){
            Object value = blq.getValue();
        }
    }
}


public class TestGetValue {
    @Test
    public void testGetValue(){
//        BlockingLinkedQueue<Long> blq = new BlockingLinkedQueue<>();
//        ExecutorService executor = Executors.newCachedThreadPool();
//        Long start = System.currentTimeMillis();
//        for(int i=0;i<5;++i){
//            executor.execute(new ProduceValue(blq));
//        }
//
//        try {
//            executor.awaitTermination(5,TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        for(int i=0;i<5;++i){
//            executor.execute(new GetValue(blq));
//        }
//
//        try {
//            executor.awaitTermination(5,TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//
//  }
        Long allDiff = 0L;
        Long sns = System.nanoTime();
        Long sms = System.currentTimeMillis();
        Long ems = 0L;
        Long ens = 0L;
        for(int i= 0 ;i<100;++i){
            sns = System.nanoTime();
            sms = System.currentTimeMillis();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ens = System.nanoTime();
            ems = System.currentTimeMillis();

            allDiff += (ens - sns) / (ems - sms);

        }









        System.out.println(allDiff / 100);



    }

}
