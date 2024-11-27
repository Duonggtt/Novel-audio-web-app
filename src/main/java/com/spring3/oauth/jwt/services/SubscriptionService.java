package com.spring3.oauth.jwt.services;

import jakarta.mail.MessagingException;

public interface SubscriptionService {
    void checkSubscriptions() throws MessagingException;
}
