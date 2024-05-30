package com.example.hystrix.test;

import com.example.hystrix.conc.variable.HystrixRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
    static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        new Thread(() -> {
            new Test().task();
        }).start();
    }

    public void task() {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            Boolean b1 = new CommandHelloWorld(2).execute();
            Boolean b2 = new CommandHelloWorld(1).execute();
            Boolean b3 = new CommandHelloWorld(2).execute();
            if (b1 == Boolean.TRUE) {
                logger.info("b1 is true");
            }
            if (b2 == Boolean.FALSE) {
                logger.info("b2 is FALSE");
            }
            if (b3 == Boolean.TRUE) {
                logger.info("b3 is true");
            }
        } finally {
            context.shutdown();
        }
    }
}
