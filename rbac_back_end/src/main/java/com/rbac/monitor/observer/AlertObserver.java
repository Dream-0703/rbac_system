package com.rbac.monitor.observer;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.event.MonitoringEventType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rbac.monitor.util.AlertRule;

/**
 * 告警观察者：基于简单阈值规则触发告警（实验用）。
 */
public class AlertObserver implements MonitoringObserver {
    private final double defaultThreshold;
    // 支持自定义告警规则：metricName -> rule
    private final Map<String, AlertRule> rules = new ConcurrentHashMap<>();
    // 滑动窗口用于趋势/均值判断
    private final Map<String, SlidingWindow> windows = new ConcurrentHashMap<>();
    private final int windowSize;

    public AlertObserver(double defaultThreshold) {
        this(defaultThreshold, 5);
    }

    public AlertObserver(double defaultThreshold, int windowSize) {
        this.defaultThreshold = defaultThreshold;
        this.windowSize = windowSize;
    }

    @Override
    public boolean supports(MonitoringEventType type) {
        // 对系统、应用、业务指标都做告警判断
        return type == MonitoringEventType.SYSTEM_METRIC
                || type == MonitoringEventType.APP_METRIC
                || type == MonitoringEventType.BIZ_METRIC;
    }

    @Override
    public void onEvent(MonitoringEvent event) {
        double threshold = resolveThreshold(event.getMetricName(), event.getTags(), defaultThreshold);
        if (matchRule(event.getMetricName(), event.getMetricValue(), threshold)) {
            System.out.println("[ALERT] metric=" + event.getMetricName()
                    + ", value=" + event.getMetricValue()
                    + ", threshold=" + threshold
                    + ", source=" + event.getSource()
                    + ", message=" + event.getMessage());
        }
    }

    public void addRule(String metricName, AlertRule rule) {
        rules.put(metricName, rule);
    }

    private double resolveThreshold(String metric, Map<String, String> tags, double fallback) {
        if (rules.containsKey(metric)) {
            // 自定义规则中阈值
            return fallback; // 实际阈值在 rule 中使用，fallback 仅为打印
        }
        if (tags == null) {
            return fallback;
        }
        try {
            String override = tags.get("threshold");
            if (override != null) {
                return Double.parseDouble(override);
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private boolean matchRule(String metric, double value, double threshold) {
        AlertRule rule = rules.get(metric);
        if (rule != null) {
            return rule.match(value);
        }
        // 窗口均值判断
        double avg = windows.computeIfAbsent(metric, k -> new SlidingWindow(windowSize)).addAndAvg(value);
        return value >= threshold || avg >= threshold;
    }

    private static class SlidingWindow {
        private final double[] data;
        private int idx = 0;
        private int filled = 0;

        SlidingWindow(int size) { data = new double[size]; }

        synchronized double addAndAvg(double v) {
            data[idx] = v;
            idx = (idx + 1) % data.length;
            if (filled < data.length) filled++;
            double sum = 0;
            for (int i = 0; i < filled; i++) sum += data[i];
            return sum / filled;
        }
    }
}

