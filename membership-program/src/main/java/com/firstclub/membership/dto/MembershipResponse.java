package com.firstclub.membership.dto;

import com.firstclub.membership.entity.MembershipStatus;
import com.firstclub.membership.entity.PlanType;
import com.firstclub.membership.entity.TierLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipResponse {
    private Long membershipId;
    private Long userId;
    private String userEmail;
    private PlanType planType;
    private TierLevel tierLevel;
    private MembershipStatus status;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private BigDecimal amountPaid;
    private Boolean autoRenewal;
    private List<BenefitDTO> benefits;
    private Boolean isActive;
    private Boolean isExpired;
}
