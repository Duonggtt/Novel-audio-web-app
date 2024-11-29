package com.spring3.oauth.jwt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Data
@ToString
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Table(name = "COIN_PACKAGE")
public class CoinPackage {
    @Id
    private String id;

    @Column(name = "before_coin_amount")
    private int beforeCoinAmount; // Số coin trước khi discount

    @Column(name = "final_coin_amount")
    private int finalCoinAmount; // Số coin sau khi discount

    @Column(name = "coin_bonus")
    private int coinBonus; // Số coin bonus

    @Column(name = "money_amount")
    private long moneyAmount; // Số tiền mua coin

    private int discount; // Phần trăm giảm giá

}
