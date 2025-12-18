package com.rbac.monitor.observer;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.event.MonitoringEventType;

/**
 * 观察者接口：关注特定类型的监控事件并处理。
 */
public interface MonitoringObserver {
    /**
     * 是否支持该事件类型，默认全量支持。
     */
    default boolean supports(MonitoringEventType type) {
        return true;
    }

    /**
     * 处理事件。
     */
    void onEvent(MonitoringEvent event);
}

