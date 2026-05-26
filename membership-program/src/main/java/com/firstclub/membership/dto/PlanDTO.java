package com.firstclub.membership.dto;

import com.firstclub.membership.entity.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDTO {
    private Long id;
    private PlanType planType;
    private BigDecimal price;
    private Integer durationInDays;
    private String description;
}
