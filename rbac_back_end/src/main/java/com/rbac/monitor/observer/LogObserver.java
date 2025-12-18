package com.rbac.monitor.observer;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.event.MonitoringEventType;

/**
 * 简单日志观察者：将监控事件输出到标准输出，便于实验观察。
 */
public class LogObserver implements MonitoringObserver {
    private final MonitoringEventType focusType; // 可选：只关心某类事件

    public LogObserver() {
        this.focusType = null;
    }

    public LogObserver(MonitoringEventType focusType) {
        this.focusType = focusType;
    }

    @Override
    public boolean supports(MonitoringEventType type) {
        return focusType == null || focusType == type;
    }

    @Override
    public void onEvent(MonitoringEvent event) {
        System.out.println("[LOG] " + event);
    }
}

