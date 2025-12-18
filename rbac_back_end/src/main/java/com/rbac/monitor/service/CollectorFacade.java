package com.rbac.monitor.service;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.subject.MonitoringSubject;

import java.util.List;

/**
 * 采集入口：聚合多个采集器，将事件统一发布到主题。
 */
public class CollectorFacade {
    private final List<MetricCollector> collectors;
    private final MonitoringSubject subject;

    public CollectorFacade(List<MetricCollector> collectors, MonitoringSubject subject) {
        this.collectors = collectors;
        this.subject = subject;
    }

    /**
     * 轮询所有采集器并发布事件。
     */
    public void pollAndPublish() {
        for (MetricCollector collector : collectors) {
            if (collector == null) {
                continue;
            }
            MonitoringEvent event = collector.collect();
            if (event != null) {
                subject.publish(event);
            }
        }
    }
}

