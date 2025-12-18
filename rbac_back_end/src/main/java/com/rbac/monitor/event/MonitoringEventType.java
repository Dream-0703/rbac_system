package com.rbac.monitor.event;

/**
 * 监控事件类型枚举，覆盖系统、应用、业务、告警等类别。
 */
public enum MonitoringEventType {
    SYSTEM_METRIC,   // 系统指标：CPU、内存、IO、网络
    APP_METRIC,      // 应用性能：方法耗时、调用次数、异常率
    BIZ_METRIC,      // 业务指标：转化率、成功率等
    ALERT,           // 告警事件
    AUDIT            // 审计事件/安全事件
}

