package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Subscription;
import com.spring3.oauth.jwt.repositories.SubscriptionRepository;
import com.spring3.oauth.jwt.services.EmailService;
import com.spring3.oauth.jwt.services.SubscriptionService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EmailService emailService; // You'll need to implement this

    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    @Transactional
    public void checkSubscriptions() throws MessagingException {
        LocalDateTime warningDate = LocalDateTime.now().plusDays(3);
        List<Subscription> expiringSubscriptions = subscriptionRepository
            .findByActiveAndEndDateBeforeAndNotificationSentFalse(true, warningDate);

        for (Subscription subscription : expiringSubscriptions) {
            emailService.sendExpirationWarning(
                subscription.getUser().getEmail(),
                subscription.getPackageInfo().getName(),
                subscription.getEndDate()
            );
            subscription.setNotificationSent(true);
            subscriptionRepository.save(subscription);
        }

        // Deactivate expired subscriptions
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiredSubscriptions = subscriptionRepository
            .findByActiveAndEndDateBeforeAndNotificationSentFalse(true, now);

        for (Subscription subscription : expiredSubscriptions) {
            subscription.setActive(false);
            subscriptionRepository.save(subscription);
            emailService.sendSubscriptionExpiredNotification(
                subscription.getUser().getEmail(),
                subscription.getPackageInfo().getName()
            );
        }
    }
}
