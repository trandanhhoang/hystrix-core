package com.example.concurentnetflix.conc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueueFuture<K> implements Future<K> {
    /* GROUP config ****************** */
    private static final Logger logger = LoggerFactory.getLogger(QueueFuture.class);
    private final long timeout;
    private final boolean executionIsolationThreadInterruptOnTimeout;

    /* GROUP action ****************** */
    private final ThreadPoolExecutor executor;
    private final Callable<K> callable;
    // this field is a wraper of executor and wraper of callable
    private volatile Future<K> actualFuture = null; // have state so need volatile
    private volatile K result; // have state so need volatile
    /* GROUP condition ****************** */

    // check is start only 1 time, that mean put callable into executor 1 time
    private final AtomicBoolean started = new AtomicBoolean(false);

    // check is future is get() only 1 time
    private final AtomicBoolean actualFutureExecuted = new AtomicBoolean(false);

    // await when get() times 2,3,4. count down when first actualGet() done
    private final CountDownLatch actualResponseRecieved = new CountDownLatch(1);

    public QueueFuture(long timeout, boolean executionIsolationThreadInterruptOnTimeout, ThreadPoolExecutor executor, Callable<K> callable) {
        this.timeout = timeout;
        this.executionIsolationThreadInterruptOnTimeout = executionIsolationThreadInterruptOnTimeout;
        this.executor = executor;
        this.callable = callable;
    }

    /**
     * Route all get() to get(long timeout, TimeUnit unit) to avoid blocking
     */
    @Override
    public K get() throws InterruptedException, ExecutionException, CancellationException{
        return get(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Start execution of Callable on ThreadExecutor
     */
    public void start(){
        if(started.compareAndSet(false, true)){
            // TODO: wrap thread pool and callable later
            actualFuture = executor.submit(callable);
        }else{
            logger.info("Not the FIRST TIME START");
            // TODO, count down latch
            // how to force another thread wating on this thread
        }
    }


    @Override
    public K get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, CancellationException {
        // incase another thread come here by cache, try to start it
        start();
        if (actualFutureExecuted.compareAndSet(false,true)) {
            performActualGet();
        }else{
            // waiting
            logger.info("I'm wating for actualFutureExecuted");
            actualResponseRecieved.await();
        }
        return result;
    }

    private void performActualGet() throws ExecutionException, InterruptedException {
        try{
            if(!started.get() || actualFuture == null){
                logger.error("Future not started");
                throw new IllegalStateException("Future not started");
            }
            result = actualFuture.get(timeout, TimeUnit.MILLISECONDS);
        }catch(TimeoutException e){
            // If the task has already started, then the mayInterruptIfRunning parameter
            // determines whether the thread executing this task
            // (when known by the implementation) is interrupted in an attempt
            // to stop the task.
            actualFuture.cancel(executionIsolationThreadInterruptOnTimeout);
        }finally {
            // mark that we are done and other threads can proceed
            actualResponseRecieved.countDown();
        }
    }

    /**
     * We don't want to allow canceling
     */
    @Override
    public boolean cancel(boolean b) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return actualFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return actualFuture.isDone();
    }
}
