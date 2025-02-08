/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.aspect.utils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracted from https://github.com/jmock-developers/jmock-library
 * The shutdown logic was modified. Shutdown is still not fully supported. 
 * 
 * A {@link ScheduledExecutorService} that executes commands on the thread that calls
 * {@link #runNextPendingCommand() runNextPendingCommand}, {@link #runUntilIdle() runUntilIdle} or 
 * {@link #tick(long, TimeUnit) tick}.  Objects of this class can also be used
 * as {@link Executor}s or {@link ExecutorService}s if you just want to control background execution 
 * and don't need to schedule commands.
 * 
 * @author nat
 */
public class DeterministicScheduler implements ScheduledExecutorService {

    private static final Logger LOG = LoggerFactory.getLogger(DeterministicScheduler.class);
    private final DeltaQueue<ScheduledTask<?>> deltaQueue = new DeltaQueue<ScheduledTask<?>>();
    
    /**
     * Runs time forwards by a given duration, executing any commands scheduled for
     * execution during that time period, and any background tasks spawned by the 
     * scheduled tasks.  Therefore, when a call to tick returns, the executor 
     * will be idle.
     * 
     * @param duration
     * @param timeUnit
     */
    public void tick(long duration, TimeUnit timeUnit) {
        long remaining = toTicks(duration, timeUnit);
        
        do {
            remaining = deltaQueue.tick(remaining);
            runUntilIdle();
            
        } while (deltaQueue.isNotEmpty() && remaining > 0);
    }
    
    /**
     * Runs all commands scheduled to be executed immediately but does 
     * not tick time forward.
     */
    public void runUntilIdle() {
        while (!isIdle()) {
            runNextPendingCommand();
        }
    }
    
    /**
     * Runs the next command scheduled to be executed immediately.
     */
    public void runNextPendingCommand() {
        ScheduledTask<?> scheduledTask = deltaQueue.pop();
        
        scheduledTask.run();
        
        if (!scheduledTask.isCancelled() && scheduledTask.repeats()) {
            deltaQueue.add(scheduledTask.repeatDelay, scheduledTask);
        }
    }
    
    /**
     * Reports whether scheduler is "idle": has no commands pending immediate execution.
     * 
     * @return true if there are no commands pending immediate execution,
     *         false if there are commands pending immediate execution.
     */
    public boolean isIdle() {
        return deltaQueue.isEmpty() || deltaQueue.delay() > 0;
    }
    
    public void execute(Runnable command) {
        schedule(command, 0, TimeUnit.SECONDS);
    }
    
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        ScheduledTask<Void> task = new ScheduledTask<Void>(command);
        deltaQueue.add(toTicks(delay, unit), task);
        return task;
    }
    
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        ScheduledTask<V> task = new ScheduledTask<V>(callable);
        deltaQueue.add(toTicks(delay, unit), task);
        return task;
    }
    
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduleWithFixedDelay(command, initialDelay, period, unit);
    }
    
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        ScheduledTask<Object> task = new ScheduledTask<Object>(toTicks(delay, unit), command);
        deltaQueue.add(toTicks(initialDelay, unit), task);
        return task;
    }
    
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        // see javadoc of the class
        LOG.warn("shutdown not supported in DeterministicScheduler. This class is used for tests only.");
        return true;
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw blockingOperationsNotSupported();
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        throw blockingOperationsNotSupported();
    }
    
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException 
    {
        throw blockingOperationsNotSupported();
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) 
        throws InterruptedException, ExecutionException, TimeoutException 
    {
        throw blockingOperationsNotSupported();
    }

    public boolean isShutdown() {
        throw shutdownNotSupported();
    }

    public boolean isTerminated() {
        throw shutdownNotSupported();
    }
    
    public void shutdown() {
        // see javadoc of the class
        LOG.warn("shutdown not supported in DeterministicScheduler. This class is used for tests only. This operation is a no-op.");
    }

    public List<Runnable> shutdownNow() {
        throw shutdownNotSupported();
    }
    
    public <T> Future<T> submit(Callable<T> callable) {
        return schedule(callable, 0, TimeUnit.SECONDS);
    }
    
    public Future<?> submit(Runnable command) {
        return submit(command, null);
    }
    
    public <T> Future<T> submit(Runnable command, T result) {
        return submit(new CallableRunnableAdapter<T>(command, result));
    }
    
    private final class CallableRunnableAdapter<T> implements Callable<T> {
        private final Runnable runnable;
        private final T result;
        
        public CallableRunnableAdapter(Runnable runnable, T result) {
            this.runnable = runnable;
            this.result = result;
        }
        
        @Override
        public String toString() {
            return runnable.toString();
        }

        public T call() throws Exception {
            runnable.run();
            return result;
        }
    }
    
    private final class ScheduledTask<T> implements ScheduledFuture<T>, Runnable {
        public final long repeatDelay;
        public final Callable<T> command;
        private boolean isCancelled = false;
        private boolean isDone = false;
        private T futureResult;
        private Exception failure = null;
        
        public ScheduledTask(Callable<T> command) {
            this.repeatDelay = -1;
            this.command = command;
        }
        
        public ScheduledTask(Runnable command) {
            this(-1, command);
        }
        
        public ScheduledTask(long repeatDelay, Runnable command) {
            this.repeatDelay = repeatDelay;
            this.command = new CallableRunnableAdapter<T>(command, null); 
        }
        
        @Override
        public String toString() {
            return command.toString() + " repeatDelay=" + repeatDelay;
        }
        
        public boolean repeats() {
            return repeatDelay >= 0;
        }

        public long getDelay(TimeUnit unit) {
            return unit.convert(deltaQueue.delay(this), TimeUnit.MILLISECONDS);
        }

        public int compareTo(Delayed o) {
            throw new UnsupportedOperationException("not supported");
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            isCancelled = true;
            return deltaQueue.remove(this);
        }

        public T get() throws InterruptedException, ExecutionException {
            if (!isDone) {
                throw blockingOperationsNotSupported();
            }
            
            if (failure != null) {
                throw new ExecutionException(failure);
            }
            
            return futureResult;
        }

        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
        
        public boolean isCancelled() {
            return isCancelled;
        }
        
        public boolean isDone() {
            return isDone;
        }

        public void run() {
            try {
                futureResult = command.call();
            }
            catch (Exception e) {
                failure = e;
            }
            isDone = true;
        }
    }

    private long toTicks(long duration, TimeUnit timeUnit) {
        return TimeUnit.MILLISECONDS.convert(duration, timeUnit);
    }
    
    private UnsupportedOperationException blockingOperationsNotSupported() {
        return new UnsupportedOperationException("cannot perform blocking wait on a task scheduled on a " + DeterministicScheduler.class.getName());
    }
    
    private UnsupportedOperationException shutdownNotSupported() {
        return new UnsupportedOperationException("shutdown not supported");
    }
}
