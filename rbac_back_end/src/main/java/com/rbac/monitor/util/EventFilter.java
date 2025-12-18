package com.rbac.monitor.util;

import com.rbac.monitor.event.MonitoringEvent;

/**
 * 事件过滤/转换接口，可用于在分发前丢弃或修改事件。
 */
@FunctionalInterface
public interface EventFilter {
    /**
     * @param event 输入事件
     * @return 返回 null 表示丢弃；返回事件本身或新的事件用于继续分发。
     */
    MonitoringEvent filter(MonitoringEvent event);
}

