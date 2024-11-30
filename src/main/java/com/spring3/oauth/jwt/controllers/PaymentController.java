package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.entity.Payment;
import com.spring3.oauth.jwt.models.request.PaymentCoinRequest;
import com.spring3.oauth.jwt.models.request.PaymentRequest;
import com.spring3.oauth.jwt.models.response.PaymentCoinResponse;
import com.spring3.oauth.jwt.models.response.PaymentResponse;
import com.spring3.oauth.jwt.repositories.CoinPackageRepository;
import com.spring3.oauth.jwt.repositories.PackageRepository;
import com.spring3.oauth.jwt.repositories.PaymentRepository;
import com.spring3.oauth.jwt.services.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/payment")
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CoinPackageRepository coinPackageRepository;


    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping("/create/{userId}")
    public ResponseEntity<Map<String, String>> createPayment(@RequestBody PaymentRequest request, @PathVariable long userId) {
        String paymentUrl = vnPayService.createPayment(request, userId);
        // Lấy transaction number từ payment
        Payment payment = paymentRepository.findByUser_Id(userId);

        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        response.put("transactionNo", payment.getTransactionNo());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping("/coins/create/{userId}")
    public ResponseEntity<Map<String, String>> createCoinPayment(@RequestBody PaymentCoinRequest request, @PathVariable long userId) {
        String paymentUrl = vnPayService.createCoinPayment(request, userId);
        // Lấy transaction number từ payment
        Payment payment = paymentRepository.findByUser_Id(userId);

        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        response.put("transactionNo", payment.getTransactionNo());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/packages")
    public ResponseEntity<?> getAvailablePackages() {
        return ResponseEntity.ok(packageRepository.findAll());
    }

    @GetMapping("/coins/packages")
    public ResponseEntity<?> getAvailableCoinPackages() {
        return ResponseEntity.ok(coinPackageRepository.findAll());
    }

    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping("/subscribe")
    public ResponseEntity<String> createSubscription(@RequestBody PaymentRequest request, @RequestParam long userId) {
        String paymentUrl = vnPayService.createPayment(request, userId);
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/callback-html")
    public String paymentCallbackHtml(@RequestParam Map<String, String> queryParams) {
        PaymentResponse response = vnPayService.processPaymentResponse(queryParams);

        // Lấy dữ liệu từ response
        String statusClass = response.isSuccess() ? "success" : "error";
        String message = response.getMessage() != null ? response.getMessage() : "Unknown status";
        String transactionNo = response.getTransactionNo() != null ? response.getTransactionNo() : "N/A";
        String packageName = response.getPackageName() != null ? response.getPackageName() : "N/A";

        // Định dạng ngày hết hạn
        String expirationDate;
        if (response.getExpirationDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            expirationDate = response.getExpirationDate().format(formatter);
        } else {
            expirationDate = "N/A";
        }
        // Định dạng số tiền
        String totalAmount = queryParams.getOrDefault("vnp_Amount", "N/A");
        if (!totalAmount.equals("N/A")) {
            totalAmount = formatAmount(totalAmount);
        }

        // Định dạng ngày thanh toán
        String paymentDate = queryParams.getOrDefault("vnp_PayDate", "N/A");
        if (!paymentDate.equals("N/A")) {
            paymentDate = formatPaymentDate(paymentDate);
        }

        // Gọi hàm generatePaymentHtml
        return generatePaymentHtml(statusClass, message, transactionNo, packageName, expirationDate, totalAmount, paymentDate);
    }

    @GetMapping("/coins/callback-html")
    public String paymentCoinCallbackHtml(@RequestParam Map<String, String> queryParams) {
        PaymentCoinResponse response = vnPayService.processPaymentCoinResponse(queryParams);

        // Lấy dữ liệu từ response
        String statusClass = response.isSuccess() ? "success" : "error";
        String message = response.getMessage() != null ? response.getMessage() : "Unknown status";
        String transactionNo = response.getTransactionNo() != null ? response.getTransactionNo() : "N/A";
        String coinPackageName = response.getPackageName() != null ? response.getPackageName() : "N/A";
        String finalCoinAmount = response.getFinalCoinAmount() != null ? response.getFinalCoinAmount() : "N/A";
        String discount = response.getDiscount() != 0 ? String.valueOf(response.getDiscount()) : "N/A";

        // Định dạng số tiền
        String totalAmount = queryParams.getOrDefault("vnp_Amount", "N/A");
        if (!totalAmount.equals("N/A")) {
            totalAmount = formatAmount(totalAmount);
        }

        // Định dạng ngày thanh toán
        String paymentDate = queryParams.getOrDefault("vnp_PayDate", "N/A");
        if (!paymentDate.equals("N/A")) {
            paymentDate = formatPaymentDate(paymentDate);
        }

        // Gọi hàm generatePaymentHtml
        return generateCoinPaymentHtml(statusClass, message, transactionNo, coinPackageName, finalCoinAmount, totalAmount, discount, paymentDate);
    }

    @GetMapping("/check-status/{transactionNo}")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable String transactionNo) {
        PaymentResponse response = vnPayService.findPaymentByTransactionNo(transactionNo);

        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        result.put("message", response.getMessage());
        result.put("transactionNo", response.getTransactionNo());
        result.put("packageName", response.getPackageName());
        result.put("expirationDate", response.getExpirationDate());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/coins/check-status/{transactionNo}")
    public ResponseEntity<Map<String, Object>> checkPaymentCoinStatus(@PathVariable String transactionNo) {
        PaymentCoinResponse response = vnPayService.findPaymentCoinByTransactionNo(transactionNo);

        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        result.put("message", response.getMessage());
        result.put("transactionNo", response.getTransactionNo());
        result.put("packageName", response.getPackageName());
        result.put("finalCoinAmount", response.getFinalCoinAmount());
        result.put("discount", response.getDiscount());

        return ResponseEntity.ok(result);
    }

    private String formatAmount(String rawAmount) {
        try {
            // Chuyển đổi số tiền từ chuỗi sang số nguyên
            long amount = Long.parseLong(rawAmount);
            // Chia cho 100 để lấy số tiền thực (VNPay trả về số nhân với 100)
            amount /= 100;
            // Định dạng tiền theo locale Việt Nam
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            return formatter.format(amount) + " VND"; // Thêm đơn vị tiền tệ
        } catch (Exception e) {
            // Trả về giá trị mặc định nếu lỗi xảy ra
            return "Invalid amount";
        }
    }

    private String formatPaymentDate(String rawDate) {
        try {
            // Định nghĩa formatter để parse chuỗi ngày gốc
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            // Định nghĩa formatter để định dạng lại chuỗi ngày
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            // Parse chuỗi gốc thành LocalDateTime
            LocalDateTime dateTime = LocalDateTime.parse(rawDate, inputFormatter);
            // Định dạng lại chuỗi ngày
            return dateTime.format(outputFormatter);
        } catch (Exception e) {
            // Trả về giá trị mặc định nếu lỗi xảy ra
            return "Invalid date";
        }
    }


    private String generatePaymentHtml(String statusClass, String message, String transactionNo, String packageName, String expirationDate, String totalAmount, String paymentDate) {
        StringBuilder html = new StringBuilder();
        html.append("""
    <!DOCTYPE html>
    <html>
    <head>
        <title>Payment Status</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                background-color: #f4f4f9;
                margin: 0;
                padding: 0;
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
            }
            .invoice-container {
                width: 100%;
                max-width: 400px;
                background: white;
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
                border-radius: 8px;
                padding: 20px;
                text-align: left;
            }
            .invoice-header {
                text-align: center;
                border-bottom: 1px solid #ddd;
                padding-bottom: 10px;
                margin-bottom: 20px;
            }
            .invoice-header h1 {
                font-size: 1.5em;
                margin: 0;
            }
            .invoice-header .status {
                font-size: 1.2em;
                margin-top: 5px;
            }
            .success {
                color: green;
            }
            .error {
                color: red;
            }
            .invoice-details {
                font-size: 1em;
                line-height: 1.5;
            }
            .invoice-details p {
                margin: 5px 0;
            }
            .footer {
                text-align: center;
                margin-top: 20px;
                font-size: 0.9em;
                color: #777;
            }
            .countdown {
                font-size: 0.8em;
                color: #888;
                text-align: center;
                margin-top: 10px;
            }
        </style>
    </head>
    <body>
        <div class="invoice-container">
""");
        html.append("<div class=\"invoice-header\">")
            .append("<h1>Payment Receipt</h1>")
            .append("<div class=\"status ").append(statusClass).append("\">").append(message).append("</div>")
            .append("</div>")
            .append("<div class=\"invoice-details\">")
            .append("<p><strong>Transaction ID:</strong> ").append(transactionNo).append("</p>")
            .append("<p><strong>Package:</strong> ").append(packageName).append("</p>")
            .append("<p><strong>Expiration Date:</strong> ").append(expirationDate).append("</p>")
            .append("<p><strong>Total Amount:</strong> ").append(totalAmount).append("</p>")
            .append("<p><strong>Payment Date:</strong> ").append(paymentDate).append("</p>")
            .append("</div>")
            .append("<div class=\"footer\">Thank you for your payment!</div>")
            .append("</div>")
            .append("</body></html>");
        return html.toString();
    }

    private String generateCoinPaymentHtml(String statusClass, String message, String transactionNo, String coinPackageName, String finalCoinAmount, String totalAmount, String discount, String paymentDate) {
        StringBuilder html = new StringBuilder();
        html.append("""
    <!DOCTYPE html>
    <html>
    <head>
        <title>Payment Status</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                background-color: #f4f4f9;
                margin: 0;
                padding: 0;
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
            }
            .invoice-container {
                width: 100%;
                max-width: 400px;
                background: white;
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
                border-radius: 8px;
                padding: 20px;
                text-align: left;
            }
            .invoice-header {
                text-align: center;
                border-bottom: 1px solid #ddd;
                padding-bottom: 10px;
                margin-bottom: 20px;
            }
            .invoice-header h1 {
                font-size: 1.5em;
                margin: 0;
            }
            .invoice-header .status {
                font-size: 1.2em;
                margin-top: 5px;
            }
            .success {
                color: green;
            }
            .error {
                color: red;
            }
            .invoice-details {
                font-size: 1em;
                line-height: 1.5;
            }
            .invoice-details p {
                margin: 5px 0;
            }
            .footer {
                text-align: center;
                margin-top: 20px;
                font-size: 0.9em;
                color: #777;
            }
            .countdown {
                font-size: 0.8em;
                color: #888;
                text-align: center;
                margin-top: 10px;
            }
        </style>
    </head>
    <body>
        <div class="invoice-container">
""");
        html.append("<div class=\"invoice-header\">")
            .append("<h1>Payment Receipt</h1>")
            .append("<div class=\"status ").append(statusClass).append("\">").append(message).append("</div>")
            .append("</div>")
            .append("<div class=\"invoice-details\">")
            .append("<p><strong>Transaction ID:</strong> ").append(transactionNo).append("</p>")
            .append("<p><strong>Package:</strong> ").append(coinPackageName).append("</p>")
            .append("<p><strong>Total coin:</strong> ").append(finalCoinAmount).append("</p>")
            .append("<p><strong>Total Amount:</strong> ").append(totalAmount).append("</p>")
            .append("<p><strong>Discount:</strong> ").append(discount).append("%</p>")
            .append("<p><strong>Payment Date:</strong> ").append(paymentDate).append("</p>")
            .append("</div>")
            .append("<div class=\"footer\">Thank you for your payment!</div>")
            .append("</div>")
            .append("</body></html>");
        return html.toString();
    }

}