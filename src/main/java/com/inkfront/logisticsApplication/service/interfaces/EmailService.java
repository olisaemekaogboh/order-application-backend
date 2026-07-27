// service/interfaces/EmailService.java
package com.inkfront.logisticsApplication.service.interfaces;

import java.util.List;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

    void sendEmailWithHtml(String to, String subject, String htmlBody);

    void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String attachmentName);

    void sendVerificationEmail(String to, String token);

    void sendPasswordResetEmail(String to, String token);

    void sendOrderConfirmationEmail(String to, String orderNumber, String orderDetails);

    void sendOrderUpdateEmail(String to, String orderNumber, String status);

    void sendDriverAssignmentEmail(String to, String orderNumber, String driverName, String driverPhone);

    void sendPaymentConfirmationEmail(String to, String orderNumber, String amount);

    void sendWelcomeEmail(String to, String name);

    void sendBulkEmails(List<String> recipients, String subject, String body);
}