package com.example.concurentnetflix.conc;

import com.example.concurentnetflix.conc.concurrency.HystrixConcurrencyStrategy;
import com.example.concurentnetflix.conc.concurrency.HystrixConcurrencyStrategyDefault;
import com.example.concurentnetflix.conc.variable.HystrixRequestVariableDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public abstract class HystrixCommand<R> {
    static final Logger logger = LoggerFactory.getLogger(HystrixCommand.class);
    public final HystrixThreadPool threadPool;
    private final boolean cachingEnable;
    private final HystrixRequestCache requestCache;
    private final HystrixConcurrencyStrategy concurrencyStrategy;

    protected HystrixCommand(HystrixCommandGroupKey key, boolean cachingEnable) {
        this.threadPool = HystrixThreadPool.Factory.getInstace(key.name());
        this.cachingEnable = cachingEnable;
        this.concurrencyStrategy = HystrixConcurrencyStrategyDefault.getInstance();
        this.requestCache = HystrixRequestCache.getInstance(key, this.concurrencyStrategy);
    }

    protected abstract R run();

    protected String getCacheKey() {
        return null;
    }

    // hold thread pool
    // implement callable
    public final R execute() {
        try {
            if (cachingEnable) {
                Future<R> fromCache = requestCache.get(getCacheKey());
                if (fromCache != null) {
                    logger.info("get from cache");
                    return fromCache.get();
                }
            }

            QueueFuture<R> queueFuture = new QueueFuture<>(1000, true, threadPool.getExecutor(), this::run);

            if (cachingEnable) {
                Future<R> fromCache = requestCache.putIfAbsent(getCacheKey(), queueFuture);
                if (fromCache != null){
                    return fromCache.get();
                }
            }

            queueFuture.start();
            return queueFuture.get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
