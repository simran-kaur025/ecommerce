package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.entity.OrderProduct;
import com.bootcamp.ecommerce.entity.Product;
import com.bootcamp.ecommerce.entity.Seller;
import com.bootcamp.ecommerce.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bootcamp.ecommerce.constant.EmailConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailServiceImpl implements EmailService {
    @Autowired
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendActivationEmail(String email, String token) {
        try {
            String activationLink = ACTIVATE_ACCOUNT_URL + token;

            String body = """
                    Click the link below to activate your account.
                    Link is valid for three hours.

                    %s
                    """.formatted(activationLink);

            sendEmail(email, ACTIVATE_ACCOUNT_SUBJECT, body);

        } catch (Exception ex) {
            log.error("Failed to send activation email to {}", email, ex);
        }
    }

    @Async
    public void sendSellerRegistrationEmail(String toEmail) {
        try {
            sendEmail(toEmail,SELLER_CREATED_SUBJECT, SELLER_BODY);

            log.info("Seller registration email sent to {}", toEmail);

        } catch (Exception ex) {
            log.error("Failed to send seller email to {}", toEmail, ex);
        }
    }

    @Async
    @Override
    public void sendForgotPasswordEmail(String email, String token) {
        try {
            String resetLink = RESET_PASSWORD_URL + token;

            String body = """
                    Click the link to reset password to your account

                    %s
                    """.formatted(resetLink);

            log.info("Sending forgot password email to {}", email);
            sendEmail(email, RESET_PASSWORD_SUBJECT, body);

        } catch (Exception ex) {
            log.error("Failed to send forgot password email to {}", email, ex);
        }
    }

    @Async
    @Override
    public void sendProductActivatedEmail(String toEmail, Product product) {

        String body = String.format("""
            Product Activated Successfully

            Product Details:
            Name: %s
            Brand: %s
            Description: %s
            Status: ACTIVE
            """,
                product.getName(),
                product.getBrand(),
                product.getDescription()
        );

        sendEmail(toEmail, PRODUCT_ACTIVATED_SUBJECT, body);
    }

    @Async
    @Override
    public void sendProductDeactivatedEmail(String toEmail, Product product) {

        String body = String.format("""
            Product Deactivated

            Product Details:
            Name: %s
            Brand: %s
            Description: %s
            Status: INACTIVE
            """,
                product.getName(),
                product.getBrand(),
                product.getDescription()
        );

        sendEmail(toEmail, PRODUCT_DEACTIVATED_SUBJECT, body);
    }

    @Async
    @Override
    public void sendPasswordChangeEmail(String email) {

        sendEmail(email, PASSWORD_CHANGED_SUBJECT, PASSWORD_CHANGED_BODY);
    }


    @Async
    @Override
    public void sendProductApprovalEmail(String adminEmail, Product product, String sellerName) {

        String body = String.format("""
        New Product Added - Awaiting Approval

        Seller: %s

        Product Details:
        Name: %s
        Brand: %s
        Description: %s

        Please review and approve the product.
        """,
                sellerName,
                product.getName(),
                product.getBrand(),
                product.getDescription()
        );

        sendEmail(adminEmail, PRODUCT_APPROVAL_SUBJECT, body);
    }


    @Async
    @Override
    public void sendAccountActivatedEmail(String email) {

        sendEmail(email, ACCOUNT_ACTIVATED_SUBJECT, ACCOUNT_ACTIVATED_MESSAGE);
    }
    @Async
    @Override
    public void sendAccountDeactivatedEmail(String email) {

        sendEmail(email, ACCOUNT_DEACTIVATED_SUBJECT, ACCOUNT_DEACTIVATED_MESSAGE);
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(fromEmail);

        mailSender.send(message);
    }


    @Async
    @Override
    public void sendPendingOrdersReminder(Seller seller, List<OrderProduct> items) {

        String subject = PENDING_ORDERS_SUBJECT;

        String itemDetails = "";
        for (OrderProduct item : items) {
            itemDetails += "OrderProductId: " + item.getId()
                    + " (OrderId: " + item.getOrder().getId() + ")\n";
        }

        String body = String.format(
                PENDING_ORDERS_MESSAGE_PREFIX,
                seller.getUser().getFirstName()
        ) + itemDetails + PENDING_ORDERS_MESSAGE_SUFFIX;

        sendEmail(seller.getUser().getEmail(), subject, body);
    }
}
