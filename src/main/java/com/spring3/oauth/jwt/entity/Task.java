package com.spring3.oauth.jwt.entity;


import com.spring3.oauth.jwt.entity.enums.TaskTypeEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TASKS")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String description;

    private TaskTypeEnum type;

    @Column(name = "reward_coins")
    private int rewardCoins;

}
