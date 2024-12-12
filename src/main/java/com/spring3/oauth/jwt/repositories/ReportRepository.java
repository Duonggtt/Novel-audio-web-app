package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>{

    List<Report> findByDate(String date); // Lấy báo cáo theo ngày
    List<Report> findByDateAndApiPath(String date, String apiPath); // Lấy báo cáo theo ngày và apiPath
}
