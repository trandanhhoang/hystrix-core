package com.example.javanica.aop.aspectj;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class HystrixCommandAspect {

    @Before("execution(@com.example.javanica.annotation.HystrixCommand * *(..))")
    public void beforeHystrixCommand() {
        System.out.println("HystrixCommandAspect.beforeHystrixCommand");
    }
}
