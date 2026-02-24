package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.entity.Product;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendForgotPasswordEmail(String toEmail, String token) ;
    void sendActivationEmail(String email, String token);
    void sendSellerRegistrationEmail(String toEmail);

    void sendProductActivatedEmail(String toEmail,Product product);

    void sendProductDeactivatedEmail(String toEmail, Product product);
    void sendPasswordChangeEmail(String email);
    void sendProductApprovalEmail(String adminEmail, Product product, String sellerName);
}
