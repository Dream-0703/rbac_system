package com.rbac.monitor.subject;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.observer.MonitoringObserver;
import com.rbac.monitor.util.EventFilter;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 异步主题：通过队列 + 工作线程进行分发，保证顺序且降低调用方阻塞。
 */
public class AsyncMonitoringSubject implements MonitoringSubject, AutoCloseable {
    private final List<MonitoringObserver> observers = new CopyOnWriteArrayList<>();
    private final List<EventFilter> filters = new CopyOnWriteArrayList<>();
    private final BlockingQueue<MonitoringEvent> queue = new LinkedBlockingQueue<>(10000);
    private final Thread worker;
    private volatile boolean running = true;

    public AsyncMonitoringSubject() {
        worker = new Thread(this::loop, "monitor-dispatcher");
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void register(MonitoringObserver observer) {
        if (observer != null) observers.add(observer);
    }

    @Override
    public void unregister(MonitoringObserver observer) {
        observers.remove(observer);
    }

    public void addFilter(EventFilter filter) {
        if (filter != null) filters.add(filter);
    }

    @Override
    public void publish(MonitoringEvent event) {
        if (!running || event == null) return;
        queue.offer(event); // 若满则丢弃旧数据可改用 offer with timeout
    }

    private void loop() {
        while (running) {
            try {
                MonitoringEvent event = queue.take();
                MonitoringEvent processed = applyFilters(event);
                if (processed == null) continue;
                for (MonitoringObserver observer : observers) {
                    if (observer.supports(processed.getEventType())) {
                        try {
                            observer.onEvent(processed);
                        } catch (Exception e) {
                            System.err.println("异步监控观察者处理异常: " + e.getMessage());
                        }
                    }
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ignored) {
            }
        }
    }

    private MonitoringEvent applyFilters(MonitoringEvent event) {
        MonitoringEvent current = event;
        for (EventFilter filter : filters) {
            current = filter.filter(current);
            if (current == null) return null;
        }
        return current;
    }

    @Override
    public void close() {
        running = false;
        worker.interrupt();
    }
}

