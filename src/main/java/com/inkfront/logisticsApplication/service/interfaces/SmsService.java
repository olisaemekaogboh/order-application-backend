// service/interfaces/SmsService.java
package com.inkfront.logisticsApplication.service.interfaces;

import java.util.List;

public interface SmsService {

    void sendSms(String phoneNumber, String message);

    void sendSmsWithTemplate(String phoneNumber, String templateName, String... params);

    void sendBulkSms(List<String> phoneNumbers, String message);

    void sendOrderUpdateSms(String phoneNumber, String orderNumber, String status);

    void sendDriverAssignmentSms(String phoneNumber, String orderNumber, String driverName, String driverPhone);

    void sendDeliveryConfirmationSms(String phoneNumber, String orderNumber);

    void sendOtpSms(String phoneNumber, String otp);
}