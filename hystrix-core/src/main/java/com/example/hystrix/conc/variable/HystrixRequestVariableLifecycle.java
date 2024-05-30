package com.example.hystrix.conc.variable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public interface HystrixRequestVariableLifecycle<T> {
    T initialValue();

    void shutdown(T value);
}
