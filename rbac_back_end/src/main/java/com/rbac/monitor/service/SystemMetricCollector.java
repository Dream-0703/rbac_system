package com.rbac.monitor.service;

import com.rbac.monitor.event.MonitoringEvent;
import com.rbac.monitor.event.MonitoringEventType;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

/**
 * 系统指标采集器：采集CPU负载、可用内存等基础指标。
 */
public class SystemMetricCollector implements MetricCollector {
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final String source;
    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hal = systemInfo.getHardware();

    // 用于吞吐计算的上次采样值
    private final AtomicLong prevNetBytes = new AtomicLong(-1);
    private final AtomicLong prevDiskRead = new AtomicLong(-1);
    private final AtomicLong prevDiskWrite = new AtomicLong(-1);
    private final AtomicLong prevTs = new AtomicLong(-1);

    public SystemMetricCollector(String source) {
        this.source = source;
    }

    @Override
    public MonitoringEvent collect() {
        double cpuLoad = readCpuLoad();
        double freeMemoryMb = readFreeMemoryMb();
        double totalMemoryMb = readTotalMemoryMb();
        double diskFreeGb = readDiskFreeGb();
        double netMbps = readNetworkMbps();
        double diskReadMBps = readDiskReadMBps();
        double diskWriteMBps = readDiskWriteMBps();

        return new MonitoringEvent(
                MonitoringEventType.SYSTEM_METRIC,
                Instant.now(),
                "cpu.load",
                cpuLoad,
                Map.of(
                        "freeMemoryMb", String.valueOf(freeMemoryMb),
                        "totalMemoryMb", String.valueOf(totalMemoryMb),
                        "diskFreeGb", String.valueOf(diskFreeGb),
                        "netMbps", String.valueOf(netMbps),
                        "diskReadMBps", String.valueOf(diskReadMBps),
                        "diskWriteMBps", String.valueOf(diskWriteMBps)
                ),
                source,
                "INFO",
                "System CPU/memory/disk snapshot"
        );
    }

    private double readCpuLoad() {
        try {
            // com.sun.management.OperatingSystemMXBean 才有 getSystemCpuLoad
            if (osBean instanceof com.sun.management.OperatingSystemMXBean extended) {
                double load = extended.getSystemCpuLoad();
                return load < 0 ? 0 : load * 100; // 转百分比
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    private double readFreeMemoryMb() {
        try {
            GlobalMemory memory = hal.getMemory();
            return memory.getAvailable() / 1024.0 / 1024.0;
        } catch (Exception ignored) {
        }
        return -1;
    }

    private double readTotalMemoryMb() {
        try {
            GlobalMemory memory = hal.getMemory();
            return memory.getTotal() / 1024.0 / 1024.0;
        } catch (Exception ignored) {
        }
        return -1;
    }

    private double readDiskFreeGb() {
        try {
            long free = 0;
            long total = 0;
            for (var root : java.io.File.listRoots()) {
                free += root.getFreeSpace();
                total += root.getTotalSpace();
            }
            return free / 1024.0 / 1024.0 / 1024.0;
        } catch (Exception ignored) {
        }
        return -1;
    }

    private double readNetworkMbps() {
        try {
            long now = System.currentTimeMillis();
            List<NetworkIF> nics = hal.getNetworkIFs();
            long sum = 0;
            for (NetworkIF nic : nics) {
                nic.updateAttributes();
                sum += nic.getBytesRecv() + nic.getBytesSent();
            }
            long lastSum = prevNetBytes.getAndSet(sum);
            long lastTs = prevTs.getAndSet(now);
            if (lastSum < 0 || lastTs < 0) return -1;
            long deltaBytes = sum - lastSum;
            long deltaMs = now - lastTs;
            if (deltaMs <= 0) return -1;
            double bytesPerSec = deltaBytes * 1000.0 / deltaMs;
            return bytesPerSec * 8 / 1_000_000.0; // Mbps
        } catch (Exception ignored) {
        }
        return -1;
    }

    private double readDiskReadMBps() {
        try {
            long now = System.currentTimeMillis();
            List<HWDiskStore> disks = hal.getDiskStores();
            long sumRead = 0;
            for (HWDiskStore d : disks) {
                d.updateAttributes();
                sumRead += d.getReadBytes();
            }
            long lastRead = prevDiskRead.getAndSet(sumRead);
            long lastTs = prevTs.get();
            if (lastRead < 0 || lastTs < 0) return -1;
            long delta = sumRead - lastRead;
            long deltaMs = now - lastTs;
            if (deltaMs <= 0) return -1;
            double bytesPerSec = delta * 1000.0 / deltaMs;
            return bytesPerSec / 1024.0 / 1024.0;
        } catch (Exception ignored) {
        }
        return -1;
    }

    private double readDiskWriteMBps() {
        try {
            long now = System.currentTimeMillis();
            List<HWDiskStore> disks = hal.getDiskStores();
            long sumWrite = 0;
            for (HWDiskStore d : disks) {
                d.updateAttributes();
                sumWrite += d.getWriteBytes();
            }
            long lastWrite = prevDiskWrite.getAndSet(sumWrite);
            long lastTs = prevTs.get();
            if (lastWrite < 0 || lastTs < 0) return -1;
            long delta = sumWrite - lastWrite;
            long deltaMs = now - lastTs;
            if (deltaMs <= 0) return -1;
            double bytesPerSec = delta * 1000.0 / deltaMs;
            return bytesPerSec / 1024.0 / 1024.0;
        } catch (Exception ignored) {
        }
        return -1;
    }
}

