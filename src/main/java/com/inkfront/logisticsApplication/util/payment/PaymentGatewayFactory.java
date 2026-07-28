package com.inkfront.logisticsApplication.util.payment;

import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.exception.PaymentGatewayException;
import com.inkfront.logisticsApplication.service.interfaces.payment.PaymentGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PaymentGatewayFactory {

    private final Map<PaymentGateway, PaymentGatewayService> gatewayServiceMap;

    @Autowired
    public PaymentGatewayFactory(List<PaymentGatewayService> services) {
        this.gatewayServiceMap = services.stream()
                .collect(Collectors.toMap(
                        PaymentGatewayService::getGateway,
                        Function.identity()
                ));
        log.info("Initialized PaymentGatewayFactory with {} gateways", gatewayServiceMap.size());
    }

    public PaymentGatewayService getService(PaymentGateway gateway) {
        PaymentGatewayService service = gatewayServiceMap.get(gateway);
        if (service == null) {
            throw new PaymentGatewayException("Unsupported payment gateway: " + gateway);
        }
        return service;
    }
}