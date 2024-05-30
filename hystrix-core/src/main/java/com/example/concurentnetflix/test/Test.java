package com.example.concurentnetflix.test;

import com.example.concurentnetflix.conc.variable.HystrixRequestContext;

public class Test {
    public static void main(String[] args) {
        new Thread(()-> {
            new Test().task("1");
        }).start();

        new Thread(()-> {
            new Test().task("2");
        }).start();

    }

    public void task(String task){
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            Boolean b1 = new CommandHelloWorld(2).execute();
//            Boolean b2 = new CommandUsingRequestCache(1).execute();
//            Boolean b3 = new CommandUsingRequestCache(2).execute();
            if (b1 == Boolean.TRUE) {
                System.out.println("b1 is true");
            }
        }finally {
            context.shutdown();
        }
    }
}
