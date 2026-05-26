package com.firstclub.membership.strategy;

import com.firstclub.membership.entity.MembershipTier;
import com.firstclub.membership.entity.TierLevel;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.repository.MembershipTierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service for evaluating the appropriate tier for a user.
 * Uses Strategy pattern to apply multiple evaluation strategies.
 * Thread-safe for concurrent tier evaluations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TierEvaluator {

    private final List<TierEvaluationStrategy> strategies;
    private final MembershipTierRepository tierRepository;

    /**
     * Evaluates and returns the highest tier the user is eligible for.
     * This method is thread-safe and can be called concurrently.
     *
     * @param user The user to evaluate
     * @return The highest eligible tier
     */
    public synchronized MembershipTier evaluateTier(User user) {
        log.info("Evaluating tier for user: {}", user.getId());

        List<MembershipTier> allTiers = tierRepository.findAll();

        // Sort tiers by priority (descending)
        allTiers.sort(Comparator.comparing((MembershipTier t) -> t.getLevel().getPriority()).reversed());

        // Sort strategies by priority (ascending - lower is higher priority)
        List<TierEvaluationStrategy> sortedStrategies = strategies.stream()
            .sorted(Comparator.comparingInt(TierEvaluationStrategy::getPriority))
            .toList();

        // Find the highest tier the user is eligible for
        for (MembershipTier tier : allTiers) {
            if (isUserEligibleForTier(user, tier, sortedStrategies)) {
                log.info("User {} is eligible for tier: {}", user.getId(), tier.getLevel());
                return tier;
            }
        }

        // Default to SILVER tier
        log.info("User {} defaulting to SILVER tier", user.getId());
        return tierRepository.findByLevel(TierLevel.SILVER)
            .orElseThrow(() -> new RuntimeException("SILVER tier not found"));
    }

    /**
     * Checks if user is eligible for a specific tier.
     * Thread-safe for concurrent evaluations.
     */
    private boolean isUserEligibleForTier(User user, MembershipTier tier, List<TierEvaluationStrategy> strategies) {
        // If tier has no criteria, it's the default tier (SILVER)
        if (tier.getCriteria() == null || tier.getCriteria().isEmpty()) {
            return tier.getLevel() == TierLevel.SILVER;
        }

        // Check if any strategy confirms eligibility
        for (TierEvaluationStrategy strategy : strategies) {
            if (strategy.isEligible(user, tier)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a user can upgrade to a specific tier level.
     */
    public boolean canUpgradeToTier(User user, TierLevel targetLevel) {
        MembershipTier eligibleTier = evaluateTier(user);
        return eligibleTier.getLevel().getPriority() >= targetLevel.getPriority();
    }
}
