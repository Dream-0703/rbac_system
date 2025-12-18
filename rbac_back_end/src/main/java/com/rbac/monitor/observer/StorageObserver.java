package com.rbac.monitor.observer;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.event.MonitoringEventType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 存储观察者：将事件持久到内存列表，提供查询。
 * 便于实现“多级存储策略”和“历史数据查询分析”的基础。
 */
public class StorageObserver implements MonitoringObserver {
    private final List<MonitoringEvent> buffer = new ArrayList<>();
    private final int maxSize;

    public StorageObserver(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean supports(MonitoringEventType type) {
        return true;
    }

    @Override
    public synchronized void onEvent(MonitoringEvent event) {
        buffer.add(event);
        if (buffer.size() > maxSize) {
            buffer.remove(0); // 简单的 FIFO 截断
        }
    }

    public synchronized List<MonitoringEvent> queryByMetric(String metricName) {
        return buffer.stream()
                .filter(e -> e.getMetricName().equals(metricName))
                .collect(Collectors.toList());
    }

    public synchronized List<MonitoringEvent> queryByTimeRange(Instant from, Instant to) {
        return buffer.stream()
                .filter(e -> !e.getTimestamp().isBefore(from) && !e.getTimestamp().isAfter(to))
                .collect(Collectors.toList());
    }
}

