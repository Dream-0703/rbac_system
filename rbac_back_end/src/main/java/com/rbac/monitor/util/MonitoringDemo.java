package com.rbac.monitor.util;

import com.rbac.monitor.observer.AggregationObserver;
import com.rbac.monitor.observer.AlertObserver;
import com.rbac.monitor.observer.AppMetricStatsObserver;
import com.rbac.monitor.observer.FileStorageObserver;
import com.rbac.monitor.observer.LogObserver;
import com.rbac.monitor.observer.StorageObserver;
import com.rbac.monitor.service.AppMetricCollector;
import com.rbac.monitor.service.BizMetricCollector;
import com.rbac.monitor.service.CollectorFacade;
import com.rbac.monitor.service.MetricCollector;
import com.rbac.monitor.service.SystemMetricCollector;
import com.rbac.monitor.subject.AsyncMonitoringSubject;
import com.rbac.monitor.subject.MonitoringSubject;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实验用 Demo：启动主题、注册观察者、定时采集系统/业务指标并发布。
 */
public class MonitoringDemo {
    public static void main(String[] args) {
        MonitoringSubject subject = new AsyncMonitoringSubject(); // 异步分发，满足性能/顺序要求

        // 注册观察者
        subject.register(new LogObserver());           // 打印所有事件
        AlertObserver alertObserver = new AlertObserver(80, 5);
        subject.register(alertObserver);               // 阈值告警
        AggregationObserver aggObserver = new AggregationObserver();
        subject.register(aggObserver);                 // 实时聚合
        StorageObserver storageObserver = new StorageObserver(500);
        subject.register(storageObserver);             // 缓存历史查询
        FileStorageObserver fileStorageObserver = new FileStorageObserver("monitoring.log", 500);
        subject.register(fileStorageObserver);         // 文件级持久化
        AppMetricStatsObserver appStatsObserver = new AppMetricStatsObserver();
        subject.register(appStatsObserver);            // 应用级统计

        // 构造采集器
        AtomicInteger orderSuccess = new AtomicInteger(90);
        MetricCollector sysCollector = new SystemMetricCollector("demo-host");
        MetricCollector bizCollector = new BizMetricCollector(
                "order.success.rate",
                () -> (double) orderSuccess.getAndAdd(-1), // 每次递减，触发告警演示
                "biz-service"
        );
        MetricCollector appCollector = new AppMetricCollector(
                "api.latency",
                () -> {
                    // 模拟接口耗时与偶发异常
                    Thread.sleep(100);
                    if (orderSuccess.get() % 5 == 0) throw new RuntimeException("mock error");
                    return null;
                },
                "app-service"
        );
        CollectorFacade facade = new CollectorFacade(List.of(sysCollector, bizCollector, appCollector), subject);

        // 定时任务：每 2 秒采集一次
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(facade::pollAndPublish, 0, 2, TimeUnit.SECONDS);

        // Demo 运行 10 秒后退出
        scheduler.schedule(() -> {
            System.out.println("Demo finished.");
            System.out.println("Agg snapshot: " + aggObserver.snapshot());
            System.out.println("Stored events count: " + storageObserver.queryByMetric("cpu.load").size());
            System.out.println("App stats: " + appStatsObserver.snapshot());
            scheduler.shutdownNow();
        }, 10, TimeUnit.SECONDS);
    }
}

