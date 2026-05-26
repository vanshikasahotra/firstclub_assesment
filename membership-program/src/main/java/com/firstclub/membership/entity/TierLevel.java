package com.firstclub.membership.entity;

public enum TierLevel {
    SILVER(1),
    GOLD(2),
    PLATINUM(3);

    private final int priority;

    TierLevel(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
