package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.Report;
import com.spring3.oauth.jwt.repositories.ReportRepository;
import com.spring3.oauth.jwt.services.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
public class ScheduledTask {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ReportRepository reportRepository; // Giả sử bạn có repository để lưu vào DB
    private static final Logger log = LoggerFactory.getLogger(ScheduledTask.class);

    @Scheduled(cron = "0 0 * * * ?") // Chạy mỗi giờ vào phút 0
    public void saveReportToDatabase() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateKey = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Tìm các key theo pattern
        Set<String> keys = redisTemplate.keys("api:*-h:*");

        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                try {
                    String[] parts = key.split("-h:");
                    String apiPath = parts[0].replace("api:", "");
                    String hour = parts[1];

                    // Lấy giá trị bộ đếm
                    String countStr = redisTemplate.opsForValue().get(key);
                    int count = countStr != null ? Integer.parseInt(countStr) : 0;

                    // Tạo báo cáo
                    Report report = Report.builder()
                        .date(dateKey)
                        .apiPath(apiPath)
                        .hour(hour)
                        .count(count)
                        .build();

                    // Lưu vào database
                    reportRepository.save(report);

                    // Xóa key khỏi Redis sau khi lưu
                    redisTemplate.delete(key);

                    log.info("Saved report for API {} at hour {} with count {}", apiPath, hour, count);
                } catch (Exception e) {
                    log.error("Error processing key {}: {}", key, e.getMessage());
                }
            }
        } else {
            log.info("No API call reports to save");
        }
    }
}
