package com.firstclub.membership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tier_benefits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BenefitType benefitType;

    @Column(nullable = false)
    private String description;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column
    private Boolean freeDelivery;

    @Column
    private Boolean exclusiveDeals;

    @Column
    private Boolean earlyAccessToSales;

    @Column
    private Boolean prioritySupport;

    @Column(length = 1000)
    private String additionalInfo;
}
