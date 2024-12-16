package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.Payment;
import com.spring3.oauth.jwt.entity.UserActivityReport;
import com.spring3.oauth.jwt.models.dtos.PayReportResponseDTO;
import com.spring3.oauth.jwt.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.*;
import java.time.*;
import java.util.stream.IntStream;

@Service
public class ReportService {
    @Autowired
    private ReportRepository reportRepository;

    public List<PayReportResponseDTO> getPayReport() {
        LocalDate endDate = LocalDate.now();  // endDate is the current day
        LocalDate startDate = endDate.minusDays(6);  // startDate is 6 days before endDate

        // Convert LocalDate to String with yyyy-MM-dd format
        String startDateStr = startDate.toString();
        String endDateStr = endDate.toString();

        // Get all payments within the last 7 days
        List<Payment> payments = reportRepository.findPaymentsBetweenDates(startDateStr, endDateStr);

        // Group payments by date (convert payDate to LocalDateTime)
        Map<LocalDate, List<Payment>> paymentsByDate = payments.stream()
            .collect(Collectors.groupingBy(payment -> {
                // Convert payDate string to LocalDateTime
                try {
                    // Chuyển đổi từ String thành LocalDateTime và chỉ lấy ngày
                    LocalDateTime dateTime = LocalDateTime.parse(payment.getPayDate(), DateTimeFormatter.ISO_DATE_TIME);
                    return dateTime.toLocalDate();  // Lấy phần ngày
                } catch (Exception e) {
                    System.out.println("Error parsing date: " + payment.getPayDate());
                    return null;  // Nếu có lỗi, trả về null (hoặc có thể xử lý theo cách khác)
                }
            }));

        // Log the paymentsByDate to ensure the map is correctly populated
        paymentsByDate.forEach((date, dailyPayments) -> {
            System.out.println("Payments for date: " + date);
            dailyPayments.forEach(payment -> System.out.println(payment.getOrderInfo()));
        });

        // Generate the list of dates from startDate to endDate (inclusive)
        return startDate.datesUntil(endDate.plusDays(1))  // Loop includes the endDate
            .map(date -> {
                // Use the formatted LocalDate as the key
                System.out.println("Checking payments for date: " + date);

                List<Payment> dailyPayments = paymentsByDate.getOrDefault(date, Collections.emptyList());
                System.out.println("dailyPayments size: " + dailyPayments.size());

                // Calculate total amount
                long totalAmount = dailyPayments.stream()
                    .mapToLong(Payment::getAmount)
                    .sum();

                // Calculate total coins purchased
                long coinAmount = dailyPayments.stream()
                    .filter(payment -> payment.getOrderInfo() != null && payment.getOrderInfo().contains("coin"))  // Check if order_info contains "coin"
                    .mapToLong(payment -> {
                        String orderInfo = payment.getOrderInfo().trim();  // Trim whitespace

                        // Check if order_info contains "coin_pack_"
                        if (orderInfo.contains("coin_pack_")) {
                            String[] parts = orderInfo.split("coin_pack_");

                            // If the part after "coin_pack_" can be parsed as a number, take it
                            if (parts.length > 1) {
                                try {
                                    long coins = Long.parseLong(parts[1].trim());  // Trim and parse the coin number
                                    return coins;  // Return the coin number
                                } catch (NumberFormatException e) {
                                    System.out.println("Error parsing coins for order_info " + orderInfo + ": " + e.getMessage());
                                }
                            }
                        }

                        // If there's no valid coin number, return 0
                        return 0L;
                    })
                    .sum();

                // Return the PayReportResponseDTO with LocalDate
                return PayReportResponseDTO.builder()
                    .date(date)  // Use LocalDate directly
                    .totalAmount(totalAmount)
                    .coinAmount(coinAmount)
                    .build();
            })
            .collect(Collectors.toList());
    }

    public List<List<UserActivityReport>> getReportsWithAllHours(String date) {
        // Lấy báo cáo hiện có cho cả 2 api (comments và read-chapter)
        List<UserActivityReport> commentReports = reportRepository.findByDateAndApiPath(date, "comments");
        List<UserActivityReport> readChapterReports = reportRepository.findByDateAndApiPath(date, "read-chapter");

        // Điền các giờ còn thiếu cho cả 2 api
        List<UserActivityReport> filledCommentReports = fillMissingHours(commentReports, date, "comments");
        List<UserActivityReport> filledReadChapterReports = fillMissingHours(readChapterReports, date, "read-chapter");

        // Trả về một danh sách chứa 2 danh sách con: 1 cho comments và 1 cho read-chapter
        return List.of(filledCommentReports, filledReadChapterReports);
    }

    private List<UserActivityReport> fillMissingHours(List<UserActivityReport> existingReports, String date, String apiPath) {
        // Tạo danh sách 24 giờ với count mặc định là 0
        return IntStream.range(0, 24)
            .mapToObj(hour -> {
                String hourStr = String.format("%02d", hour);
                return existingReports.stream()
                    .filter(r -> r.getApiPath().equals(apiPath) && r.getHour().equals(hourStr))
                    .findFirst()
                    .orElse(UserActivityReport.builder()
                        .date(date)
                        .apiPath(apiPath)
                        .hour(hourStr)
                        .count(0)
                        .build());
            })
            .collect(Collectors.toList());
    }
}
