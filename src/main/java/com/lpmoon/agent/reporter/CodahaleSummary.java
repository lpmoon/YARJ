package com.lpmoon.agent.reporter;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.TimeUnit;

/**
 * Created by zblacker on 2017/6/29.
 *
 * 基于Codahale的时间分布统计
 */
public class CodahaleSummary extends AbstractSummary {

    private MetricRegistry metrics;
    private ConsoleReporter reporter;

    @Override
    void doInnerStart() {
        metrics = new MetricRegistry();
        reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        reporter.start(1, TimeUnit.SECONDS);
    }

    @Override
    void doInnerStop() {
        reporter.stop();
        reporter.close();
    }

    @Override
    public String getName() {
        return "Codahale";
    }

    @Override
    public void report(String className, String method, long cost) {
        Histogram histogram = metrics.histogram(className + "." + method);
        histogram.update(cost);
    }

    public static CodahaleSummary summary = new CodahaleSummary();

    public static Summary getInstance() {
        return summary;
    }
}
