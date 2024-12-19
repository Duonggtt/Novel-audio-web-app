package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByUser_Id(long userId);
    @Query("SELECT p FROM Payment p WHERE p.transactionNo = :transactionNo")
    Optional<Payment> findByTransactionNo(String transactionNo);
    Payment findTop1ByUserIdOrderByIdDesc(Long userId);
    Payment findFirstByUserIdAndOrderInfoContainingOrderByIdDesc(long userId, String orderInfo);
    @Query("select p from Payment p where p.user.id = ?1")
    List<Payment> findAllUserById(long userId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
    BigDecimal sumAllAmount();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.payDate < :date")
    BigDecimal sumAmountByCreatedDateBefore(@Param("date") String date);

    // Thêm method mới để lấy tổng amount theo khoảng thời gian
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
        "WHERE p.payDate >= :startDate AND p.payDate < :endDate")
    BigDecimal sumAmountByDateRange(@Param("startDate") String startDate,
                                    @Param("endDate") String endDate);
}