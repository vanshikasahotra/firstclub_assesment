package com.firstclub.membership.entity;

public enum PlanType {
    MONTHLY(30),
    QUARTERLY(90),
    YEARLY(365);

    private final int durationInDays;

    PlanType(int durationInDays) {
        this.durationInDays = durationInDays;
    }

    public int getDurationInDays() {
        return durationInDays;
    }
}
