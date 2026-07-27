// service/impl/DistanceServiceImpl.java
package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.service.interfaces.DistanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistanceServiceImpl implements DistanceService {

    private final RestTemplate restTemplate;

    @Value("${google.maps.api-key:}")
    private String googleMapsApiKey;

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double EARTH_RADIUS_MILES = 3959.0;

    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public double calculateDistance(String address1, String address2) {
        try {
            double[] coords1 = geocodeAddressToCoordinates(address1);
            double[] coords2 = geocodeAddressToCoordinates(address2);

            if (coords1 == null || coords2 == null) {
                log.warn("Could not geocode one or both addresses");
                return 0.0;
            }

            return calculateDistance(coords1[0], coords1[1], coords2[0], coords2[1]);
        } catch (Exception e) {
            log.error("Failed to calculate distance between addresses: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public double getDistanceInKm(double lat1, double lon1, double lat2, double lon2) {
        return calculateDistance(lat1, lon1, lat2, lon2);
    }

    @Override
    public double getDistanceInMiles(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_MILES * c;
    }

    @Override
    public String geocodeAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        // If Google Maps API key is available, use it for accurate geocoding
        if (googleMapsApiKey != null && !googleMapsApiKey.isEmpty()) {
            try {
                String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                        address.replace(" ", "+") + "&key=" + googleMapsApiKey;

                var response = restTemplate.getForObject(url, java.util.Map.class);

                if (response != null && "OK".equals(response.get("status"))) {
                    var results = (java.util.List<?>) response.get("results");
                    if (results != null && !results.isEmpty()) {
                        var firstResult = (java.util.Map<?, ?>) results.get(0);
                        var geometry = (java.util.Map<?, ?>) firstResult.get("geometry");
                        var location = (java.util.Map<?, ?>) geometry.get("location");

                        double lat = ((Number) location.get("lat")).doubleValue();
                        double lng = ((Number) location.get("lng")).doubleValue();

                        return lat + "," + lng;
                    }
                }
            } catch (Exception e) {
                log.warn("Google Maps geocoding failed, using fallback: {}", e.getMessage());
            }
        }

        // Fallback: return formatted address
        return address;
    }

    @Override
    public double[] geocodeAddressToCoordinates(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        // If Google Maps API key is available, use it for accurate geocoding
        if (googleMapsApiKey != null && !googleMapsApiKey.isEmpty()) {
            try {
                String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                        address.replace(" ", "+") + "&key=" + googleMapsApiKey;

                var response = restTemplate.getForObject(url, java.util.Map.class);

                if (response != null && "OK".equals(response.get("status"))) {
                    var results = (java.util.List<?>) response.get("results");
                    if (results != null && !results.isEmpty()) {
                        var firstResult = (java.util.Map<?, ?>) results.get(0);
                        var geometry = (java.util.Map<?, ?>) firstResult.get("geometry");
                        var location = (java.util.Map<?, ?>) geometry.get("location");

                        double lat = ((Number) location.get("lat")).doubleValue();
                        double lng = ((Number) location.get("lng")).doubleValue();

                        return new double[]{lat, lng};
                    }
                }
            } catch (Exception e) {
                log.warn("Google Maps geocoding failed: {}", e.getMessage());
            }
        }

        // Fallback: generate approximate coordinates based on Nigeria regions
        return generateFallbackCoordinates(address);
    }

    @Override
    public String reverseGeocode(double latitude, double longitude) {
        if (!isValidCoordinate(latitude, longitude)) {
            return null;
        }

        // If Google Maps API key is available, use it
        if (googleMapsApiKey != null && !googleMapsApiKey.isEmpty()) {
            try {
                String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                        latitude + "," + longitude + "&key=" + googleMapsApiKey;

                var response = restTemplate.getForObject(url, java.util.Map.class);

                if (response != null && "OK".equals(response.get("status"))) {
                    var results = (java.util.List<?>) response.get("results");
                    if (results != null && !results.isEmpty()) {
                        var firstResult = (java.util.Map<?, ?>) results.get(0);
                        return (String) firstResult.get("formatted_address");
                    }
                }
            } catch (Exception e) {
                log.warn("Reverse geocoding failed: {}", e.getMessage());
            }
        }

        return "Nigeria";
    }

    @Override
    public long estimateTravelTime(double distanceKm, String vehicleType) {
        double speedKmPerHour;

        switch (vehicleType.toUpperCase()) {
            case "MOTORCYCLE":
                speedKmPerHour = 50.0;
                break;
            case "MINI_VAN":
                speedKmPerHour = 60.0;
                break;
            case "STANDARD":
                speedKmPerHour = 45.0;
                break;
            case "TRUCK":
                speedKmPerHour = 40.0;
                break;
            default:
                speedKmPerHour = 45.0;
        }

        // Add traffic factor for Nigeria (use 0.7-0.8 for traffic conditions)
        double trafficFactor = 0.75;
        double adjustedSpeed = speedKmPerHour * trafficFactor;

        if (adjustedSpeed <= 0) {
            return 0;
        }

        // Return estimated time in minutes
        return Math.round((distanceKm / adjustedSpeed) * 60);
    }

    @Override
    public boolean isAddressValid(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }

        // Basic validation: at least 5 characters
        if (address.trim().length() < 5) {
            return false;
        }

        // Check if address contains common patterns
        String[] parts = address.split(",");
        if (parts.length < 2) {
            // Nigeria addresses typically have at least City, State
            return false;
        }

        return true;
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    /**
     * Generate fallback coordinates for Nigeria regions
     * This is a simplified mapping for common Nigerian cities
     */
    private double[] generateFallbackCoordinates(String address) {
        String lowerAddress = address.toLowerCase();

        // Map of Nigerian cities to approximate coordinates
        if (lowerAddress.contains("lagos") || lowerAddress.contains("ikeja")) {
            return new double[]{6.5244, 3.3792};
        } else if (lowerAddress.contains("abuja") || lowerAddress.contains("fct")) {
            return new double[]{9.0579, 7.4951};
        } else if (lowerAddress.contains("kano")) {
            return new double[]{12.0022, 8.5919};
        } else if (lowerAddress.contains("ibadan")) {
            return new double[]{7.3776, 3.9470};
        } else if (lowerAddress.contains("port harcourt")) {
            return new double[]{4.8156, 7.0498};
        } else if (lowerAddress.contains("benin")) {
            return new double[]{6.3350, 5.6037};
        } else if (lowerAddress.contains("enugu")) {
            return new double[]{6.4463, 7.5130};
        } else if (lowerAddress.contains("kaduna")) {
            return new double[]{10.5105, 7.4165};
        } else if (lowerAddress.contains("aba")) {
            return new double[]{5.1066, 7.3667};
        } else if (lowerAddress.contains("maiduguri")) {
            return new double[]{11.8333, 13.1500};
        } else if (lowerAddress.contains("ilorin")) {
            return new double[]{8.4966, 4.5426};
        } else if (lowerAddress.contains("warri")) {
            return new double[]{5.5174, 5.7506};
        } else if (lowerAddress.contains("jos")) {
            return new double[]{9.8965, 8.8583};
        } else if (lowerAddress.contains("akure")) {
            return new double[]{7.2571, 5.2058};
        } else if (lowerAddress.contains("owerri")) {
            return new double[]{5.4764, 7.0300};
        } else {
            // Default to Lagos if unknown
            return new double[]{6.5244, 3.3792};
        }
    }
}