package com.rbac.monitor.service;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.event.MonitoringEventType;

import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 业务指标采集器：通过函数式 Supplier 取得业务指标值，封装为事件。
 */
public class BizMetricCollector implements MetricCollector {
    private final String metricName;
    private final Supplier<Double> metricSupplier;
    private final String source;

    public BizMetricCollector(String metricName, Supplier<Double> metricSupplier, String source) {
        this.metricName = metricName;
        this.metricSupplier = metricSupplier;
        this.source = source;
    }

    @Override
    public MonitoringEvent collect() {
        Double value = metricSupplier.get();
        if (value == null) {
            return null;
        }
        return new MonitoringEvent(
                MonitoringEventType.BIZ_METRIC,
                Instant.now(),
                metricName,
                value,
                Map.of(),
                source,
                "INFO",
                "biz metric collected"
        );
    }
}

