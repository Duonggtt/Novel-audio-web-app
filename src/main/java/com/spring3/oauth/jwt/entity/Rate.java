package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "RATES")
public class Rate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "rate_point")
    private BigDecimal rate;

    @Column(name = "rate_quantity")
    private int rateQuantity;

    @OneToOne
    @JoinColumn(name = "novel_id")
    private Novel novel;
}
