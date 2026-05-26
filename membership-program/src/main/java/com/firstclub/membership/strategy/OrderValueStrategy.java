package com.firstclub.membership.strategy;

import com.firstclub.membership.entity.CriteriaType;
import com.firstclub.membership.entity.MembershipTier;
import com.firstclub.membership.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy for evaluating tier eligibility based on order value.
 */
@Component
public class OrderValueStrategy implements TierEvaluationStrategy {

    @Override
    public boolean isEligible(User user, MembershipTier tier) {
        return tier.getCriteria().stream()
            .filter(criteria -> criteria.getCriteriaType() == CriteriaType.ORDER_VALUE)
            .anyMatch(criteria -> {
                BigDecimal userOrderValue = user.getMonthlyOrderValue();
                BigDecimal requiredValue = criteria.getMinOrderValue();
                return requiredValue != null &&
                       userOrderValue.compareTo(requiredValue) >= 0;
            });
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
