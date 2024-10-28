package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Data
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "READING_LIBRARYS")
public class ReadingLibrary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "readingLibrary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserReadingProgress> readingProgressList;

}
