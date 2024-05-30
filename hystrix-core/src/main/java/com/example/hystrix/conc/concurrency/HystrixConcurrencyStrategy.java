package com.example.hystrix.conc.concurrency;

import com.example.hystrix.conc.variable.HystrixRequestVariable;
import com.example.hystrix.conc.variable.HystrixRequestVariableDefault;
import com.example.hystrix.conc.variable.HystrixRequestVariableLifecycle;

/**
 * Custom implementations of this interface can be used to override default behavior via 2 mechanisms:
 * 1. Injection:
 * Implementation can be injected into HystrixCommand
 * 2. Plugin
 * Using HystrixPlugins.registerConcurrencyStrategy an implementation can be registered globally to take precedence and override all other implementations.
 * // TODO implement it lately
 */
public abstract class HystrixConcurrencyStrategy {

    public <T> HystrixRequestVariable<T> getRequestVariable(final HystrixRequestVariableLifecycle<T> rv) {
        return new HystrixRequestVariableDefault<T>() {
            @Override
            public T initialValue() {
                return rv.initialValue();
            }

            public void shutdown(T value) {
                rv.shutdown(value);
            };
        };
    }
}
