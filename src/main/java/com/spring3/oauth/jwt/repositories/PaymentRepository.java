package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}