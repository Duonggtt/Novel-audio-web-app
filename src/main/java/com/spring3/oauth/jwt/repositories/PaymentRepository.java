package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByUser_Id(long userId);
    @Query("SELECT p FROM Payment p WHERE p.transactionNo = :transactionNo")
    Optional<Payment> findByTransactionNo(String transactionNo);
    Payment findTop1ByUserIdOrderByIdDesc(Long userId);
    Payment findFirstByUserIdAndOrderInfoContainingOrderByIdDesc(long userId, String orderInfo);
}