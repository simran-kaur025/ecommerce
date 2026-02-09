package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    @Autowired
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendForgotPasswordEmail(String email,String token) {
        try {
            String resetLink = "http://localhost:8080/users/updatepassword?token=" + token;
            String emailBody = "Click the link to reset password to your account\n" + resetLink;
            log.info("Sending forgot password email to: {}", email);
            sendEmail(email, "Reset Password to Your Account", emailBody);
        }
        catch (Exception ex) {
                log.error("Failed to send forgot password email to {}", email, ex);
        }
    }


    @Async
    @Override
    public void sendActivationEmail(String email, String token) {
        String subject = "Activate Your Account";
        String activationLink = "http://localhost:8080/api/register/customer/activate?token=" + token;
        String message = "Click the link below to activate your account:\n Link is valid for three hour\n\n" + activationLink;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
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
