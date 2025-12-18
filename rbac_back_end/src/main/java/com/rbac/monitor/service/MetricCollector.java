package com.rbac.monitor.service;

import com.rbac.monitor.event.MonitoringEvent;

/**
 * 监控采集器接口：负责从特定数据源采集并产出标准事件。
 */
public interface MetricCollector {
    /**
     * 采集并返回事件；无事件可返回 null。
     */
    MonitoringEvent collect();
}

