package com.firstclub.membership.controller;

import com.firstclub.membership.dto.*;
import com.firstclub.membership.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    /**
     * Get all available membership plans.
     * GET /api/memberships/plans
     */
    @GetMapping("/plans")
    public ResponseEntity<List<PlanDTO>> getAllPlans() {
        List<PlanDTO> plans = membershipService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * Get all membership tiers.
     * GET /api/memberships/tiers
     */
    @GetMapping("/tiers")
    public ResponseEntity<List<TierDTO>> getAllTiers() {
        List<TierDTO> tiers = membershipService.getAllTiers();
        return ResponseEntity.ok(tiers);
    }

    /**
     * Subscribe to a membership plan.
     * POST /api/memberships/subscribe
     */
    @PostMapping("/subscribe")
    public ResponseEntity<MembershipResponse> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        MembershipResponse response = membershipService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get current membership for a user.
     * GET /api/memberships/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<MembershipResponse> getCurrentMembership(@PathVariable Long userId) {
        MembershipResponse response = membershipService.getCurrentMembership(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Upgrade membership tier.
     * PUT /api/memberships/upgrade
     */
    @PutMapping("/upgrade")
    public ResponseEntity<MembershipResponse> upgradeTier(@Valid @RequestBody TierUpgradeRequest request) {
        MembershipResponse response = membershipService.upgradeTier(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Downgrade membership tier.
     * PUT /api/memberships/downgrade
     */
    @PutMapping("/downgrade")
    public ResponseEntity<MembershipResponse> downgradeTier(@Valid @RequestBody TierUpgradeRequest request) {
        MembershipResponse response = membershipService.downgradeTier(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel membership.
     * DELETE /api/memberships/user/{userId}
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<MembershipResponse> cancelMembership(@PathVariable Long userId) {
        MembershipResponse response = membershipService.cancelMembership(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Auto-evaluate and upgrade tier based on user activity.
     * POST /api/memberships/evaluate/{userId}
     */
    @PostMapping("/evaluate/{userId}")
    public ResponseEntity<MembershipResponse> evaluateTier(@PathVariable Long userId) {
        MembershipResponse response = membershipService.autoEvaluateTier(userId);
        return ResponseEntity.ok(response);
    }
}
