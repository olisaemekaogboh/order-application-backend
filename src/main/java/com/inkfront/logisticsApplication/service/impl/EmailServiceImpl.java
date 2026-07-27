// service/impl/EmailServiceImpl.java
package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendEmailWithHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(attachmentName, new ByteArrayDataSource(attachment, "application/octet-stream"));
            mailSender.send(message);
            log.info("Email with attachment sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email with attachment to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify Your Email";
        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        String htmlBody = buildVerificationEmailHtml(to, verificationLink);
        sendEmailWithHtml(to, subject, htmlBody);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Reset Your Password";
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String htmlBody = buildPasswordResetEmailHtml(to, resetLink);
        sendEmailWithHtml(to, subject, htmlBody);
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(String to, String orderNumber, String orderDetails) {
        String subject = "Order Confirmation - " + orderNumber;
        String htmlBody = buildOrderConfirmationEmailHtml(to, orderNumber, orderDetails);
        sendEmailWithHtml(to, subject, htmlBody);
    }

    @Override
    @Async
    public void sendOrderUpdateEmail(String to, String orderNumber, String status) {
        String subject = "Order Update - " + orderNumber;
        String htmlBody = buildOrderUpdateEmailHtml(to, orderNumber, status);
        sendEmailWithHtml(to, subject, htmlBody);
    }

    @Override
    @Async
    public void sendDriverAssignmentEmail(String to, String orderNumber, String driverName, String driverPhone) {
        String subject = "Driver Assigned - " + orderNumber;
        String htmlBody = buildDriverAssignmentEmailHtml(to, orderNumber, driverName, driverPhone);
        sendEmailWithHtml(to, subject, htmlBody);
    }

    @Override
    @Async
    public void sendPaymentConfirmationEmail(String to, String orderNumber, String amount) {
        String subject = "Payment Confirmation - " + orderNumber;
        String htmlBody = buildPaymentConfirmationEmailHtml(to, orderNumber, amount);
        sendEmailWithHtml(to, subject, htmlBody);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Welcome to Logistics Platform";
        String htmlBody = buildWelcomeEmailHtml(to, name);
        sendEmailWithHtml(to, subject, htmlBody);
    }

    @Override
    @Async
    public void sendBulkEmails(List<String> recipients, String subject, String body) {
        for (String recipient : recipients) {
            sendEmail(recipient, subject, body);
        }
    }

    // Email HTML builders

    private String buildVerificationEmailHtml(String to, String verificationLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Welcome to Logistics Platform</h2>" +
                "<p>Hello,</p>" +
                "<p>Please click the link below to verify your email address:</p>" +
                "<a href='" + verificationLink + "'>Verify Email</a>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>If you didn't create an account, please ignore this email.</p>" +
                "<br><p>Regards,<br>Logistics Platform Team</p>" +
                "</body></html>";
    }

    private String buildPasswordResetEmailHtml(String to, String resetLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Reset Your Password</h2>" +
                "<p>Hello,</p>" +
                "<p>We received a request to reset your password. Click the link below to set a new password:</p>" +
                "<a href='" + resetLink + "'>Reset Password</a>" +
                "<p>This link will expire in 15 minutes.</p>" +
                "<p>If you didn't request a password reset, please ignore this email.</p>" +
                "<br><p>Regards,<br>Logistics Platform Team</p>" +
                "</body></html>";
    }

    private String buildOrderConfirmationEmailHtml(String to, String orderNumber, String orderDetails) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Order Confirmation</h2>" +
                "<p>Hello,</p>" +
                "<p>Your order has been confirmed.</p>" +
                "<h3>Order Details:</h3>" +
                "<p><strong>Order Number:</strong> " + orderNumber + "</p>" +
                "<p>" + orderDetails + "</p>" +
                "<br><p>Regards,<br>Logistics Platform Team</p>" +
                "</body></html>";
    }

    private String buildOrderUpdateEmailHtml(String to, String orderNumber, String status) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Order Update</h2>" +
                "<p>Hello,</p>" +
                "<p>Your order <strong>" + orderNumber + "</strong> status has been updated to: <strong>" + status + "</strong></p>" +
                "<p>You can track your order in your dashboard.</p>" +
                "<br><p>Regards,<br>Logistics Platform Team</p>" +
                "</body></html>";
    }

    private String buildDriverAssignmentEmailHtml(String to, String orderNumber, String driverName, String driverPhone) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Driver Assigned</h2>" +
                "<p>Hello,</p>" +
                "<p>A driver has been assigned to your order <strong>" + orderNumber + "</strong></p>" +
                "<h3>Driver Details:</h3>" +
                "<p><strong>Name:</strong> " + driverName + "</p>" +
                "<p><strong>Phone:</strong> " + driverPhone + "</p>" +
                "<br><p>Regards,<br>Logistics Platform Team</p>" +
                "</body></html>";
    }

    private String buildPaymentConfirmationEmailHtml(String to, String orderNumber, String amount) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Payment Confirmation</h2>" +
                "<p>Hello,</p>" +
                "<p>Your payment for order <strong>" + orderNumber + "</strong> has been confirmed.</p>" +
                "<p><strong>Amount:</strong> " + amount + "</p>" +
                "<br><p>Regards,<br>Logistics Platform Team</p>" +
                "</body></html>";
    }

    private String buildWelcomeEmailHtml(String to, String name) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Welcome to Logistics Platform</h2>" +
                "<p>Hello " + name + ",</p>" +
                "<p>Welcome to Logistics Platform! We're excited to have you on board.</p>" +
                "<p>You can now:</p>" +
                "<ul>" +
                "<li>Create and track orders</li>" +
                "<li>Manage your deliveries</li>" +
                "<li>View your order history</li>" +
                "</ul>" +
                "<br><p>Regards,<br>Logistics Platform Team</p>" +
                "</body></html>";
    }

    // Helper class for attachment handling
    private static class ByteArrayDataSource implements jakarta.activation.DataSource {
        private final byte[] data;
        private final String contentType;

        public ByteArrayDataSource(byte[] data, String contentType) {
            this.data = data;
            this.contentType = contentType;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public java.io.InputStream getInputStream() throws java.io.IOException {
            return new java.io.ByteArrayInputStream(data);
        }

        @Override
        public String getName() {
            return "attachment";
        }

        @Override
        public java.io.OutputStream getOutputStream() throws java.io.IOException {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}