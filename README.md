# Hystrix: Latency and Fault Tolerance for Distributed Systems
## Introduction

- This is a rewrite project Hystrix from scracth
- About Hystrix: it is a latency and fault tolerance library designed to isolate points of access to remote systems, services and 3rd party libraries, stop cascading failure and enable resilience in complex distributed systems where failure is inevitable.
- You can know detail about it from ref: https://github.com/Netflix/Hystrix 

## What does it do?

#### 1) Latency and Fault Tolerance
- Support thread isolate (DONE)
  -  TODO: semaphore isolation with circuit breakers 

#### 2) Realtime Operations
- TODO: Realtime monitoring and configuration changes. Watch service and property changes take effect immediately as they spread across a fleet.
Be alerted, make decisions, affect change and see results in seconds.

#### 3) Concurrency
- Parallel execution. Concurrency aware request caching. (DONE)
  - TODO: Automated batching through request collapsing.
  - 
## Hello World!
Code to be isolated is wrapped inside the run() method of a HystrixCommand similar to the following:
```java
public class CommandHelloWorld extends HystrixCommand<String> {
    private final String name;
    public CommandHelloWorld(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.name = name;
    }
    @Override
    protected String run() {
        return "Hello " + name + "!";
    }
}
```

This command could be used like this:

```java
String s = new CommandHelloWorld("Bob").execute();
```

## Demo
Example source code can be found at src/main/java/com/example/hystrix/test/Test.java
After run, you will see output similar to the following:

```
9 [Thread-0] INFO com.example.hystrix.conc.QueueFuture - Not the FIRST TIME START
9 [pool-1-thread-1] INFO com.example.hystrix.test.CommandHelloWorld - pool-1-thread-1
10 [Thread-0] INFO com.example.hystrix.conc.QueueFuture - Not the FIRST TIME START
10 [pool-1-thread-2] INFO com.example.hystrix.test.CommandHelloWorld - pool-1-thread-2
10 [Thread-0] INFO com.example.hystrix.conc.HystrixCommand - get from cache
10 [Thread-0] INFO com.example.hystrix.conc.QueueFuture - Not the FIRST TIME START
10 [Thread-0] INFO com.example.hystrix.conc.QueueFuture - I'm waiting for actualFutureExecuted
11 [Thread-0] INFO com.example.hystrix.test.Test - b1 is true
11 [Thread-0] INFO com.example.hystrix.test.Test - b2 is FALSE
11 [Thread-0] INFO com.example.hystrix.test.Test - b3 is true
```