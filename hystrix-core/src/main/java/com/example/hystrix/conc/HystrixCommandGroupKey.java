package com.example.hystrix.conc;

import java.util.concurrent.ConcurrentHashMap;

public interface HystrixCommandGroupKey {
    String name();

    class Factory {
        private Factory() {}

        private static ConcurrentHashMap<String, HystrixCommandGroupKey> intern = new ConcurrentHashMap<>();

        public static HystrixCommandGroupKey asKey(String name) {
            HystrixCommandGroupKey key = intern.get(name);
            if (key == null) {
                intern.putIfAbsent(name, new HystrixCommandGroupKeyDefault(name));
            }
            return intern.get(name);
        }

        private static class HystrixCommandGroupKeyDefault implements HystrixCommandGroupKey {
            private final String name;

            public HystrixCommandGroupKeyDefault(String name) {
                this.name = name;
            }

            @Override
            public String name() {
                return name;
            }
        }


    }
}
