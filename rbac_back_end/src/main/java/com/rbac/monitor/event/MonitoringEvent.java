package com.rbac.monitor.event;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * 统一的监控事件模型，后续所有采集/分发/处理都以此为载体。
 */
public class MonitoringEvent {
    private MonitoringEventType eventType;
    private Instant timestamp;
    private String metricName;
    private double metricValue;
    private Map<String, String> tags;
    private String source;
    private String severity; // 仅在告警/审计类事件下使用
    private String message;

    public MonitoringEvent(MonitoringEventType eventType,
                           Instant timestamp,
                           String metricName,
                           double metricValue,
                           Map<String, String> tags,
                           String source,
                           String severity,
                           String message) {
        this.eventType = eventType;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.tags = tags == null ? Collections.emptyMap() : Collections.unmodifiableMap(tags);
        this.source = source;
        this.severity = severity;
        this.message = message;
    }

    public MonitoringEventType getEventType() {
        return eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMetricName() {
        return metricName;
    }

    public double getMetricValue() {
        return metricValue;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getSource() {
        return source;
    }

    public String getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "MonitoringEvent{" +
                "eventType=" + eventType +
                ", timestamp=" + timestamp +
                ", metricName='" + metricName + '\'' +
                ", metricValue=" + metricValue +
                ", tags=" + tags +
                ", source='" + source + '\'' +
                ", severity='" + severity + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

