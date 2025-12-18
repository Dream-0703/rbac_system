package com.rbac.monitor.util;

/**
 * 简单告警规则：阈值 + 比较符。
 */
public class AlertRule {
    public enum Operator { GT, GE, LT, LE }

    private final double threshold;
    private final Operator operator;

    public AlertRule(double threshold, Operator operator) {
        this.threshold = threshold;
        this.operator = operator;
    }

    public boolean match(double value) {
        return switch (operator) {
            case GT -> value > threshold;
            case GE -> value >= threshold;
            case LT -> value < threshold;
            case LE -> value <= threshold;
        };
    }
}

