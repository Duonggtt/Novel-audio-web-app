package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@ToString
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Table(name = "COIN_WALLET")
public class CoinWallet {
    @Id
    private String id;

    @Column(name = "coin_amount")
    private int coinAmount;

    @Column(name = "coin_spent")
    private int coinSpent;

    @Column(name = "created_date")
    private LocalDate createdDate;
}
