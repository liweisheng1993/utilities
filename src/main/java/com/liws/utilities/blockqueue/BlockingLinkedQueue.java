package com.liws.utilities.blockqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by liweisheng on 16/8/27.
 */
public class BlockingLinkedQueue<E> {
    private static Logger LOG = LoggerFactory.getLogger(BlockingLinkedQueue.class);
    private static AtomicLong uuid = new AtomicLong(0L);
    static final int COMPLETE = 0;
    static final int WAITING = 1;
    static final Unsafe unsafe;

    static final long waitHeadOffset;
    static final long waitTailOffset;
    static final long valueHeadOffset;
    static final long valueTailOffset;

    static{
        try{
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe)f.get(null);
            waitHeadOffset = unsafe.objectFieldOffset(BlockingLinkedQueue.class.getDeclaredField("waitHead"));
            waitTailOffset = unsafe.objectFieldOffset(BlockingLinkedQueue.class.getDeclaredField("waitTail"));
            valueHeadOffset = unsafe.objectFieldOffset(BlockingLinkedQueue.class.getDeclaredField("valueHead"));
            valueTailOffset = unsafe.objectFieldOffset(BlockingLinkedQueue.class.getDeclaredField("valueTail"));
        }catch (Exception e){
            throw new Error(e);
        }
    }

    private WaiterNode waitHead;

    private WaiterNode waitTail;

    private ValueNode valueHead;

    private ValueNode valueTail;

    public BlockingLinkedQueue(){
        waitHead = waitTail = new WaiterNode(null,null,COMPLETE);
        valueHead = valueTail = null;
    }


    public void put(E value){
        addValue(value);
    }

    public E poll(){
        return  takeValue(-1);
    }

    private E takeValue(long millis){
        Long tick = 1L;
        E value = null;
        Thread currentThread = Thread.currentThread();
        WaiterNode waiterNode = new WaiterNode(null, currentThread, WAITING);
        addWaiterNode(waiterNode);
        for(;;){
            if(waiterNode.getPrev() == waitHead){
                value = getValue();
                LOG.debug("waiter:{}, poll value:{}",waiterNode,value);
                compareAndSetWaitHead(waitHead,waiterNode);
                waiterNode.setStatus(COMPLETE);
                WaiterNode nextNode = waiterNode.getNext();
                if(null != nextNode){
                    nextNode.unparkSelf();
                    LOG.debug("unpark waiter:{},time:{}",nextNode.toString(),System.currentTimeMillis());
                }
                break;
            }else{
                WaiterNode prev = waiterNode.getPrev();

                if(prev != waitHead){
                    LOG.debug("park self:{},time:{}", waiterNode, System.currentTimeMillis());
                    if(0 == millis){
                        break;
                    }else if(millis < 0){
                        waiterNode.parkSelf(1000000 * tick);
                        tick = tick << 1;
                    }else{
                        Long timeAlreadyWait = (tick-1) * 1000000;
                        if(timeAlreadyWait >= millis){
                            return value;
                        }else if(millis - timeAlreadyWait > tick * 1000000){
                            waiterNode.parkSelf(1000000 * tick);
                            tick = tick << 1;
                        }else{
                            waiterNode.parkSelf(millis - timeAlreadyWait);
                        }
                    }
                    LOG.debug("unpark self:{}",waiterNode);
                }
            }
        }
        return value;
    }

    public E getValue(){
        ValueNode oldHead = valueHead;
        ValueNode oldTail = valueTail;
        if(null == oldHead){
            return null;
        }

        if(compareAndSetValueHead(oldHead,oldHead.getNext())){
            if(oldTail == oldHead){
                compareAndSetValueTail(oldTail,null);
            }

            return oldHead.getValue();
        }

        return null;
    }

    private void addValue(E value){
        ValueNode valueNode = new ValueNode(value);
        LOG.debug("add new value:{}",value);
        for(;;){
            ValueNode oldTail = valueTail;
            if(null == oldTail){
                if(compareAndSetValueTail(null, valueNode)){
                    valueHead = valueTail;
                    LOG.debug("tail is null, set to:{},head is:{}",valueNode.value,valueHead.value);
                    //TODO:如果waitNode上有等待线程,唤醒它.
                    WaiterNode headNext = waitHead.getNext();

                    while(null != headNext &&  headNext.getStatus() == COMPLETE){
                        headNext = headNext.getNext();
                    }

                    if(null != headNext){
                        headNext.unparkSelf();
                    }
                    break;
                }
            }else{
                if(compareAndSetValueTail(oldTail, valueNode)){
                    LOG.debug("previous tail:{}, add value:{},current tail:{},head:{}",oldTail.value,value,valueTail.value,valueHead.value);
                    oldTail.setNext(valueNode);
                    break;
                }
            }
        }
    }

    private void addWaiterNode(WaiterNode waiter){
        for(;;){
            WaiterNode oldTail = this.waitTail;
            waiter.setPrev(oldTail);
            if(compareAndSetWaitTail(oldTail, waiter)){
                LOG.debug("add new waiter:{},previous tail:{},current tail:{}",waiter.toString(),oldTail.toString(),waiter.toString());
                oldTail.setNext(waiter);
                break;
            }
        }
    }

    private boolean compareAndSetWaitTail(WaiterNode expect, WaiterNode newNode){
        return unsafe.compareAndSwapObject(this, waitTailOffset,expect,newNode);
    }

    private boolean compareAndSetWaitHead(WaiterNode expect, WaiterNode newNode){
        return unsafe.compareAndSwapObject(this, waitHeadOffset,expect,newNode);
    }

    private boolean compareAndSetValueHead(ValueNode expect, ValueNode newNode){
        return unsafe.compareAndSwapObject(this,valueHeadOffset,expect,newNode);
    }

    private boolean compareAndSetValueTail(ValueNode expect, ValueNode newNode){
        return unsafe.compareAndSwapObject(this,valueTailOffset,expect,newNode);
    }

    class ValueNode{
        private E value;

        private ValueNode next;

        public ValueNode(){
            value = null;
            next = null;
        }

        public ValueNode(E v){
            this.value = v;
            this.next = null;
        }

        public ValueNode(E value, ValueNode next){
            this.value = value;
            this.next = next;
        }

        public ValueNode next(){return this.next;}

        public boolean hasNext(){return null != this.next;}

        public E getValue() {
            return value;
        }

        public void setValue(E value) {
            this.value = value;
        }

        public ValueNode getNext() {
            return next;
        }

        public void setNext(ValueNode next) {
            this.next = next;
        }
    }

    class WaiterNode{
        private Long uuid;
        volatile private  int status;
        private WaiterNode prev;

        private WaiterNode next;

        private Thread currentThread;

        public WaiterNode(WaiterNode nextNode, Thread thread,int status){
            this.next = nextNode;
            this.currentThread = thread;
            if(null != nextNode){
                nextNode.prev = this;
            }

            this.status = status;
            this.uuid = BlockingLinkedQueue.uuid.getAndIncrement();

        }

        public WaiterNode getPrev() {
            return prev;
        }

        public void setPrev(WaiterNode prev) {
            this.prev = prev;
        }

        public WaiterNode getNext() {
            return next;
        }

        public void setNext(WaiterNode next) {
            this.next = next;
        }

        public Thread getCurrentThread() {
            return currentThread;
        }

        public void setCurrentThread(Thread currentThread) {
            this.currentThread = currentThread;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public void parkSelf(){
            LockSupport.park(this);
        }

        public void parkSelf(long nanos){
            LockSupport.parkNanos(this,nanos);
        }

        public void unparkSelf(){
            LockSupport.unpark(currentThread);
        }

        @Override
        public String toString() {
            return "WaiterNode{" +
                    "uuid=" + uuid +
                    ", status=" + status +
                    ", currentThread=" + currentThread +
                    '}';
        }
    }
}
