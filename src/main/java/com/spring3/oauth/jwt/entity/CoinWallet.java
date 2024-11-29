package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
