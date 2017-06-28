package com.lpmoon.reporter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zblacker on 2017/5/10.
 */

// it is replaced by CodahaleSummary
@Deprecated
public class SimpleSummary extends AbstractSummary {

    private volatile Map<String, AtomicInteger> countSummary = new ConcurrentHashMap<String, AtomicInteger>();
    private volatile Map<String, AtomicInteger> timeSummary = new ConcurrentHashMap<String, AtomicInteger>();

    @Override
    void doInnerStart() {
        new Thread(() -> {
            while (!stopped) {
                try {
                    print();
                    Thread.sleep(2500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    void doInnerStop() {
        countSummary.clear();
        timeSummary.clear();
    }

    @Override
    public String getName() {
        return "simple";
    }

    @Override
    public void report(String clazz, String method, long cost) {
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
        time.addAndGet((int) cost);
    }

    private void print() {
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

    public static SimpleSummary summary = new SimpleSummary();

    public static Summary getInstance() {
        return summary;
    }
}
