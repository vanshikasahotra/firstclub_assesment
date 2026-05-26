package com.firstclub.membership.strategy;

import com.firstclub.membership.entity.CriteriaType;
import com.firstclub.membership.entity.MembershipTier;
import com.firstclub.membership.entity.TierCriteria;
import com.firstclub.membership.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy for evaluating tier eligibility based on combined criteria.
 * All criteria must be met for eligibility.
 */
@Component
public class CombinedStrategy implements TierEvaluationStrategy {

    @Override
    public boolean isEligible(User user, MembershipTier tier) {
        return tier.getCriteria().stream()
            .filter(criteria -> criteria.getCriteriaType() == CriteriaType.COMBINED)
            .anyMatch(criteria -> evaluateCombinedCriteria(user, criteria));
    }

    private boolean evaluateCombinedCriteria(User user, TierCriteria criteria) {
        boolean orderCountMet = true;
        boolean orderValueMet = true;
        boolean cohortMet = true;

        // Check order count
        if (criteria.getMinOrderCount() != null) {
            int userOrderCount = user.getMonthlyOrderCount();
            orderCountMet = userOrderCount >= criteria.getMinOrderCount();
        }

        // Check order value
        if (criteria.getMinOrderValue() != null) {
            BigDecimal userOrderValue = user.getMonthlyOrderValue();
            orderValueMet = userOrderValue.compareTo(criteria.getMinOrderValue()) >= 0;
        }

        // Check cohort
        if (criteria.getCohortName() != null) {
            cohortMet = user.getCohort() != null &&
                       user.getCohort().equalsIgnoreCase(criteria.getCohortName());
        }

        return orderCountMet && orderValueMet && cohortMet;
    }

    @Override
    public int getPriority() {
        return 0; // Highest priority
    }
}
