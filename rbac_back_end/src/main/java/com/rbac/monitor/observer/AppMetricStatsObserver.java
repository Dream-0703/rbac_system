package com.rbac.monitor.observer;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.event.MonitoringEventType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 应用指标统计：统计调用次数、错误次数、最大/平均耗时。
 * 依赖 APP_METRIC 事件的 metricValue 为耗时，tags 中的 error 标识错误。
 */
public class AppMetricStatsObserver implements MonitoringObserver {
    public static class Stat {
        private long count;
        private long errorCount;
        private double sumLatency;
        private double maxLatency;

        synchronized void add(double latency, boolean error) {
            count++;
            if (error) errorCount++;
            sumLatency += latency;
            maxLatency = Math.max(maxLatency, latency);
        }

        public synchronized long getCount() { return count; }
        public synchronized long getErrorCount() { return errorCount; }
        public synchronized double getAvgLatency() { return count == 0 ? 0 : sumLatency / count; }
        public synchronized double getMaxLatency() { return maxLatency; }
        public synchronized double getErrorRate() { return count == 0 ? 0 : (double) errorCount / count; }
    }

    private final Map<String, Stat> stats = new ConcurrentHashMap<>();

    @Override
    public boolean supports(MonitoringEventType type) {
        return type == MonitoringEventType.APP_METRIC;
    }

    @Override
    public void onEvent(MonitoringEvent event) {
        boolean error = Boolean.parseBoolean(event.getTags().getOrDefault("error", "false"));
        double latency = event.getMetricValue();
        stats.computeIfAbsent(event.getMetricName(), k -> new Stat()).add(latency, error);
    }

    public Map<String, Stat> snapshot() {
        return Map.copyOf(stats);
    }
}

