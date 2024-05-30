package com.example.hystrix.conc.variable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains the state and manages the lifecycle of {@link HystrixRequestVariable} instances, so that multiple thread
 * within single request can share state.
 * <p>
 * If {@link HystrixRequestVariableDefault} is used (directly or indirectly by above-mentioned features)
 * and this context has not been initialized then an {@link IllegalStateException} will be thrown
 **/
public class HystrixRequestContext {

    private static ThreadLocal<HystrixRequestContext> requestVariables = new ThreadLocal<>();

    ConcurrentHashMap<HystrixRequestVariableDefault<?>, HystrixRequestVariableDefault.LazyInitializer<?>> state = new ConcurrentHashMap<>();

    public static boolean isCurrentThreadInitialized() {
        HystrixRequestContext context = requestVariables.get();
        return context != null && context.state != null;
    }

    public static HystrixRequestContext getContextForCurrentThread(){
        HystrixRequestContext context = requestVariables.get();
        if (context != null && context.state != null){
            return context;
        }
        return null;
    }

    // call this at the beginning of each request, from parent thread
    // so any children thread cal be accessible from the parent thread
    // TODO: why ??
    public static HystrixRequestContext initializeContext(){
        HystrixRequestContext context = new HystrixRequestContext();
        requestVariables.set(context);
        return context;
    }

    // instance should be via static factory method
    private HystrixRequestContext(){}

    // TODO: this must be call if initializeContext was called or mem leak will occur
    // TODO: what is initializeContext and why mem leak occur here ???
    public void shutdown(){
        if (state!= null){
            for(HystrixRequestVariableDefault<?> v: state.keySet()){
                try{
                    HystrixRequestVariableDefault.remove(this,v);
                }catch(Throwable t){
                    HystrixRequestVariableDefault.logger.error("Error in shutdown of HystrixRequestVariableDefault",t);
                }
            }
            // null out so it can be garbage collected even if the containing object is still
            // being held in ThreadLocals on threads that weren't cleaned up
            // TODO: what it mean ?
            state = null;
        }
    }
}
