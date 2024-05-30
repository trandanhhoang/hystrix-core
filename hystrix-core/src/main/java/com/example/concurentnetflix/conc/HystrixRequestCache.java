package com.example.concurentnetflix.conc;

import com.example.concurentnetflix.conc.concurrency.HystrixConcurrencyStrategy;
import com.example.concurentnetflix.conc.variable.HystrixRequestVariableHolder;
import com.example.concurentnetflix.conc.variable.HystrixRequestVariableLifecycle;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class HystrixRequestCache {

    private final RequestCacheKey rckey;

    private final HystrixConcurrencyStrategy concurrencyStrategy;

    private final static ConcurrentHashMap<RequestCacheKey, HystrixRequestCache> caches = new ConcurrentHashMap<>();

    private static final HystrixRequestVariableHolder<ConcurrentHashMap<ValueCacheKey, Future<?>>> requestVariableForCache =
            new HystrixRequestVariableHolder<>(new HystrixRequestVariableLifecycle<>() {

                @Override
                public ConcurrentHashMap<ValueCacheKey, Future<?>> initialValue() {
                    return new ConcurrentHashMap<>();
                }

                @Override
                public void shutdown(ConcurrentHashMap<ValueCacheKey, Future<?>> value) {
                    // nothing to shutdown
                }
            });

    private HystrixRequestCache(RequestCacheKey rckey, HystrixConcurrencyStrategy concurrencyStrategy) {
        this.rckey = rckey;
        this.concurrencyStrategy = concurrencyStrategy;
    }

    public static HystrixRequestCache getInstance(HystrixCommandGroupKey key, HystrixConcurrencyStrategy concurrencyStrategy) {
        RequestCacheKey rcKey = new RequestCacheKey(key.name());
        HystrixRequestCache c = caches.get(rcKey);
        if (c == null) {
            HystrixRequestCache newRequestCache = new HystrixRequestCache(rcKey, concurrencyStrategy);
            HystrixRequestCache exist = caches.putIfAbsent(rcKey, newRequestCache);
            // or maybe we just return cache.get(rcKey) instead
            if (exist == null) {
                c = newRequestCache;
            } else {
                c = exist;
            }
        }
        return c;
    }

    public <R> Future<R> get(String cacheKey) {
        ValueCacheKey key = getRequestCacheKey(cacheKey);
        if (key != null) {
            return (Future<R>) requestVariableForCache.get(concurrencyStrategy).get(key);
        }
        return null;
    }

    /**
     * Put the Future in the cache if it does not already exist.
     * If this method returns a non-null value then another thread won the race and it should be returned instead of proceeding with execution of the new Future.
     */
    @SuppressWarnings("unchecked")
    public <T> Future<T> putIfAbsent(String cacheKey, Future<T> f) {
        ValueCacheKey key = getRequestCacheKey(cacheKey);
        if (key != null) {
            /* look for the stored value */
            Future<T> alreadySet = (Future<T>) requestVariableForCache.get(concurrencyStrategy).putIfAbsent(key, f);
            if (alreadySet != null) {
                // someone beat us so we didn't cache this
                return alreadySet;
            }
        }
        // we either set it in the cache or do not have a cache key
        return null;
    }

    public void clear(String cacheKey) {
        ValueCacheKey key = getRequestCacheKey(cacheKey);
        if (key != null) {
            requestVariableForCache.get(concurrencyStrategy).remove(key);
        }
    }

    /**
     * Request CacheKey: HystrixRequestCache.prefix + concurrencyStrategy + HystrixCommand.getCacheKey (as injected via get/put to this class)
     *
     * @return ValueCacheKey
     */
    private ValueCacheKey getRequestCacheKey(String cacheKey) {
        if (cacheKey != null) {
            return new ValueCacheKey(rckey, cacheKey);
        } else {
            return null;
        }
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    private static class ValueCacheKey {
        private final RequestCacheKey rcKey;
        private final String valueCacheKey;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private static class RequestCacheKey {
        private final String key;
    }
}
