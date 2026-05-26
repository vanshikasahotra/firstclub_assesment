package com.firstclub.membership.repository;

import com.firstclub.membership.entity.MembershipStatus;
import com.firstclub.membership.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {

    Optional<UserMembership> findByUserId(Long userId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT um FROM UserMembership um WHERE um.user.id = :userId")
    Optional<UserMembership> findByUserIdWithLock(@Param("userId") Long userId);

    Optional<UserMembership> findByUserIdAndStatus(Long userId, MembershipStatus status);
}
