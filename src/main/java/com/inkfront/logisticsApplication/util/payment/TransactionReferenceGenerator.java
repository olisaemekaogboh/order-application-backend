package com.inkfront.logisticsApplication.util.payment;

import com.inkfront.logisticsApplication.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class TransactionReferenceGenerator {

    private final PaymentTransactionRepository paymentTransactionRepository;

    public String generate() {
        String prefix = "PAY";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", (int)(Math.random() * 1000000));
        String reference = prefix + timestamp + random;

        // Ensure uniqueness
        while (paymentTransactionRepository.existsByTransactionReference(reference)) {
            random = String.format("%06d", (int)(Math.random() * 1000000));
            reference = prefix + timestamp + random;
        }
        return reference;
    }
}