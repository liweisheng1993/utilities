package com.liws.utilities.distributedlock;

import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.locks.Lock;

/**
 * Created by liweisheng on 16/9/8.
 */
public class ReentrantDistributedLock {

    private Thread ownerThread;
    private volatile Long state;
    private LockInternal lockInternal;
    public ReentrantDistributedLock(String lockName, ZooKeeper zkClient) throws Exception{
        this.lockInternal = new LockInternal(lockName,zkClient);
        this.state = 0L;
    }

    public void lock() throws Exception{
        Thread currentThread  = Thread.currentThread();
        if(null == ownerThread){
            lockInternal.tryAcquire();
            ownerThread = currentThread;
            ++state;
        }else{
            if(ownerThread == currentThread){
                ++state;
            }else{
                lockInternal.tryAcquire();
                ownerThread = currentThread;
                ++state;
            }
        }
    }
    public void unlock() throws Exception{
        Thread currentThread = Thread.currentThread();
        if(ownerThread == null || ownerThread != currentThread){
            throw new IllegalMonitorStateException();
        }else{
            --state;
            if(0 == state){
                ownerThread = null;
                lockInternal.release();
            }
        }
    }
    public boolean tryLock(){
        return tryLock(0L);
    }

    public boolean tryLock(Long timeWaitMillis){
        try{
            Thread currentThread = Thread.currentThread();
            if(null == ownerThread){
                boolean result = lockInternal.tryAcquire(timeWaitMillis);
                if(true == result){
                    ownerThread = currentThread;
                    ++state;
                }
                return result;
            }else{
                if(ownerThread == currentThread){
                    ++state;
                    return true;
                }else{
                    boolean result = lockInternal.tryAcquire(timeWaitMillis);
                    if(true == result){
                        ownerThread = currentThread;
                        ++state;
                    }
                    return result;
                }
            }
        }catch (Exception e){
            return false;
        }
    }
}
