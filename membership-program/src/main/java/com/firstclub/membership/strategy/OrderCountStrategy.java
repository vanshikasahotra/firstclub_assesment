package com.firstclub.membership.strategy;

import com.firstclub.membership.entity.CriteriaType;
import com.firstclub.membership.entity.MembershipTier;
import com.firstclub.membership.entity.User;
import org.springframework.stereotype.Component;

/**
 * Strategy for evaluating tier eligibility based on order count.
 */
@Component
public class OrderCountStrategy implements TierEvaluationStrategy {

    @Override
    public boolean isEligible(User user, MembershipTier tier) {
        return tier.getCriteria().stream()
            .filter(criteria -> criteria.getCriteriaType() == CriteriaType.ORDER_COUNT)
            .anyMatch(criteria -> {
                int userOrderCount = user.getMonthlyOrderCount();
                Integer requiredCount = criteria.getMinOrderCount();
                return requiredCount != null && userOrderCount >= requiredCount;
            });
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
