//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.util;

import java.util.concurrent.locks.*;
import java.util.concurrent.*;
import java.util.*;

public class SimpleTimer
{
    private final Lock lock;
    private final Condition condition;
    private final InternalClock clock;
    private final TaskQueue queue;
    private final TimerThread thread;
    private final InternalClock.TimeReachedJob reachedJob;
    private final Object threadReaper;
    
    public SimpleTimer(final InternalClock c, final String name, final boolean isDaemon) {
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
        this.queue = new TaskQueue();
        this.threadReaper = new Object() {
            @Override
            protected void finalize() throws Throwable {
                SimpleTimer.this.lock.lock();
                try {
                    SimpleTimer.this.thread.newTasksMayBeScheduled = false;
                    SimpleTimer.this.condition.signal();
                }
                finally {
                    SimpleTimer.this.lock.unlock();
                }
            }
        };
        this.clock = c;
        this.thread = new TimerThread(this.clock, this.lock, this.condition, this.queue);
        if (!c.isJobSchedulable()) {
            this.thread.setName(name);
            this.thread.setDaemon(isDaemon);
            this.thread.start();
            this.reachedJob = null;
        }
        else {
            this.reachedJob = this.clock.createJob((InternalClock.TimeReachedEvent)new InternalClock.TimeReachedEvent() {
                public void timeIsReached(final long _millis) {
                    SimpleTimer.this.timeReachedEvent(_millis);
                }
            });
        }
    }
    
    public void schedule(final SimpleTimerTask task, final long time) {
        if (time < 0L) {
            throw new IllegalArgumentException("Illegal execution time.");
        }
        if (!task.schedule()) {
            throw new IllegalStateException("Task already scheduled or cancelled");
        }
        task.executionTime = time;
        this.lock.lock();
        try {
            if (!this.thread.newTasksMayBeScheduled) {
                throw new IllegalStateException("Timer already cancelled.");
            }
            this.queue.add(task);
            if (this.queue.getMin() == task) {
                this.condition.signal();
                if (this.reachedJob != null) {
                    this.clock.schedule(this.reachedJob, time);
                }
            }
        }
        finally {
            this.lock.unlock();
        }
    }
    
    public void cancel() {
        this.lock.lock();
        try {
            this.thread.newTasksMayBeScheduled = false;
            this.queue.clear();
            this.condition.signal();
            if (this.reachedJob != null) {
                this.clock.disableJob(this.reachedJob);
            }
        }
        finally {
            this.lock.unlock();
        }
    }
    
    public int purge() {
        this.lock.lock();
        try {
            int count = 0;
            for (int i = this.queue.size(); i > 0; --i) {
                if (this.queue.get(i).isCancelled()) {
                    this.queue.quickRemove(i);
                    ++count;
                }
            }
            if (count != 0) {
                this.queue.heapify();
            }
            return count;
        }
        finally {
            this.lock.unlock();
        }
    }
    
    void timeReachedEvent(final long currentTime) {
        SimpleTimerTask task = null;
        while (true) {
            this.lock.lock();
            Label_0122: {
                try {
                    while (!this.queue.isEmpty()) {
                        task = this.queue.getMin();
                        final long executionTime = task.executionTime;
                        final boolean fired = executionTime <= currentTime;
                        if (fired) {
                            this.queue.removeMin();
                            if (!task.execute()) {
                                continue;
                            }
                        }
                        if (!task.isCancelled()) {
                            break Label_0122;
                        }
                        this.queue.removeMin();
                    }
                    return;
                }
                finally {
                    this.lock.unlock();
                }
            }
            if (task.isScheduled()) {
                break;
            }
            task.run();
        }
        this.clock.schedule(this.reachedJob, task.scheduledExecutionTime());
    }
    
    class TimerThread extends Thread
    {
        boolean newTasksMayBeScheduled;
        private TaskQueue queue;
        private final InternalClock clock;
        private final Lock lock;
        private final Condition condition;
        
        TimerThread(final InternalClock clock, final Lock l, final Condition c, final TaskQueue queue) {
            this.newTasksMayBeScheduled = true;
            this.lock = l;
            this.condition = c;
            this.clock = clock;
            this.queue = queue;
            this.setPriority(10);
        }
        
        @Override
        public void run() {
            try {
                this.mainLoop();
            }
            finally {
                this.lock.lock();
                try {
                    this.newTasksMayBeScheduled = false;
                    this.queue.clear();
                }
                finally {
                    this.lock.unlock();
                }
            }
        }
        
        private void mainLoop() {
        Label_0016_Outer:
            while (this.newTasksMayBeScheduled) {
                this.lock.lock();
                SimpleTimerTask firedTask;
                while (true) {
                    try {
                        while (true) {
                            if (this.queue.isEmpty() && this.newTasksMayBeScheduled) {
                                this.condition.await();
                            }
                            else {
                                if (!this.newTasksMayBeScheduled) {
                                    return;
                                }
                                final SimpleTimerTask task = this.queue.getMin();
                                if (task.isCancelled()) {
                                    this.queue.removeMin();
                                }
                                else {
                                    final long currentTime = this.clock.millis();
                                    final long executionTime = task.executionTime;
                                    final boolean fired = executionTime <= currentTime;
                                    if (fired) {
                                        this.queue.removeMin();
                                        if (!task.execute()) {
                                            continue Label_0016_Outer;
                                        }
                                        firedTask = task;
                                        break;
                                    }
                                    else {
                                        this.condition.await(executionTime - currentTime, TimeUnit.MILLISECONDS);
                                    }
                                }
                            }
                        }
                    }
                    catch (InterruptedException ex) {
                        continue;
                    }
                    finally {
                        this.lock.unlock();
                    }
                    break;
                }
                firedTask.run();
            }
        }
    }
    
    class TaskQueue
    {
        private SimpleTimerTask[] queue;
        private int size;
        
        TaskQueue() {
            this.queue = new SimpleTimerTask[128];
            this.size = 0;
        }
        
        int size() {
            return this.size;
        }
        
        void add(final SimpleTimerTask task) {
            if (this.size + 1 == this.queue.length) {
                this.queue = Arrays.copyOf(this.queue, 2 * this.queue.length);
            }
            this.queue[++this.size] = task;
            this.fixUp(this.size);
        }
        
        SimpleTimerTask getMin() {
            return this.queue[1];
        }
        
        SimpleTimerTask get(final int i) {
            return this.queue[i];
        }
        
        void removeMin() {
            this.queue[1] = this.queue[this.size];
            this.queue[this.size--] = null;
            this.fixDown(1);
        }
        
        void quickRemove(final int i) {
            this.queue[i] = this.queue[this.size];
            this.queue[this.size--] = null;
        }
        
        boolean isEmpty() {
            return this.size == 0;
        }
        
        void clear() {
            for (int i = 1; i <= this.size; ++i) {
                this.queue[i] = null;
            }
            this.size = 0;
        }
        
        private void fixUp(int k) {
            while (k > 1) {
                final int j = k >> 1;
                if (this.queue[j].executionTime <= this.queue[k].executionTime) {
                    break;
                }
                final SimpleTimerTask tmp = this.queue[j];
                this.queue[j] = this.queue[k];
                this.queue[k] = tmp;
                k = j;
            }
        }
        
        private void fixDown(int k) {
            int j;
            while ((j = k << 1) <= this.size && j > 0) {
                if (j < this.size && this.queue[j].executionTime > this.queue[j + 1].executionTime) {
                    ++j;
                }
                if (this.queue[k].executionTime <= this.queue[j].executionTime) {
                    break;
                }
                final SimpleTimerTask tmp = this.queue[j];
                this.queue[j] = this.queue[k];
                this.queue[k] = tmp;
                k = j;
            }
        }
        
        void heapify() {
            for (int i = this.size / 2; i >= 1; --i) {
                this.fixDown(i);
            }
        }
    }
}
