package com.inkfront.logisticsApplication.validator;

import com.inkfront.logisticsApplication.dto.request.order.OrderRequestDTO;
import com.inkfront.logisticsApplication.dto.request.order.PriceCalculationRequestDTO;
import com.inkfront.logisticsApplication.domain.constants.AppConstants;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderValidator {

    public List<String> validateOrderRequest(OrderRequestDTO request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("Order request cannot be null");
            return errors;
        }

        // Validate distance
        if (request.getDistanceKm() == null) {
            errors.add("Distance is required");
        } else {
            if (request.getDistanceKm() < AppConstants.MINIMUM_DISTANCE_KM) {
                errors.add("Distance must be at least " + AppConstants.MINIMUM_DISTANCE_KM + " km");
            }
            if (request.getDistanceKm() > AppConstants.MAXIMUM_DISTANCE_KM) {
                errors.add("Distance cannot exceed " + AppConstants.MAXIMUM_DISTANCE_KM + " km");
            }
        }

        // Validate weight
        if (request.getWeight() != null) {
            if (request.getWeight() < AppConstants.MINIMUM_WEIGHT_KG) {
                errors.add("Weight must be at least " + AppConstants.MINIMUM_WEIGHT_KG + " kg");
            }
            if (request.getWeight() > AppConstants.MAXIMUM_WEIGHT_KG) {
                errors.add("Weight cannot exceed " + AppConstants.MAXIMUM_WEIGHT_KG + " kg");
            }
        }

        // Validate volume
        if (request.getVolume() != null) {
            if (request.getVolume() < AppConstants.MINIMUM_VOLUME_CUBIC) {
                errors.add("Volume must be at least " + AppConstants.MINIMUM_VOLUME_CUBIC + " m³");
            }
            if (request.getVolume() > AppConstants.MAXIMUM_VOLUME_CUBIC) {
                errors.add("Volume cannot exceed " + AppConstants.MAXIMUM_VOLUME_CUBIC + " m³");
            }
        }

        // Validate vehicle type
        if (request.getVehicleType() == null) {
            errors.add("Vehicle type is required");
        }

        // Validate dates
        if (request.getPickupDate() == null) {
            errors.add("Pickup date is required");
        } else if (request.getPickupDate().isBefore(java.time.LocalDateTime.now())) {
            errors.add("Pickup date cannot be in the past");
        }

        return errors;
    }

    public List<String> validatePriceCalculation(PriceCalculationRequestDTO request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("Price calculation request cannot be null");
            return errors;
        }

        if (request.getDistanceKm() == null) {
            errors.add("Distance is required");
        } else if (request.getDistanceKm() < AppConstants.MINIMUM_DISTANCE_KM) {
            errors.add("Distance must be at least " + AppConstants.MINIMUM_DISTANCE_KM + " km");
        }

        if (request.getVehicleType() == null) {
            errors.add("Vehicle type is required");
        }

        if (request.getWeight() != null && request.getWeight() < 0) {
            errors.add("Weight cannot be negative");
        }

        if (request.getVolume() != null && request.getVolume() < 0) {
            errors.add("Volume cannot be negative");
        }

        return errors;
    }

    public boolean isValidOrderStatusTransition(String currentStatus, String newStatus) {
        // Implementation for status transition validation
        return true; // Placeholder
    }

    public boolean isValidPickupAndDeliveryLocations(String pickup, String delivery) {
        if (pickup == null || delivery == null) {
            return false;
        }
        return !pickup.trim().isEmpty() && !delivery.trim().isEmpty() &&
                !pickup.trim().equalsIgnoreCase(delivery.trim());
    }
}