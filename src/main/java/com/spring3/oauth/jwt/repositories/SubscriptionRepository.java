package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>{
    List<Subscription> findByActiveAndEndDateBeforeAndNotificationSentFalse(boolean active, LocalDateTime date);
    List<Subscription> findByUserIdAndActive(long userId, boolean active);
    Optional<Subscription> findByUserIdAndPackageInfoIdAndActive(long userId, long packageInfoId, boolean active);
    Optional<Subscription> findByPayment_TransactionNo(String transactionNo);
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.active = :active")
    Optional<Subscription> findSubByUserIdAndActive(long userId, boolean active);
    Subscription findSubsByUserIdAndActive(long userId, boolean active);
    @Query("SELECT s FROM Subscription s WHERE s.user.id = ?1 AND s.packageInfo.id = ?2 AND s.endDate > CURRENT_TIMESTAMP AND s.active = true")
    Subscription findActiveSubscriptionByUserIdAndPackageId(long userId, long packageId);
}
