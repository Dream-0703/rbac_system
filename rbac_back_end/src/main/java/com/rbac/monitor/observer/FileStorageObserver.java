package com.rbac.monitor.observer;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.event.MonitoringEventType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件级存储观察者：将事件追加写入文件，并保留内存窗口。
 */
public class FileStorageObserver implements MonitoringObserver {
    private final List<MonitoringEvent> buffer = new ArrayList<>();
    private final int maxSize;
    private final String filePath;

    public FileStorageObserver(String filePath, int maxSize) {
        this.filePath = filePath;
        this.maxSize = maxSize;
    }

    @Override
    public boolean supports(MonitoringEventType type) {
        return true;
    }

    @Override
    public synchronized void onEvent(MonitoringEvent event) {
        buffer.add(event);
        if (buffer.size() > maxSize) {
            buffer.remove(0);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(event.toString());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("写入监控文件失败: " + e.getMessage());
        }
    }

    public synchronized List<MonitoringEvent> queryByMetric(String metricName) {
        return buffer.stream()
                .filter(e -> e.getMetricName().equals(metricName))
                .collect(Collectors.toList());
    }

    public synchronized List<MonitoringEvent> queryByTimeRange(Instant from, Instant to) {
        return buffer.stream()
                .filter(e -> !e.getTimestamp().isBefore(from) && !e.getTimestamp().isAfter(to))
                .collect(Collectors.toList());
    }
}

