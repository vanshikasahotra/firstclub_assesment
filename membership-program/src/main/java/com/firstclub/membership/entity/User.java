package com.firstclub.membership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "cohort_name")
    private String cohort;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserMembership membership;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Computed fields for tier calculation
    @Transient
    public int getMonthlyOrderCount() {
        if (orders == null) return 0;
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return (int) orders.stream()
                .filter(order -> order.getCreatedAt().isAfter(oneMonthAgo))
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .count();
    }

    @Transient
    public BigDecimal getMonthlyOrderValue() {
        if (orders == null) return BigDecimal.ZERO;
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return orders.stream()
                .filter(order -> order.getCreatedAt().isAfter(oneMonthAgo))
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
