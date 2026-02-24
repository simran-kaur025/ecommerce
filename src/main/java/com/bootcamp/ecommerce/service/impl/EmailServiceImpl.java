package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.entity.Product;
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

import static com.bootcamp.ecommerce.constant.Constant.PRODUCT_APPROVAL_SUBJECT;
import static com.bootcamp.ecommerce.constant.Constant.PRODUCT_DEACTIVATED_SUBJECT;

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
            String activationLink = Constant.ACTIVATE_ACCOUNT_URL + token;

            String body = """
                    Click the link below to activate your account.
                    Link is valid for three hours.

                    %s
                    """.formatted(activationLink);

            sendEmail(email, Constant.ACTIVATE_ACCOUNT_SUBJECT, body);

        } catch (Exception ex) {
            log.error("Failed to send activation email to {}", email, ex);
        }
    }

    @Async
    public void sendSellerRegistrationEmail(String toEmail) {
        try {
            sendEmail(toEmail,
                    Constant.SELLER_CREATED_SUBJECT,
                    Constant.SELLER_BODY);

            log.info("Seller registration email sent to {}", toEmail);

        } catch (Exception ex) {
            log.error("Failed to send seller email to {}", toEmail, ex);
        }
    }

    @Async
    @Override
    public void sendForgotPasswordEmail(String email, String token) {
        try {
            String resetLink = Constant.RESET_PASSWORD_URL + token;

            String body = """
                    Click the link to reset password to your account

                    %s
                    """.formatted(resetLink);

            log.info("Sending forgot password email to {}", email);
            sendEmail(email, Constant.RESET_PASSWORD_SUBJECT, body);

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

        sendEmail(toEmail, Constant.PRODUCT_ACTIVATED_SUBJECT, body);
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

        sendEmail(email, Constant.PASSWORD_CHANGED_SUBJECT, Constant.PASSWORD_CHANGED_BODY);
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
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(fromEmail);

        mailSender.send(message);
    }

}
