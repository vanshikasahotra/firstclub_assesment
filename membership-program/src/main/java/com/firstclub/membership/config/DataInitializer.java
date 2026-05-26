package com.firstclub.membership.config;

import com.firstclub.membership.entity.*;
import com.firstclub.membership.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing membership data...");

        initializePlans();
        initializeTiers();
        initializeSampleUsers();

        log.info("Data initialization completed!");
    }

    private void initializePlans() {
        log.info("Creating membership plans...");

        MembershipPlan monthly = MembershipPlan.builder()
            .planType(PlanType.MONTHLY)
            .price(new BigDecimal("99.00"))
            .durationInDays(30)
            .description("Monthly subscription with flexible cancellation")
            .active(true)
            .build();

        MembershipPlan quarterly = MembershipPlan.builder()
            .planType(PlanType.QUARTERLY)
            .price(new BigDecimal("249.00"))
            .durationInDays(90)
            .description("Quarterly subscription - Save 16%!")
            .active(true)
            .build();

        MembershipPlan yearly = MembershipPlan.builder()
            .planType(PlanType.YEARLY)
            .price(new BigDecimal("899.00"))
            .durationInDays(365)
            .description("Yearly subscription - Best value, Save 25%!")
            .active(true)
            .build();

        planRepository.save(monthly);
        planRepository.save(quarterly);
        planRepository.save(yearly);

        log.info("Created {} membership plans", 3);
    }

    private void initializeTiers() {
        log.info("Creating membership tiers with benefits and criteria...");

        // SILVER TIER - Entry level (default)
        MembershipTier silver = MembershipTier.builder()
            .level(TierLevel.SILVER)
            .priority(1)
            .description("Entry-level membership with basic benefits")
            .build();
        silver = tierRepository.save(silver);

        createBenefit(silver, BenefitType.DISCOUNT, "5% discount on selected items",
                     new BigDecimal("5.00"), false, false, false, false);
        createBenefit(silver, BenefitType.FREE_DELIVERY, "Free delivery on orders above $50",
                     null, true, false, false, false);

        log.info("Created SILVER tier");

        // GOLD TIER - Mid level
        MembershipTier gold = MembershipTier.builder()
            .level(TierLevel.GOLD)
            .priority(2)
            .description("Mid-tier membership with enhanced benefits")
            .build();
        gold = tierRepository.save(gold);

        createBenefit(gold, BenefitType.DISCOUNT, "10% discount on selected items",
                     new BigDecimal("10.00"), false, false, false, false);
        createBenefit(gold, BenefitType.FREE_DELIVERY, "Free delivery on all orders",
                     null, true, false, false, false);
        createBenefit(gold, BenefitType.EXCLUSIVE_DEALS, "Access to exclusive weekly deals",
                     null, false, true, false, false);

        // GOLD tier criteria
        createCriteria(gold, CriteriaType.ORDER_COUNT, 5, null, null,
                      "Complete 5 orders in a month");
        createCriteria(gold, CriteriaType.ORDER_VALUE, null, new BigDecimal("500.00"), null,
                      "Spend $500 in a month");
        createCriteria(gold, CriteriaType.COHORT, null, null, "VIP",
                      "Be part of VIP cohort");

        log.info("Created GOLD tier");

        // PLATINUM TIER - Premium level
        MembershipTier platinum = MembershipTier.builder()
            .level(TierLevel.PLATINUM)
            .priority(3)
            .description("Premium membership with all benefits")
            .build();
        platinum = tierRepository.save(platinum);

        createBenefit(platinum, BenefitType.DISCOUNT, "15% discount on all items",
                     new BigDecimal("15.00"), false, false, false, false);
        createBenefit(platinum, BenefitType.FREE_DELIVERY, "Free express delivery on all orders",
                     null, true, false, false, false);
        createBenefit(platinum, BenefitType.EXCLUSIVE_DEALS, "Access to premium exclusive deals",
                     null, false, true, false, false);
        createBenefit(platinum, BenefitType.EARLY_ACCESS, "Early access to all sales and new products",
                     null, false, false, true, false);
        createBenefit(platinum, BenefitType.PRIORITY_SUPPORT, "24/7 priority customer support",
                     null, false, false, false, true);

        // PLATINUM tier criteria - Combined criteria (all must be met)
        createCriteria(platinum, CriteriaType.COMBINED, 10, new BigDecimal("1000.00"), null,
                      "Complete 10 orders AND spend $1000 in a month");
        createCriteria(platinum, CriteriaType.COHORT, null, null, "PREMIUM",
                      "Be part of PREMIUM cohort");

        log.info("Created PLATINUM tier");
    }

    private void createBenefit(MembershipTier tier, BenefitType type, String description,
                              BigDecimal discountPercentage, Boolean freeDelivery,
                              Boolean exclusiveDeals, Boolean earlyAccess, Boolean prioritySupport) {
        TierBenefit benefit = TierBenefit.builder()
            .tier(tier)
            .benefitType(type)
            .description(description)
            .discountPercentage(discountPercentage)
            .freeDelivery(freeDelivery)
            .exclusiveDeals(exclusiveDeals)
            .earlyAccessToSales(earlyAccess)
            .prioritySupport(prioritySupport)
            .build();

        tier.getBenefits().add(benefit);
    }

    private void createCriteria(MembershipTier tier, CriteriaType type, Integer minOrderCount,
                               BigDecimal minOrderValue, String cohort, String description) {
        TierCriteria criteria = TierCriteria.builder()
            .tier(tier)
            .criteriaType(type)
            .minOrderCount(minOrderCount)
            .minOrderValue(minOrderValue)
            .cohortName(cohort)
            .description(description)
            .build();

        tier.getCriteria().add(criteria);
    }

    private void initializeSampleUsers() {
        log.info("Creating sample users...");

        User user1 = User.builder()
            .email("john.doe@example.com")
            .name("John Doe")
            .cohort("REGULAR")
            .build();

        User user2 = User.builder()
            .email("jane.smith@example.com")
            .name("Jane Smith")
            .cohort("VIP")
            .build();

        User user3 = User.builder()
            .email("bob.wilson@example.com")
            .name("Bob Wilson")
            .cohort("PREMIUM")
            .build();

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        log.info("Created {} sample users", 3);
    }
}
