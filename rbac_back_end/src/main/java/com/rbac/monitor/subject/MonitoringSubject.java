package com.rbac.monitor.subject;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.observer.MonitoringObserver;

/**
 * 监控事件主题接口：负责注册观察者和分发事件。
 */
public interface MonitoringSubject {
    void register(MonitoringObserver observer);

    void unregister(MonitoringObserver observer);

    /**
     * 发布事件到主题，内部会分发给所有支持的观察者。
     */
    void publish(MonitoringEvent event);
}

