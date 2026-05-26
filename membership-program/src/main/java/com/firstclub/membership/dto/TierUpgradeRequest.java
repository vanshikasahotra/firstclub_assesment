package com.firstclub.membership.dto;

import com.firstclub.membership.entity.TierLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierUpgradeRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "New tier level is required")
    private TierLevel newTierLevel;
}
