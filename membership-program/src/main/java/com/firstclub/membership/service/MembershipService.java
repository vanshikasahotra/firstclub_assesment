package com.firstclub.membership.service;

import com.firstclub.membership.dto.*;
import com.firstclub.membership.entity.*;
import com.firstclub.membership.exception.MembershipException;
import com.firstclub.membership.exception.ResourceNotFoundException;
import com.firstclub.membership.repository.*;
import com.firstclub.membership.strategy.TierEvaluator;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing membership subscriptions with concurrency support.
 * Uses optimistic locking to handle concurrent tier updates safely.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@EnableRetry
public class MembershipService {

    private final UserRepository userRepository;
    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final UserMembershipRepository membershipRepository;
    private final TierEvaluator tierEvaluator;

    /**
     * Subscribe a user to a membership plan.
     * Thread-safe with optimistic locking.
     */
    @Transactional
    @Retryable(
        retryFor = {OptimisticLockException.class, ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100)
    )
    public MembershipResponse subscribe(SubscriptionRequest request) {
        log.info("Processing subscription for user: {}", request.getUserId());

        User user = getUserById(request.getUserId());

        // Check if user already has an active membership
        membershipRepository.findByUserId(user.getId()).ifPresent(existing -> {
            if (existing.isActive()) {
                throw new MembershipException("User already has an active membership");
            }
        });

        MembershipPlan plan = planRepository.findByPlanType(request.getPlanType())
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + request.getPlanType()));

        MembershipTier tier = tierRepository.findByLevel(request.getTierLevel())
            .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + request.getTierLevel()));

        // Validate user is eligible for requested tier
        if (!tierEvaluator.canUpgradeToTier(user, request.getTierLevel())) {
            throw new MembershipException("User not eligible for tier: " + request.getTierLevel());
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusDays(plan.getDurationInDays());

        UserMembership membership = UserMembership.builder()
            .user(user)
            .plan(plan)
            .tier(tier)
            .status(MembershipStatus.ACTIVE)
            .startDate(now)
            .expiryDate(expiry)
            .amountPaid(plan.getPrice())
            .autoRenewal(request.getAutoRenewal() != null ? request.getAutoRenewal() : false)
            .build();

        UserMembership saved = membershipRepository.save(membership);
        log.info("Subscription created successfully for user: {}", user.getId());

        return mapToResponse(saved);
    }

    /**
     * Upgrade user tier with optimistic locking for concurrency control.
     */
    @Transactional
    @Retryable(
        retryFor = {OptimisticLockException.class, ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100)
    )
    public MembershipResponse upgradeTier(TierUpgradeRequest request) {
        log.info("Processing tier upgrade for user: {} to tier: {}", request.getUserId(), request.getNewTierLevel());

        User user = getUserById(request.getUserId());

        // Use optimistic locking to prevent concurrent modifications
        UserMembership membership = membershipRepository.findByUserIdWithLock(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("No active membership found for user"));

        if (!membership.isActive()) {
            throw new MembershipException("Cannot upgrade inactive membership");
        }

        MembershipTier currentTier = membership.getTier();
        TierLevel currentLevel = currentTier.getLevel();
        TierLevel newLevel = request.getNewTierLevel();

        // Validate it's actually an upgrade
        if (newLevel.getPriority() <= currentLevel.getPriority()) {
            throw new MembershipException("New tier must be higher than current tier");
        }

        // Check eligibility
        if (!tierEvaluator.canUpgradeToTier(user, newLevel)) {
            throw new MembershipException("User not eligible for tier: " + newLevel);
        }

        MembershipTier newTier = tierRepository.findByLevel(newLevel)
            .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + newLevel));

        membership.setTier(newTier);
        UserMembership updated = membershipRepository.save(membership);

        log.info("Tier upgraded successfully for user: {} to tier: {}", user.getId(), newLevel);
        return mapToResponse(updated);
    }

    /**
     * Downgrade user tier with optimistic locking.
     */
    @Transactional
    @Retryable(
        retryFor = {OptimisticLockException.class, ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100)
    )
    public MembershipResponse downgradeTier(TierUpgradeRequest request) {
        log.info("Processing tier downgrade for user: {} to tier: {}", request.getUserId(), request.getNewTierLevel());

        User user = getUserById(request.getUserId());

        UserMembership membership = membershipRepository.findByUserIdWithLock(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("No active membership found for user"));

        if (!membership.isActive()) {
            throw new MembershipException("Cannot downgrade inactive membership");
        }

        TierLevel currentLevel = membership.getTier().getLevel();
        TierLevel newLevel = request.getNewTierLevel();

        // Validate it's actually a downgrade
        if (newLevel.getPriority() >= currentLevel.getPriority()) {
            throw new MembershipException("New tier must be lower than current tier");
        }

        MembershipTier newTier = tierRepository.findByLevel(newLevel)
            .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + newLevel));

        membership.setTier(newTier);
        UserMembership updated = membershipRepository.save(membership);

        log.info("Tier downgraded successfully for user: {} to tier: {}", user.getId(), newLevel);
        return mapToResponse(updated);
    }

    /**
     * Cancel user membership.
     */
    @Transactional
    public MembershipResponse cancelMembership(Long userId) {
        log.info("Cancelling membership for user: {}", userId);

        User user = getUserById(userId);

        UserMembership membership = membershipRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for user"));

        if (membership.getStatus() == MembershipStatus.CANCELLED) {
            throw new MembershipException("Membership already cancelled");
        }

        membership.setStatus(MembershipStatus.CANCELLED);
        membership.setAutoRenewal(false);
        UserMembership cancelled = membershipRepository.save(membership);

        log.info("Membership cancelled successfully for user: {}", userId);
        return mapToResponse(cancelled);
    }

    /**
     * Get current membership for a user.
     */
    @Transactional(readOnly = true)
    public MembershipResponse getCurrentMembership(Long userId) {
        User user = getUserById(userId);

        UserMembership membership = membershipRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for user"));

        return mapToResponse(membership);
    }

    /**
     * Auto-evaluate and upgrade tier based on user activity.
     * Thread-safe with optimistic locking.
     */
    @Transactional
    @Retryable(
        retryFor = {OptimisticLockException.class, ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100)
    )
    public MembershipResponse autoEvaluateTier(Long userId) {
        log.info("Auto-evaluating tier for user: {}", userId);

        User user = getUserById(userId);

        UserMembership membership = membershipRepository.findByUserIdWithLock(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for user"));

        if (!membership.isActive()) {
            throw new MembershipException("Cannot evaluate tier for inactive membership");
        }

        MembershipTier eligibleTier = tierEvaluator.evaluateTier(user);

        // Only upgrade if eligible tier is higher
        if (eligibleTier.getLevel().getPriority() > membership.getTier().getLevel().getPriority()) {
            membership.setTier(eligibleTier);
            UserMembership updated = membershipRepository.save(membership);
            log.info("Tier auto-upgraded for user: {} to tier: {}", userId, eligibleTier.getLevel());
            return mapToResponse(updated);
        }

        log.info("No tier change for user: {}, current tier is optimal", userId);
        return mapToResponse(membership);
    }

    /**
     * Get all available membership plans.
     */
    @Transactional(readOnly = true)
    public List<PlanDTO> getAllPlans() {
        return planRepository.findByActiveTrue().stream()
            .map(this::mapToPlanDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get all membership tiers.
     */
    @Transactional(readOnly = true)
    public List<TierDTO> getAllTiers() {
        return tierRepository.findAll().stream()
            .map(this::mapToTierDTO)
            .collect(Collectors.toList());
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private MembershipResponse mapToResponse(UserMembership membership) {
        List<BenefitDTO> benefits = membership.getTier().getBenefits().stream()
            .map(this::mapToBenefitDTO)
            .collect(Collectors.toList());

        return MembershipResponse.builder()
            .membershipId(membership.getId())
            .userId(membership.getUser().getId())
            .userEmail(membership.getUser().getEmail())
            .planType(membership.getPlan().getPlanType())
            .tierLevel(membership.getTier().getLevel())
            .status(membership.getStatus())
            .startDate(membership.getStartDate())
            .expiryDate(membership.getExpiryDate())
            .amountPaid(membership.getAmountPaid())
            .autoRenewal(membership.getAutoRenewal())
            .benefits(benefits)
            .isActive(membership.isActive())
            .isExpired(membership.isExpired())
            .build();
    }

    private BenefitDTO mapToBenefitDTO(TierBenefit benefit) {
        return BenefitDTO.builder()
            .benefitType(benefit.getBenefitType())
            .description(benefit.getDescription())
            .discountPercentage(benefit.getDiscountPercentage())
            .freeDelivery(benefit.getFreeDelivery())
            .exclusiveDeals(benefit.getExclusiveDeals())
            .earlyAccessToSales(benefit.getEarlyAccessToSales())
            .prioritySupport(benefit.getPrioritySupport())
            .additionalInfo(benefit.getAdditionalInfo())
            .build();
    }

    private PlanDTO mapToPlanDTO(MembershipPlan plan) {
        return PlanDTO.builder()
            .id(plan.getId())
            .planType(plan.getPlanType())
            .price(plan.getPrice())
            .durationInDays(plan.getDurationInDays())
            .description(plan.getDescription())
            .build();
    }

    private TierDTO mapToTierDTO(MembershipTier tier) {
        List<BenefitDTO> benefits = tier.getBenefits().stream()
            .map(this::mapToBenefitDTO)
            .collect(Collectors.toList());

        return TierDTO.builder()
            .id(tier.getId())
            .level(tier.getLevel())
            .priority(tier.getPriority())
            .description(tier.getDescription())
            .benefits(benefits)
            .build();
    }
}
