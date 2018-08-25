package com.lpmoon.agent.reporter;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by zblacker on 2017/6/29.
 *
 * 基于Codahale的时间分布统计
 */
public class CodahaleSummary {

    private MetricRegistry metrics;
    private ConsoleReporter reporter;
    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;
    private Object lock = new Object();

    public void start() {
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
        metrics = new MetricRegistry();
        reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .outputTo(printStream)
                .build();
    }

    public void stop() {
        reporter.stop();
        reporter.close();
    }

    public void reportHistogram(String key, long cost) {
        Histogram histogram = metrics.histogram(key);
        histogram.update(cost);
    }

    public String getSummary() {
        // 避免并发reset
        synchronized (lock) {
            reporter.report();
            String data = new String(outputStream.toByteArray());
            outputStream.reset();
            return data;
        }
    }
}
