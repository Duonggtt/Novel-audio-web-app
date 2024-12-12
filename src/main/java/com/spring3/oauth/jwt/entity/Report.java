package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "REPORTS")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;  // UUID hoặc String để làm ID
    private String date;  // Ngày báo cáo

    @Column(name = "api_path")
    private String apiPath; // Đường dẫn API
    private String hour; // Khung giờ
    private int count; // Số lần gọi API trong khung giờ
}
