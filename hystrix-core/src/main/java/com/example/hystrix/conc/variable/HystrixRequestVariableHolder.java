package com.example.hystrix.conc.variable;

import com.example.hystrix.conc.concurrency.HystrixConcurrencyStrategy;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.concurrent.ConcurrentHashMap;

// layer between actual HystrixRequestVariable and calling code to allow inject implementation of HystrixConcurrencyStrategy
public class HystrixRequestVariableHolder<T> {
    private static ConcurrentHashMap<RVCacheKey, HystrixRequestVariable<?>> requestVariableInstaces = new ConcurrentHashMap<>();

    private final HystrixRequestVariableLifecycle<T> lifeCycleMethod;

    public HystrixRequestVariableHolder(HystrixRequestVariableLifecycle<T> hystrixRequestVariableLifecycle) {
        this.lifeCycleMethod = hystrixRequestVariableLifecycle;
    }

    public T get(HystrixConcurrencyStrategy concurrencyStrategy){
        RVCacheKey key = new RVCacheKey(this);
        HystrixRequestVariable<?> rv = requestVariableInstaces.get(key);

        if (rv == null) {
            requestVariableInstaces.putIfAbsent(key,concurrencyStrategy.getRequestVariable(lifeCycleMethod));
        }

        return (T) requestVariableInstaces.get(key).get();
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    private static class RVCacheKey {

        private final HystrixRequestVariableHolder<?> rvHolder;
    }
}
