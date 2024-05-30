package com.example.hystrix.conc.variable;

/**
 * Interface for a variable similar to {@link ThreadLocal} but scoped at the user request level.
 */
public interface HystrixRequestVariable<T> extends HystrixRequestVariableLifecycle<T> {

    public T get();
}
