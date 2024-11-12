package com.spring3.oauth.jwt.entity;


import com.spring3.oauth.jwt.entity.enums.TaskFrequencyEnum;
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
    private Integer id;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskTypeEnum type;

    @Enumerated(EnumType.STRING)
    private TaskFrequencyEnum frequency;

    @Column(name = "reward_coins")
    private int rewardCoins;

    @Column(name = "required_progress")
    private int requiredProgress;
    // For example: READ_CHAPTERS might need 5 chapters to complete

    // Helper method to get formatted description
    public String getFormattedDescription() {
        return type.getFormattedDescription(requiredProgress);
    }
}
