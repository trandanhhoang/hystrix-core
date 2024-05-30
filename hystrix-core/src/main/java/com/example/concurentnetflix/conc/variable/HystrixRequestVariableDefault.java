package com.example.concurentnetflix.conc.variable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class HystrixRequestVariableDefault<T> implements HystrixRequestVariable<T> {

    static final Logger logger = LoggerFactory.getLogger(HystrixRequestVariableDefault.class);

    @SuppressWarnings("unchecked")
    public static <T> void remove(HystrixRequestContext hystrixRequestContext, HystrixRequestVariableDefault<T> v) {
        LazyInitializer<?> o = hystrixRequestContext.state.remove(v);
        if (o != null) {
            v.shutdown((T) o.get());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        if (HystrixRequestContext.getContextForCurrentThread() == null) {
            throw new IllegalStateException("HystrixRequestContext is not initialized for the current thread");
        }
        ConcurrentHashMap<HystrixRequestVariableDefault<?>, LazyInitializer<?>> variableMap = HystrixRequestContext.getContextForCurrentThread().state;

        LazyInitializer<?> v = variableMap.get(this);
        if (v != null) {
            return (T) v.get();
        }

        // Purpose: Garbage collect l if we lose the thread race.
        LazyInitializer<T> l = new LazyInitializer<T>(this);
        LazyInitializer<?> existing = variableMap.putIfAbsent(this, l);
        if (existing == null) {
            // we won
            return l.get();
        } else {
            //we lost
            return (T) existing.get();
        }
    }

    @Override
    public T initialValue() {
        return null;
    }

    @Override
    public void shutdown(T value) {
        // do nothing by default
    }


    /**
     * Holder for a value that can be derived from the {@link HystrixRequestVariableDefault#initialValue} method that needs
     * to be executed once-and-only-once.
     * <p>
     * This class can be instantiated and garbage collected without calling initialValue()
     * as long as the get() method is not invoked and can thus be used with compareAndSet in
     * ConcurrentHashMap.putIfAbsent and allow "losers" in a thread-race to be discarded.
     *
     * @param <T>
     */
    static final class LazyInitializer<T> {
        private final HystrixRequestVariableDefault<T> rv;
        private T value;
        private boolean initialized = false;

        private LazyInitializer(HystrixRequestVariableDefault<T> rv) {
            this.rv = rv;
        }
        LazyInitializer(HystrixRequestVariableDefault<T> rv, T value) {
            this.rv = rv;
            this.value = value;
            this.initialized = true;
        }

        public synchronized T get() {
            if (!initialized) {
                value = rv.initialValue();
                initialized = true;
            }
            return value;
        }
    }
}
