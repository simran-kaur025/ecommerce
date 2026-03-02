package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.entity.OrderProduct;
import com.bootcamp.ecommerce.entity.Product;
import com.bootcamp.ecommerce.entity.Seller;
import jakarta.mail.MessagingException;

import java.util.List;

public interface EmailService {
    void sendForgotPasswordEmail(String toEmail, String token) ;
    void sendActivationEmail(String email, String token);
    void sendSellerRegistrationEmail(String toEmail);

    void sendProductActivatedEmail(String toEmail,Product product);

    void sendProductDeactivatedEmail(String toEmail, Product product);
    void sendPasswordChangeEmail(String email);
    void sendProductApprovalEmail(String adminEmail, Product product, String sellerName);

    void sendAccountActivatedEmail(String email);
    void sendAccountDeactivatedEmail(String email);
    void sendPendingOrdersReminder(Seller seller, List<OrderProduct> items);
}
