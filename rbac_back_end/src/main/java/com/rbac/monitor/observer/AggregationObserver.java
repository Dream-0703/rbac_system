package com.rbac.monitor.observer;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.event.MonitoringEventType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聚合观察者：实时做简单统计（count/sum/max/min/avg），用于“实时聚合与计算”要求。
 */
public class AggregationObserver implements MonitoringObserver {
    public static class Stat {
        private long count;
        private double sum;
        private double max = Double.NEGATIVE_INFINITY;
        private double min = Double.POSITIVE_INFINITY;

        synchronized void add(double v) {
            count++;
            sum += v;
            max = Math.max(max, v);
            min = Math.min(min, v);
        }

        public synchronized long getCount() { return count; }
        public synchronized double getSum() { return sum; }
        public synchronized double getMax() { return max; }
        public synchronized double getMin() { return min; }
        public synchronized double getAvg() { return count == 0 ? 0 : sum / count; }
    }

    private final Map<String, Stat> stats = new ConcurrentHashMap<>();

    @Override
    public boolean supports(MonitoringEventType type) {
        return true; // 对所有事件做聚合
    }

    @Override
    public void onEvent(MonitoringEvent event) {
        stats.computeIfAbsent(event.getMetricName(), k -> new Stat())
                .add(event.getMetricValue());
    }

    public Map<String, Stat> snapshot() {
        return Map.copyOf(stats);
    }
}

