package com.firstclub.membership.strategy;

import com.firstclub.membership.entity.MembershipTier;
import com.firstclub.membership.entity.User;

/**
 * Strategy interface for evaluating tier eligibility.
 * Allows for pluggable tier calculation logic.
 */
public interface TierEvaluationStrategy {
    /**
     * Evaluates if a user is eligible for a given tier
     */
    boolean isEligible(User user, MembershipTier tier);

    /**
     * Gets the priority of this strategy (lower = higher priority)
     */
    int getPriority();
}
