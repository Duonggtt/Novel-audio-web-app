package com.spring3.oauth.jwt.entity;

import com.spring3.oauth.jwt.entity.enums.TaskStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER_TASKS")
public class UserTaskProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Enumerated(EnumType.STRING)
    private TaskStatusEnum status = TaskStatusEnum.NOT_STARTED;

    @Column(name = "current_progress")
    private int currentProgress;

    @Column(name = "required_progress")
    private int requiredProgress;

    private boolean rewardClaimed = false;
}
