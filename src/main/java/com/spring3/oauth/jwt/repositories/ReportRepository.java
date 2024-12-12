package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.UserActivityReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<UserActivityReport, Long>{

    List<UserActivityReport> findByDate(String date); // Lấy báo cáo theo ngày
    List<UserActivityReport> findByDateAndApiPath(String date, String apiPath); // Lấy báo cáo theo ngày và apiPath
}
