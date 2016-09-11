package com.liws.utilities.distributedlock;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by liweisheng on 16/9/8.
 */
class LockInternal {
    private static Logger LOG = LoggerFactory.getLogger(LockInternal.class);
    private String lockPath;
    private ZooKeeper zkClient;
    private UUID uuid;

    public LockInternal(String lockPath,ZooKeeper zkClient) throws Exception{
        this.lockPath = lockPath;
        this.uuid = UUID.randomUUID();
        PathUtil.createPathIfNotExist(zkClient,lockPath);
    }

    /**
     * 尝试获得锁,最长等待microSecondsWait
     *
     * @param milliSecondsWait 等待时间,<0表示一直等待.
     * @return true 成功获得锁,false没有获得锁.
     * */
    public boolean tryAcquire(Long milliSecondsWait) throws Exception{
        long sessionId = zkClient.getSessionId();
        Long tryStartTimeMillis = System.currentTimeMillis();
        StringBuilder lockname = new StringBuilder();
        lockname.append(sessionId).append("_").append(uuid.toString()).append("_");

        String realLockNodeName = PathUtil.createEphermalSeqNode(zkClient, lockPath, lockname.toString());

        return holdLockOrWait(tryStartTimeMillis,milliSecondsWait,realLockNodeName);
    }

    public boolean tryAcquire() throws Exception{
        return tryAcquire(-1L);
    }

    public void release() throws Exception{

    }

    private boolean holdLockOrWait(Long tryStartTimeMillis, Long milliSecondsWait,String realLockNodeName) throws Exception{
        List<String> childrenUnderLockPath = zkClient.getChildren(lockPath, null);
        PathUtil.sortSeqentialNodes(childrenUnderLockPath);

        String smallestNode = childrenUnderLockPath.get(0);
        if(realLockNodeName == smallestNode){
            return true;
        }

        String nodeNameWaitFor = PathUtil.getSeqNodeBeforeCurrent(childrenUnderLockPath,realLockNodeName);
        if(milliSecondsWait == 0){
            return false;
        }
        else if(milliSecondsWait < 0){
            return waitLock(tryStartTimeMillis,milliSecondsWait,nodeNameWaitFor,realLockNodeName);
        }else{
            Long currentSystemMillis = System.currentTimeMillis();
            if(currentSystemMillis < tryStartTimeMillis + milliSecondsWait){
                return false;
            }
            return waitLock(tryStartTimeMillis,milliSecondsWait,nodeNameWaitFor,realLockNodeName);
        }
    }

    private boolean waitLock(Long tryStartTimeMillis, Long milliSecondWait, String nodeNameWaitFor,String realNodeName) throws Exception{
        Long currentSystemTime = System.currentTimeMillis();
        Thread currentThread = Thread.currentThread();

        try{
            if(zkClient.exists(nodeNameWaitFor,new PrevNodeDeleteWatcher(currentThread)) == null) {
                return this.holdLockOrWait(tryStartTimeMillis,milliSecondWait,realNodeName);
            }else{
                if(milliSecondWait < 0){
                    synchronized (currentThread){
                        currentThread.wait();
                    }
                }else{
                    Long timeToWait = tryStartTimeMillis + milliSecondWait - currentSystemTime;
                    if(timeToWait <= 0){
                        zkClient.delete(realNodeName,2);
                        return false;
                    }else{
                        synchronized (currentThread){
                            currentThread.wait(timeToWait);
                        }
                    }
                }
            }
        }catch (InterruptedException e){
            if(currentThread.isInterrupted()){
                return this.holdLockOrWait(tryStartTimeMillis,milliSecondWait,realNodeName);
            }else{
                zkClient.delete(realNodeName,2);
                return false;
            }
        }catch (KeeperException e){
            e.printStackTrace();
        }
        return false;
    }
}
