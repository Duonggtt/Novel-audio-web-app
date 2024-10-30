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
@Table(name = "HOBBIES")
public class Hobby {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "hobby_name")
    private String name;
}
