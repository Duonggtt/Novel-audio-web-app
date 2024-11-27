package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.Package;
import com.spring3.oauth.jwt.entity.Payment;
import com.spring3.oauth.jwt.entity.Subscription;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.models.request.PaymentRequest;
import com.spring3.oauth.jwt.models.response.PaymentResponse;
import com.spring3.oauth.jwt.repositories.PackageRepository;
import com.spring3.oauth.jwt.repositories.PaymentRepository;
import com.spring3.oauth.jwt.repositories.SubscriptionRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
@AllArgsConstructor
@Slf4j
@NoArgsConstructor
public class VNPayService {

    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public String createPayment(PaymentRequest request, long userId) {
        // Validate user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Convert package info into JSON string
        String packageInfo = createPackageInfo(request);

        // Tạo thông tin PaymentParams cho VNPay
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(request.getAmount() * 100));  // VNPay expects amount in "VND" cents
        vnp_Params.put("vnp_CurrCode", "VND");

        String orderType = "other";
        String orderInfo = String.format("USER_%d|%s|PKG_%d|%b|%d",
            userId,
            request.getOrderInfo(),
            request.getPackageId(),
            request.isRenewal(),
            request.getExistingSubscriptionId() != null ? request.getExistingSubscriptionId() : 0
        );

        vnp_Params.put("vnp_TxnRef", getRandomNumber(8));  // Random transaction reference
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        // Get current time
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);  // Set expiration time (15 minutes later)
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sort the params and create the query string
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                    .append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // Create Secure Hash using HMAC-SHA512
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(hashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        // Now create the Payment record with status 04 (processing)
        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setTransactionNo(vnp_Params.get("vnp_TxnRef"));
        payment.setOrderInfo(orderInfo);
        payment.setBankCode("");  // No bank code at creation
        payment.setPayDate("");  // No payment date yet
        payment.setResponseCode("04");  // Payment status: "04" (Processing)
        payment.setUser(user);

        // Save payment record in the database with status "04"
        paymentRepository.save(payment);

        // Return the payment URL to redirect to VNPay
        return payUrl + "?" + queryUrl;
    }



    private String createPackageInfo(PaymentRequest request) {
        // Format: PACKAGE_ID|IS_RENEWAL|SUBSCRIPTION_ID
        return String.format("PKG_%d|%b|%d",
            request.getPackageId(),
            request.isRenewal(),
            request.getExistingSubscriptionId() != null ? request.getExistingSubscriptionId() : 0
        );
    }

    @Transactional
    public PaymentResponse processPaymentResponse(Map<String, String> queryParams) {
        PaymentResponse response = new PaymentResponse();
        log.info("Received payment callback with params: {}", queryParams);

        try {
            // 1. Lấy các thông tin từ queryParams
            String responseCode = queryParams.get("vnp_ResponseCode");
            String transactionNo = queryParams.get("vnp_TransactionNo");
            String orderInfo = queryParams.get("vnp_OrderInfo");
            long amount = Long.parseLong(queryParams.get("vnp_Amount")) / 100;  // VNPay sends amount in cents

            // 2. Parse orderInfo để lấy userId và packageId
            PaymentOrderInfo paymentOrderInfo = parseOrderInfo(orderInfo);
            log.info("Parsed order info: {}", paymentOrderInfo);

            // 3. Lấy thông tin user từ database
            User user = userRepository.findById(paymentOrderInfo.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + paymentOrderInfo.getUserId()));

            // 4. Tạo và lưu payment record vào database
            Payment payment = paymentRepository.findByUser_Id(user.getId());
            if (payment == null) {
                throw new RuntimeException("Payment not found with user ID: " + user.getId());
            }
            payment.setTransactionNo(transactionNo);

            // Cập nhật trạng thái payment từ VNPay
            payment.setResponseCode(responseCode);  // Cập nhật mã phản hồi (success/fail)
            payment.setPayDate(LocalDateTime.now().toString());  // Lưu thời gian thanh toán
            paymentRepository.save(payment);  // Lưu thông tin payment vào DB

            // 5. Nếu thanh toán thành công, xử lý subscription
            if ("00".equals(responseCode)) {  // Mã "00" từ VNPay là thanh toán thành công
                Package packageInfo = packageRepository.findById(paymentOrderInfo.getPackageId())
                    .orElseThrow(() -> new RuntimeException("Package not found with ID: " + paymentOrderInfo.getPackageId()));

                // Kiểm tra subscription hiện tại của user cho package này
                Optional<Subscription> existingSubscription = subscriptionRepository
                    .findByUserIdAndPackageInfoIdAndActive(user.getId(), packageInfo.getId(), true);

                Subscription subscription;
                LocalDateTime now = LocalDateTime.now();

                if (existingSubscription.isPresent() && paymentOrderInfo.isRenewal()) {
                    // Gia hạn subscription: Deactivate subscription cũ và tạo subscription mới
                    Subscription currentSub = existingSubscription.get();
                    currentSub.setActive(false);  // Hủy subscription cũ
                    subscriptionRepository.save(currentSub);

                    // Tạo subscription mới
                    subscription = new Subscription();
                    subscription.setUser(user);
                    subscription.setPackageInfo(packageInfo);
                    subscription.setPayment(payment);
                    subscription.setStartDate(currentSub.getEndDate().isAfter(now) ? currentSub.getEndDate() : now);
                    subscription.setEndDate(subscription.getStartDate().plusMonths(packageInfo.getDuration()));  // Gia hạn thời gian
                    subscription.setActive(true);
                    subscription.setNotificationSent(false);  // Đánh dấu chưa gửi thông báo

                } else {
                    // Tạo mới subscription cho user
                    subscription = new Subscription();
                    subscription.setUser(user);
                    subscription.setPackageInfo(packageInfo);
                    subscription.setPayment(payment);
                    subscription.setStartDate(now);
                    subscription.setEndDate(now.plusMonths(packageInfo.getDuration()));  // Tính thời gian hết hạn
                    subscription.setActive(true);
                    subscription.setNotificationSent(false);  // Đánh dấu chưa gửi thông báo
                }

                subscriptionRepository.save(subscription);  // Lưu subscription vào DB

                // Trả về thông tin thành công
                response.setSuccess(true);
                response.setMessage("Payment successful");
                response.setTransactionNo(transactionNo);
                response.setPackageName(packageInfo.getName());
                response.setExpirationDate(subscription.getEndDate());

            } else {
                // Thanh toán thất bại, cập nhật trạng thái thất bại
                response.setSuccess(false);
                response.setMessage("Payment failed with code: " + responseCode);
                response.setTransactionNo(transactionNo);
            }

        } catch (Exception e) {
            log.error("Error processing payment callback: ", e);
            response.setSuccess(false);
            response.setMessage("Error processing payment: " + e.getMessage());
        }

        return response;
    }


    private PaymentOrderInfo parseOrderInfo(String orderInfo) {
        log.info("Parsing order info: {}", orderInfo);
        try {
            // Tách order info: USER_<userId>|<orderDescription>|PKG_<packageId>|<isRenewal>|<existingSubscriptionId>
            String[] parts = orderInfo.split("\\|");
            if (parts.length != 5) { // Đảm bảo có đủ 5 phần
                throw new RuntimeException("Invalid order info format. Expected 5 parts, found: " + parts.length);
            }

            // Tạo object PaymentOrderInfo để lưu thông tin đã tách
            PaymentOrderInfo info = new PaymentOrderInfo();

            // Parse userId từ USER_<userId>
            if (parts[0].startsWith("USER_")) {
                info.setUserId(Long.parseLong(parts[0].substring(5))); // Bỏ "USER_"
            } else {
                throw new RuntimeException("Invalid user ID format: " + parts[0]);
            }

            // Parse packageId từ PKG_<packageId>
            if (parts[2].startsWith("PKG_")) {
                info.setPackageId(Long.parseLong(parts[2].substring(4))); // Bỏ "PKG_"
            } else {
                throw new RuntimeException("Invalid package info format: " + parts[2]);
            }

            // Parse isRenewal
            info.setRenewal(Boolean.parseBoolean(parts[3]));

            // Parse existingSubscriptionId
            info.setExistingSubscriptionId(Long.parseLong(parts[4]));

            return info;

        } catch (Exception e) {
            log.error("Error parsing order info: {}", orderInfo, e);
            throw new RuntimeException("Failed to parse order info: " + e.getMessage());
        }
    }

    private Subscription processSubscription(PaymentOrderInfo orderInfo, Payment payment, User user) {
        // Find existing active subscription
        Optional<Subscription> existingSubscription =
            subscriptionRepository.findByUserIdAndPackageInfoIdAndActive(
                user.getId(),
                orderInfo.getPackageId(),
                true
            );

        Package packageInfo = packageRepository.findById(orderInfo.getPackageId())
            .orElseThrow(() -> new RuntimeException("Package not found"));

        if (existingSubscription.isPresent()) {
            // Renewal case
            Subscription currentSub = existingSubscription.get();
            LocalDateTime newStartDate = currentSub.getEndDate().isAfter(LocalDateTime.now())
                ? currentSub.getEndDate()
                : LocalDateTime.now();

            // Create new subscription
            Subscription newSubscription = new Subscription();
            newSubscription.setUser(user);
            newSubscription.setPackageInfo(packageInfo);
            newSubscription.setPayment(payment);
            newSubscription.setStartDate(newStartDate);
            newSubscription.setEndDate(newStartDate.plusMonths(packageInfo.getDuration()));
            newSubscription.setActive(true);
            newSubscription.setNotificationSent(false);

            // Deactivate old subscription
            currentSub.setActive(false);
            subscriptionRepository.save(currentSub);

            return subscriptionRepository.save(newSubscription);
        } else {
            // New subscription
            Subscription subscription = new Subscription();
            subscription.setUser(user);
            subscription.setPackageInfo(packageInfo);
            subscription.setPayment(payment);
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(LocalDateTime.now().plusMonths(packageInfo.getDuration()));
            subscription.setActive(true);
            subscription.setNotificationSent(false);
            return subscriptionRepository.save(subscription);
        }
    }

    public PaymentResponse findPaymentByUserId(long userId) {
        // Tìm payment trong database
        Payment payment = paymentRepository.findByUser_Id(userId);
        if(payment == null) {
            throw new RuntimeException("Payment not found with userId: " + userId);
        }

        // Tạo đối tượng phản hồi
        PaymentResponse response = new PaymentResponse();

        // Lấy mã phản hồi từ VNPay
        String responseCode = payment.getResponseCode();

        // Kiểm tra mã phản hồi từ VNPay và trả về thông điệp phù hợp
        switch (responseCode) {
            case "00":  // Thanh toán thành công
                response.setSuccess(true);
                response.setMessage("Payment successful");
                break;
            case "01":  // Lỗi trong quá trình thanh toán
                response.setSuccess(false);
                response.setMessage("Transaction failed. Please try again.");
                break;
            case "04":  // Thanh toán chưa hoàn thành, đang xử lý
                response.setSuccess(false);
                response.setMessage("Payment is being processed. Please wait.");
                break;
            case "11":  // Thanh toán thất bại
                response.setSuccess(false);
                response.setMessage("Payment failed. Please check your payment method.");
                break;
            case "07":  // Người dùng hủy giao dịch
                response.setSuccess(false);
                response.setMessage("Payment was canceled by the user.");
                break;
            case "05":  // Ngân hàng từ chối giao dịch
                response.setSuccess(false);
                response.setMessage("Payment was declined by the bank.");
                break;
            default:  // Mã phản hồi không xác định
                response.setSuccess(false);
                response.setMessage("Unknown payment status: " + responseCode);
                break;
        }

        // Thêm thông tin giao dịch vào phản hồi
        response.setTransactionNo(payment.getTransactionNo());

        // Nếu thanh toán thành công, tìm subscription và trả về thông tin gói
        if (response.isSuccess()) {
            Subscription subscription = subscriptionRepository
                .findByPayment_TransactionNo(payment.getTransactionNo())
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

            response.setPackageName(subscription.getPackageInfo().getName());
            response.setExpirationDate(subscription.getEndDate());
        }

        return response;
    }



    private Payment savePaymentRecord(Map<String, String> queryParams, User user) {
        Payment payment = new Payment();
        payment.setAmount(Long.parseLong(queryParams.get("vnp_Amount")) / 100);
        payment.setTransactionNo(queryParams.get("vnp_TransactionNo"));
        payment.setOrderInfo(queryParams.get("vnp_OrderInfo"));
        payment.setBankCode(queryParams.get("vnp_BankCode"));
        payment.setPayDate(queryParams.get("vnp_PayDate"));
        payment.setResponseCode(queryParams.get("vnp_ResponseCode"));
        payment.setUser(user);
        return paymentRepository.save(payment);
    }



    private String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secret_key);
            byte[] hash = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return hmacSHA512(hashSecret, sb.toString());
    }

    @Getter
    @Setter
    private static class PaymentOrderInfo {
        private Long userId;
        private Long packageId;
        private boolean isRenewal;
        private Long existingSubscriptionId;
    }
}