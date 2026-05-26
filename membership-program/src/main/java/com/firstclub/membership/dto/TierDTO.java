package com.firstclub.membership.dto;

import com.firstclub.membership.entity.TierLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierDTO {
    private Long id;
    private TierLevel level;
    private Integer priority;
    private String description;
    private List<BenefitDTO> benefits;
}
