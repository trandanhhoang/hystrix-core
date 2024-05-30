package com.example.hystrix.test;

import com.example.hystrix.conc.HystrixCommand;
import com.example.hystrix.conc.HystrixCommandGroupKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandHelloWorld extends HystrixCommand<Boolean> {

    static final Logger logger = LoggerFactory.getLogger(CommandHelloWorld.class);
    private final int value;

    public CommandHelloWorld(int value) {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"), true);
        this.value = value;
    }

    @Override
    protected String getCacheKey() {
        return String.valueOf(value);
    }

    @Override
    protected Boolean run() {
        logger.info(Thread.currentThread().getName());
        // call third party
        return value == 0 || value % 2 == 0;
    }
}
