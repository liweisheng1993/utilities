package com.liws.eventchannel;

import com.google.common.collect.HashMultimap;
import com.liws.eventchannel.exceptions.MethodModifierInvalidException;
import com.liws.eventchannel.exceptions.ParameterCountInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by liweisheng on 16/9/7.
 */
public class EventChannel {

    private static Logger LOG = LoggerFactory.getLogger(EventChannel.class);

    private ExecutorService threadPoll;

    private LinkedBlockingQueue<Message> messagesQueue;

    private HashMultimap<String, RegistEnrty> subscribeEntries;

    private ReadWriteLock rwLock;

    private Poller poller;
    private Thread pollerThread;

    public EventChannel() {
        threadPoll = Executors.newCachedThreadPool();
        messagesQueue = new LinkedBlockingQueue<>();
        subscribeEntries = HashMultimap.create();
        rwLock = new ReentrantReadWriteLock();
        poller = new Poller();
        pollerThread = new Thread(poller);
        pollerThread.setDaemon(true);
        pollerThread.start();
    }

    public void subsribe(Object subscriber) throws Exception {
        if (null == subscriber) {
            return;
        }

        Class clazz = subscriber.getClass();
        if (clazz.equals(Object.class)) {
            return;
        }

        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            Sub subAnnotation = method.getAnnotation(Sub.class);
            if (null != subAnnotation) {
                checkMethodValid(method);
                try {
                    rwLock.writeLock().lock();
                    subscribeEntries.put(method.getName(), new RegistEnrty(method, subscriber));
                } finally {
                    rwLock.writeLock().unlock();
                }

            }
        }
    }

    public void notify(String methodName, Object message) {
        Message m = new Message(methodName, message, null);
        try {
            rwLock.readLock().lock();
            Set<RegistEnrty> registEntries = subscribeEntries.get(methodName);

            for (RegistEnrty entry : registEntries) {
                Method method = entry.getMethod();
                if (method.getParameterTypes()[0] == message.getClass()) {
                    m = new Message(methodName, message, entry.getTarget());
                }
            }
            messagesQueue.put(m);
        } catch (InterruptedException e) {
            m = null;
        } finally {
            rwLock.readLock().unlock();
        }
    }


    private void checkMethodValid(Method method) throws Exception {
        int parameterCount = method.getParameterCount();
        if (1 != parameterCount) {
            throw new ParameterCountInvalidException(method.getName());
        }

        int modifier = method.getModifiers();

        if ((modifier & 0x1) == 0 || (modifier & 0x8) != 0) {
            throw new MethodModifierInvalidException(method.getName());
        }
    }

    private RegistEnrty getTarget(Message m) {
        String methodName = m.getMethodName();
        Object messageBody = m.getMessageBody();
        rwLock.readLock().lock();
        try {
            Set<RegistEnrty> entries = subscribeEntries.get(methodName);
            if (null == entries || entries.isEmpty()) {
                return null;
            } else {
                for (RegistEnrty entry : entries) {
                    if (messageBody.getClass() == entry.getMethod().getParameterTypes()[0]) {
                        m.setTarget(entry.getTarget());
                        return entry;
                    }
                }
            }
        } finally {
            rwLock.readLock().unlock();
        }

        return null;
    }

    private class Poller implements Runnable {
        @Override
        public void run() {
            for (; ; ) {
                Message m = messagesQueue.poll();
                if (null == m) {
                    continue;
                }
                Object target = null;
                Method method = null;
                RegistEnrty entry = null;

                entry = getTarget(m);
                if (null == entry) {
                    try {
                        messagesQueue.put(m);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                } else {
                    target = entry.getTarget();
                    method = entry.getMethod();
                }

                Invocation invocation = new Invocation(method,
                        target, m.getMessageBody());

                EventTask task = new EventTask(invocation);

                threadPoll.submit(task);
            }

        }
    }
}
