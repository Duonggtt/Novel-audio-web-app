package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Timer;

@Entity
@Data
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AUDIO_FILES")
public class  AudioFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "audio_url")
    private String audioUrl;

    private Long duration;

    @OneToOne
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;
}
