package com.firstclub.membership.strategy;

import com.firstclub.membership.entity.CriteriaType;
import com.firstclub.membership.entity.MembershipTier;
import com.firstclub.membership.entity.User;
import org.springframework.stereotype.Component;

/**
 * Strategy for evaluating tier eligibility based on user cohort.
 */
@Component
public class CohortStrategy implements TierEvaluationStrategy {

    @Override
    public boolean isEligible(User user, MembershipTier tier) {
        if (user.getCohort() == null) {
            return false;
        }

        return tier.getCriteria().stream()
            .filter(criteria -> criteria.getCriteriaType() == CriteriaType.COHORT)
            .anyMatch(criteria -> {
                String requiredCohort = criteria.getCohortName();
                return requiredCohort != null &&
                       requiredCohort.equalsIgnoreCase(user.getCohort());
            });
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
