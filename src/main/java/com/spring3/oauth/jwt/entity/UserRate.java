package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER_RATES")
public class UserRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "novel_id")
    private Novel novel;

    @Column(name = "rate_point")
    private BigDecimal ratePoint;

    @Column(name = "rated_at")
    private LocalDateTime ratedAt;

}
