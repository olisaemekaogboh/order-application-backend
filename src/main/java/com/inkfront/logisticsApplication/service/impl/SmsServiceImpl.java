// service/impl/SmsServiceImpl.java
package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.service.interfaces.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    @Value("${sms.provider:twilio}")
    private String provider;

    @Value("${sms.api.key:}")
    private String apiKey;

    @Value("${sms.from.number:+1234567890}")
    private String fromNumber;

    @Override
    @Async
    public void sendSms(String phoneNumber, String message) {
        try {
            log.info("Sending SMS to: {}, message: {}", phoneNumber, message);
            // Implement actual SMS sending logic here
            // For now, just log it
            // In production, integrate with Twilio, Vonage, or other SMS provider
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
        }
    }

    @Override
    @Async
    public void sendSmsWithTemplate(String phoneNumber, String templateName, String... params) {
        String message = "Your order update: " + String.join(", ", params);
        sendSms(phoneNumber, message);
    }

    @Override
    @Async
    public void sendBulkSms(List<String> phoneNumbers, String message) {
        for (String phoneNumber : phoneNumbers) {
            sendSms(phoneNumber, message);
        }
    }

    @Override
    @Async
    public void sendOrderUpdateSms(String phoneNumber, String orderNumber, String status) {
        String message = "Order #" + orderNumber + " status updated to: " + status;
        sendSms(phoneNumber, message);
    }

    @Override
    @Async
    public void sendDriverAssignmentSms(String phoneNumber, String orderNumber, String driverName, String driverPhone) {
        String message = "Driver " + driverName + " assigned to order #" + orderNumber + ". Phone: " + driverPhone;
        sendSms(phoneNumber, message);
    }

    @Override
    @Async
    public void sendDeliveryConfirmationSms(String phoneNumber, String orderNumber) {
        String message = "Order #" + orderNumber + " has been delivered successfully!";
        sendSms(phoneNumber, message);
    }

    @Override
    @Async
    public void sendOtpSms(String phoneNumber, String otp) {
        String message = "Your OTP code is: " + otp + ". Valid for 5 minutes.";
        sendSms(phoneNumber, message);
    }
}