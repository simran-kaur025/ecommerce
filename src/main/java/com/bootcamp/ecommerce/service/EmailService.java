package com.bootcamp.ecommerce.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendForgotPasswordEmail(String toEmail, String token) ;
    void sendActivationEmail(String email, String token);
}
