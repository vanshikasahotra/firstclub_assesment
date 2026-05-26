package com.firstclub.membership.dto;

import com.firstclub.membership.entity.BenefitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitDTO {
    private BenefitType benefitType;
    private String description;
    private BigDecimal discountPercentage;
    private Boolean freeDelivery;
    private Boolean exclusiveDeals;
    private Boolean earlyAccessToSales;
    private Boolean prioritySupport;
    private String additionalInfo;
}
