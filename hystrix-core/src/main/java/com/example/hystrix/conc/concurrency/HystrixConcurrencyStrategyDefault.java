package com.example.hystrix.conc.concurrency;


public class HystrixConcurrencyStrategyDefault extends HystrixConcurrencyStrategy{
    private static HystrixConcurrencyStrategy INSTANCE = new HystrixConcurrencyStrategyDefault();

    public static HystrixConcurrencyStrategy getInstance(){
        return INSTANCE;
    }

    private HystrixConcurrencyStrategyDefault(){
    }

}
