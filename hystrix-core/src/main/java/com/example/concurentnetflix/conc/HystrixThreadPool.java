package com.example.concurentnetflix.conc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public interface HystrixThreadPool {
    ThreadPoolExecutor getExecutor();
    class Factory{
        private Factory(){};
        private static ConcurrentHashMap<String,HystrixThreadPool> threadPools = new ConcurrentHashMap<>();

        public static HystrixThreadPool getInstace(String key){
            return threadPools.computeIfAbsent(key, k -> new HystrixThreadPoolDefault(0));
        }

        public static class HystrixThreadPoolDefault implements HystrixThreadPool {
            private final ThreadPoolExecutor threadPool;
            private final BlockingQueue<Runnable> queue;

            public HystrixThreadPoolDefault(int maxQueueSize) {
                if (maxQueueSize <= 0) {
                    // reject request
                    this.queue =  new SynchronousQueue<>();
                } else {
                    // limited queue size
                    this.queue = new LinkedBlockingQueue<>(maxQueueSize);
                }
                this.threadPool = new ThreadPoolExecutor(10,10, 1, TimeUnit.MINUTES, this.queue);
            }

            @Override
            public ThreadPoolExecutor getExecutor() {
                return threadPool;
            }
        }
    }
}
