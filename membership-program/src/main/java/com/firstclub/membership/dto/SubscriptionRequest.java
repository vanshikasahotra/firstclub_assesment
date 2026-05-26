package com.firstclub.membership.dto;

import com.firstclub.membership.entity.PlanType;
import com.firstclub.membership.entity.TierLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @Email(message = "Valid email is required")
    private String userEmail;

    @NotNull(message = "Plan type is required")
    private PlanType planType;

    @NotNull(message = "Tier level is required")
    private TierLevel tierLevel;

    private Boolean autoRenewal;
}
