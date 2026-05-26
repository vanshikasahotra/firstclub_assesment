package com.firstclub.membership.repository;

import com.firstclub.membership.entity.Order;
import com.firstclub.membership.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status AND o.createdAt >= :since")
    Long countByUserIdAndStatusAndCreatedAtAfter(
        @Param("userId") Long userId,
        @Param("status") OrderStatus status,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.id = :userId AND o.status = :status AND o.createdAt >= :since")
    BigDecimal sumTotalAmountByUserIdAndStatusAndCreatedAtAfter(
        @Param("userId") Long userId,
        @Param("status") OrderStatus status,
        @Param("since") LocalDateTime since
    );
}
