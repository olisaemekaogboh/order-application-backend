package com.inkfront.logisticsApplication.controller.payment;

import com.inkfront.logisticsApplication.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/paystack")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature) {
        log.info("Received Paystack webhook");
        paymentService.handlePaystackWebhook(payload, signature);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/flutterwave")
    public ResponseEntity<String> handleFlutterwaveWebhook(
            @RequestBody String payload,
            @RequestHeader("verif-hash") String signature) {
        log.info("Received Flutterwave webhook");
        paymentService.handleFlutterwaveWebhook(payload, signature);
        return ResponseEntity.ok("OK");
    }
}