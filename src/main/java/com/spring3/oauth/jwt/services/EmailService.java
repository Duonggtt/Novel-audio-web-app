package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private final UserRepository userRepository;


    public EmailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Hàm gửi email OTP
    public void sendOtpToEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otpCode);
        message.setFrom("novelaudio247@gmail.com");

        mailSender.send(message);  // Gửi email
    }

    // Hàm gửi email request
    public void sendForAcceptAuthorRequest(String fromEmail, long userId, String username) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        User user = userRepository.findByUsernameAndId(username, userId);
        if(user == null) {
            throw new RuntimeException("User not found.");
        }

        String acceptUrl = "http://14.225.207.58:9898/api/v1/mail-role/acceptRedirect?userId=" + userId + "&username=" + username;
        String declineUrl = "http://14.225.207.58:9898/api/v1/mail-role/declineRedirect?userId=" + userId + "&username=" + username;

        String htmlContent = "<p>User ID: " + userId + "</p>"
                + "<p>Username: " + username + "</p>"
                + "<p><a href=\"" + acceptUrl + "\" style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: white; background-color: green; text-decoration: none; border-radius: 5px;\">Accept</a></p>"
                + "<p><a href=\"" + declineUrl + "\" style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: white; background-color: red; text-decoration: none; border-radius: 5px;\">Decline</a></p>";

        helper.setTo("novelaudio247@gmail.com");
        helper.setSubject("Request for author role.");
        helper.setText(htmlContent, true);
        helper.setFrom(fromEmail);

        mailSender.send(message);  // Gửi email
    }

    public void sendNotificationToUser(String toEmail, String subject, String messageContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText("<p>" + messageContent + "</p>", true);
        helper.setFrom("novelaudio247@gmail.com");  // Your main email

        mailSender.send(message);
    }
}
