package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PAYMENTS")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long amount;

    @Column(name = "transaction_no")
    private String transactionNo;

    @Column(name = "order_info")
    private String orderInfo;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "pay_date")
    private String payDate;

    @Column(name = "response_code")
    private String responseCode;
}