package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TIERS")
public class Tier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(name = "icon_path")
    private String iconPath;

    @Column(name = "read_count_required")
    private int readCountRequired;

    @Column(name = "description")
    private String description;
}
