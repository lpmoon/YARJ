package com.lpmoon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zblacker on 2017/5/10.
 */
public class StatisticsSummary {
    private static volatile Map<String, AtomicInteger> countSummary = new ConcurrentHashMap<String, AtomicInteger>();
    private static volatile Map<String, AtomicInteger> timeSummary = new ConcurrentHashMap<String, AtomicInteger>();

    public static void incrementCount(String clazz, String method) {
        String key = clazz + "_" + method;
        AtomicInteger count = countSummary.get(key);
        if (count == null) {
            synchronized (("count" + "_" + key).intern()) {
                if (countSummary.get(key) == null) {
                    count = new AtomicInteger();
                    countSummary.put(key, count);
                }
            }
        }

        count.incrementAndGet();
    }

    public static void incrementTime(String clazz, String method, long cost) {
        String key = clazz + "_" + method;
        AtomicInteger time = timeSummary.get(key);
        if (time == null) {
            synchronized (("time" + "_" + key).intern()) {
                if (timeSummary.get(key) == null) {
                    time = new AtomicInteger();
                    timeSummary.put(key, time);
                }
            }
        }

        time.addAndGet((int) cost);
    }

    public static void print() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================\n");
        sb.append("调用次数\n");
        for (Map.Entry<String, AtomicInteger> entry : countSummary.entrySet()) {
            sb.append(entry.getKey()).append("总共调用").append(entry.getValue().intValue()).append("\n");
        }

        sb.append("总共耗时\n");
        for (Map.Entry<String, AtomicInteger> entry : timeSummary.entrySet()) {
            sb.append(entry.getKey()).append("总共耗时").append(entry.getValue().intValue()).append("ms\n");
        }
        sb.append("========================\n");
        System.out.println(sb.toString());
    }

    static {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("============================================");
                while (true) {
                    try {
                        StatisticsSummary.print();
                        Thread.sleep(2500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
