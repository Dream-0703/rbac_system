package com.rbac.monitor.subject;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.observer.MonitoringObserver;
import com.rbac.monitor.util.EventFilter;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 监控事件主题默认实现：线程安全的观察者注册与事件分发。
 */
public class DefaultMonitoringSubject implements MonitoringSubject {
    private final List<MonitoringObserver> observers = new CopyOnWriteArrayList<>();
    private final List<EventFilter> filters = new CopyOnWriteArrayList<>();

    @Override
    public void register(MonitoringObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    @Override
    public void unregister(MonitoringObserver observer) {
        observers.remove(observer);
    }

    /**
    * 注册过滤器，用于事件分发前的过滤/转换。
    */
    public void addFilter(EventFilter filter) {
        if (filter != null) {
            filters.add(filter);
        }
    }

    @Override
    public void publish(MonitoringEvent event) {
        if (event == null) {
            return;
        }
        MonitoringEvent processed = applyFilters(event);
        if (processed == null) {
            return; // 被过滤掉
        }
        for (MonitoringObserver observer : observers) {
            if (observer.supports(processed.getEventType())) {
                try {
                    observer.onEvent(processed);
                } catch (Exception e) {
                    // 避免单个观察者异常影响其他观察者
                    System.err.println("监控观察者处理异常: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private MonitoringEvent applyFilters(MonitoringEvent event) {
        MonitoringEvent current = event;
        for (EventFilter filter : filters) {
            current = filter.filter(current);
            if (current == null) {
                return null;
            }
        }
        return current;
    }
}

