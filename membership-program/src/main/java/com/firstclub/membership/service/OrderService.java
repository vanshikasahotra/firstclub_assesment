package com.firstclub.membership.service;

import com.firstclub.membership.dto.CreateOrderRequest;
import com.firstclub.membership.entity.Order;
import com.firstclub.membership.entity.OrderStatus;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.exception.ResourceNotFoundException;
import com.firstclub.membership.repository.OrderRepository;
import com.firstclub.membership.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing orders.
 * Orders are used for tier calculation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MembershipService membershipService;

    /**
     * Create a new order for a user.
     * Automatically triggers tier evaluation after order completion.
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = Order.builder()
            .user(user)
            .orderNumber(orderNumber)
            .totalAmount(request.getTotalAmount())
            .status(OrderStatus.COMPLETED)
            .build();

        Order saved = orderRepository.save(order);
        log.info("Order created: {}", saved.getOrderNumber());

        // Auto-evaluate tier after order
        try {
            membershipService.autoEvaluateTier(user.getId());
        } catch (ResourceNotFoundException e) {
            log.info("User {} doesn't have a membership yet, skipping tier evaluation", user.getId());
        }

        return saved;
    }
}
