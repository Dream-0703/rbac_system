package com.rbac.monitor.service;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.event.MonitoringEventType;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 应用性能采集器：执行一次受监控的操作，采集耗时与异常标记。
 * 适合包裹关键业务方法；若需 AOP，可在切面中构造该采集器并调用 collect。
 */
public class AppMetricCollector implements MetricCollector {
    private final String metricName;
    private final Callable<?> action;
    private final String source;

    public AppMetricCollector(String metricName, Callable<?> action, String source) {
        this.metricName = metricName;
        this.action = action;
        this.source = source;
    }

    @Override
    public MonitoringEvent collect() {
        long start = System.nanoTime();
        boolean error = false;
        String message = "success";
        try {
            action.call();
        } catch (Exception e) {
            error = true;
            message = "error: " + e.getMessage();
        }
        long durationMs = (System.nanoTime() - start) / 1_000_000;
        return new MonitoringEvent(
                MonitoringEventType.APP_METRIC,
                Instant.now(),
                metricName,
                durationMs,
                Map.of(
                        "durationMs", String.valueOf(durationMs),
                        "error", String.valueOf(error)
                ),
                source,
                error ? "ERROR" : "INFO",
                message
        );
    }
}

