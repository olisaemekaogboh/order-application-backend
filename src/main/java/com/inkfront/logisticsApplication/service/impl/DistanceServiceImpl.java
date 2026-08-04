package com.inkfront.logisticsApplication.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkfront.logisticsApplication.service.interfaces.DistanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistanceServiceImpl implements DistanceService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.maps.api-key:}")
    private String googleMapsApiKey;

    // Google Maps API URLs
    private static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    // OpenStreetMap Nominatim API (Free, no key required)
    private static final String OSM_GEOCODE_URL = "https://nominatim.openstreetmap.org/search";

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double EARTH_RADIUS_MILES = 3959.0;

    // Fallback city coordinates (used only when Google Maps API and OSM fail)
    private static final Map<String, double[]> CITY_COORDINATES = new HashMap<>();

    static {
        // Major cities in Nigeria (fallback only)
        CITY_COORDINATES.put("lagos", new double[]{6.5244, 3.3792});
        CITY_COORDINATES.put("ikeja", new double[]{6.6018, 3.3515});
        CITY_COORDINATES.put("abuja", new double[]{9.0579, 7.4951});
        CITY_COORDINATES.put("fct", new double[]{9.0579, 7.4951});
        CITY_COORDINATES.put("kano", new double[]{12.0022, 8.5919});
        CITY_COORDINATES.put("ibadan", new double[]{7.3776, 3.9470});
        CITY_COORDINATES.put("port harcourt", new double[]{4.8156, 7.0498});
        CITY_COORDINATES.put("ph", new double[]{4.8156, 7.0498});
        CITY_COORDINATES.put("benin", new double[]{6.3350, 5.6037});
        CITY_COORDINATES.put("enugu", new double[]{6.4463, 7.5130});
        CITY_COORDINATES.put("kaduna", new double[]{10.5105, 7.4165});
        CITY_COORDINATES.put("aba", new double[]{5.1066, 7.3667});
        CITY_COORDINATES.put("maiduguri", new double[]{11.8333, 13.1500});
        CITY_COORDINATES.put("ilorin", new double[]{8.4966, 4.5426});
        CITY_COORDINATES.put("warri", new double[]{5.5174, 5.7506});
        CITY_COORDINATES.put("jos", new double[]{9.8965, 8.8583});
        CITY_COORDINATES.put("akure", new double[]{7.2571, 5.2058});
        CITY_COORDINATES.put("owerri", new double[]{5.4764, 7.0300});
        CITY_COORDINATES.put("onitsha", new double[]{6.1667, 6.7833});
        CITY_COORDINATES.put("asaba", new double[]{6.1984, 6.7265});
        CITY_COORDINATES.put("abakaliki", new double[]{6.3333, 8.1167});
        CITY_COORDINATES.put("awka", new double[]{6.2167, 7.0667});
        CITY_COORDINATES.put("ummuahia", new double[]{5.5333, 7.4833});
        CITY_COORDINATES.put("calabar", new double[]{4.9500, 8.3250});
        CITY_COORDINATES.put("uyo", new double[]{5.0333, 7.9167});
        CITY_COORDINATES.put("sokoto", new double[]{13.0667, 5.2333});
        CITY_COORDINATES.put("katsina", new double[]{12.9833, 7.6000});
        CITY_COORDINATES.put("zaria", new double[]{11.0667, 7.7000});
        CITY_COORDINATES.put("minna", new double[]{9.6167, 6.5500});
        CITY_COORDINATES.put("lokoja", new double[]{7.8000, 6.7333});
        CITY_COORDINATES.put("makurdi", new double[]{7.7333, 8.5333});
        CITY_COORDINATES.put("yola", new double[]{9.2000, 12.5000});
        CITY_COORDINATES.put("jalingo", new double[]{8.9000, 11.3667});
        CITY_COORDINATES.put("damaturu", new double[]{11.7500, 11.9667});
        CITY_COORDINATES.put("bauchi", new double[]{10.3167, 9.8333});
        CITY_COORDINATES.put("gombe", new double[]{10.2833, 11.1667});
        CITY_COORDINATES.put("osogbo", new double[]{7.7667, 4.5667});
        CITY_COORDINATES.put("abeokuta", new double[]{7.1500, 3.3500});
        CITY_COORDINATES.put("adewale", new double[]{6.4333, 3.4167});
    }

    // ==================== MAIN DISTANCE CALCULATION METHODS ====================

    @Override
    public double calculateDistance(String address1, String address2) {
        log.info("Calculating distance between addresses: '{}' and '{}'", address1, address2);

        // 1. Try Google Maps Distance Matrix API first (if key is available)
        if (googleMapsApiKey != null && !googleMapsApiKey.isEmpty()) {
            try {
                Double distance = getDistanceFromGoogleMaps(address1, address2);
                if (distance != null && distance > 0) {
                    log.info("Distance from Google Maps: {} km", distance);
                    return distance;
                }
            } catch (Exception e) {
                log.warn("Google Maps distance calculation failed: {}", e.getMessage());
            }
        }

        // 2. Try OpenStreetMap geocoding + Haversine (free, no key required)
        try {
            Double distance = getDistanceFromOpenStreetMap(address1, address2);
            if (distance != null && distance > 0) {
                log.info("Distance from OpenStreetMap: {} km", distance);
                return distance;
            }
        } catch (Exception e) {
            log.warn("OpenStreetMap distance calculation failed: {}", e.getMessage());
        }

        // 3. Fallback: Use city coordinates map + Haversine
        try {
            double[] coords1 = generateFallbackCoordinates(address1);
            double[] coords2 = generateFallbackCoordinates(address2);

            if (coords1 != null && coords2 != null) {
                double distance = calculateDistance(coords1[0], coords1[1], coords2[0], coords2[1]);
                log.info("Calculated distance using fallback coordinates: {} km", distance);
                return distance;
            }
        } catch (Exception e) {
            log.error("Failed to calculate distance using fallback: {}", e.getMessage());
        }

        log.warn("All distance calculation methods failed, returning 0.0");
        return 0.0;
    }

    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for fallback calculation
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    // ==================== GOOGLE MAPS API METHODS ====================

    /**
     * Get distance from Google Maps Distance Matrix API
     */
    private Double getDistanceFromGoogleMaps(String origin, String destination) {
        try {
            String url = String.format("%s?origins=%s&destinations=%s&key=%s",
                    DISTANCE_MATRIX_URL,
                    origin.replace(" ", "+"),
                    destination.replace(" ", "+"),
                    googleMapsApiKey);

            log.debug("Calling Google Distance Matrix API: {}", url);

            var response = restTemplate.getForObject(url, Map.class);

            if (response != null && "OK".equals(response.get("status"))) {
                var rows = (java.util.List<?>) response.get("rows");
                if (rows != null && !rows.isEmpty()) {
                    var firstRow = (Map<?, ?>) rows.get(0);
                    var elements = (java.util.List<?>) firstRow.get("elements");
                    if (elements != null && !elements.isEmpty()) {
                        var firstElement = (Map<?, ?>) elements.get(0);
                        if ("OK".equals(firstElement.get("status"))) {
                            var distance = (Map<?, ?>) firstElement.get("distance");
                            if (distance != null) {
                                // Distance in meters, convert to km
                                double valueInMeters = ((Number) distance.get("value")).doubleValue();
                                return valueInMeters / 1000.0;
                            }
                        } else {
                            log.warn("Distance Matrix API element status: {}", firstElement.get("status"));
                        }
                    }
                }
            } else {
                log.warn("Distance Matrix API status: {}", response != null ? response.get("status") : "null");
            }
        } catch (Exception e) {
            log.warn("Failed to get distance from Google Maps: {}", e.getMessage());
        }
        return null;
    }

    // ==================== OPENSTREETMAP METHODS (FREE) ====================

    /**
     * Get distance using OpenStreetMap geocoding (completely free, no API key)
     */
    private Double getDistanceFromOpenStreetMap(String address1, String address2) {
        try {
            double[] coords1 = geocodeWithOpenStreetMap(address1);
            double[] coords2 = geocodeWithOpenStreetMap(address2);

            if (coords1 != null && coords2 != null) {
                return calculateDistance(coords1[0], coords1[1], coords2[0], coords2[1]);
            }
        } catch (Exception e) {
            log.warn("OpenStreetMap geocoding failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Geocode using OpenStreetMap Nominatim API (FREE, no API key required)
     * Rate limit: 1 request per second (be respectful)
     */
    private double[] geocodeWithOpenStreetMap(String address) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(OSM_GEOCODE_URL)
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", "1")
                    .build()
                    .toUriString();

            log.debug("Calling OpenStreetMap Nominatim API: {}", url);

            // OpenStreetMap requires a User-Agent header
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "LogisticsApp/1.0 (contact@yourdomain.com)");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode json = objectMapper.readTree(response.getBody());

            if (json.isArray() && json.size() > 0) {
                JsonNode first = json.get(0);
                if (first.has("lat") && first.has("lon")) {
                    double lat = first.get("lat").asDouble();
                    double lon = first.get("lon").asDouble();
                    log.debug("OpenStreetMap geocoded to: ({}, {})", lat, lon);
                    return new double[]{lat, lon};
                }
            }
        } catch (Exception e) {
            log.warn("OpenStreetMap geocoding failed for '{}': {}", address, e.getMessage());
        }
        return null;
    }

    // ==================== GEOCODING METHODS ====================

    @Override
    public String geocodeAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        // Try Google Maps Geocoding API
        if (googleMapsApiKey != null && !googleMapsApiKey.isEmpty()) {
            try {
                String geocoded = geocodeWithGoogleMaps(address);
                if (geocoded != null) {
                    return geocoded;
                }
            } catch (Exception e) {
                log.warn("Google Maps geocoding failed: {}", e.getMessage());
            }
        }

        // Try OpenStreetMap
        try {
            double[] coords = geocodeWithOpenStreetMap(address);
            if (coords != null) {
                return coords[0] + "," + coords[1];
            }
        } catch (Exception e) {
            log.warn("OpenStreetMap geocoding failed: {}", e.getMessage());
        }

        // Fallback: return formatted address
        return address;
    }

    @Override
    public double[] geocodeAddressToCoordinates(String address) {
        if (address == null || address.isEmpty()) {
            log.warn("Address is null or empty");
            return null;
        }

        log.debug("Geocoding address to coordinates: {}", address);

        // 1. Try Google Maps Geocoding API
        if (googleMapsApiKey != null && !googleMapsApiKey.isEmpty()) {
            try {
                double[] coords = geocodeWithGoogleMapsToCoordinates(address);
                if (coords != null) {
                    log.debug("Google Maps geocoded to: ({}, {})", coords[0], coords[1]);
                    return coords;
                }
            } catch (Exception e) {
                log.warn("Google Maps geocoding failed: {}", e.getMessage());
            }
        }

        // 2. Try OpenStreetMap (free)
        try {
            double[] coords = geocodeWithOpenStreetMap(address);
            if (coords != null) {
                log.debug("OpenStreetMap geocoded to: ({}, {})", coords[0], coords[1]);
                return coords;
            }
        } catch (Exception e) {
            log.warn("OpenStreetMap geocoding failed: {}", e.getMessage());
        }

        // 3. Fallback: use city coordinates map
        double[] coords = generateFallbackCoordinates(address);
        log.debug("Fallback coordinates for '{}': ({}, {})", address, coords[0], coords[1]);
        return coords;
    }

    /**
     * Geocode using Google Maps Geocoding API
     */
    private String geocodeWithGoogleMaps(String address) {
        try {
            String url = String.format("%s?address=%s&key=%s",
                    GEOCODE_URL,
                    address.replace(" ", "+"),
                    googleMapsApiKey);

            var response = restTemplate.getForObject(url, Map.class);

            if (response != null && "OK".equals(response.get("status"))) {
                var results = (java.util.List<?>) response.get("results");
                if (results != null && !results.isEmpty()) {
                    var firstResult = (Map<?, ?>) results.get(0);
                    var geometry = (Map<?, ?>) firstResult.get("geometry");
                    var location = (Map<?, ?>) geometry.get("location");

                    double lat = ((Number) location.get("lat")).doubleValue();
                    double lng = ((Number) location.get("lng")).doubleValue();

                    return lat + "," + lng;
                }
            }
        } catch (Exception e) {
            log.warn("Google Maps geocoding failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Geocode using Google Maps Geocoding API and return coordinates
     */
    private double[] geocodeWithGoogleMapsToCoordinates(String address) {
        String geocoded = geocodeWithGoogleMaps(address);
        if (geocoded != null) {
            String[] parts = geocoded.split(",");
            if (parts.length == 2) {
                try {
                    double lat = Double.parseDouble(parts[0]);
                    double lng = Double.parseDouble(parts[1]);
                    return new double[]{lat, lng};
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse coordinates from geocoded result");
                }
            }
        }
        return null;
    }

    @Override
    public String reverseGeocode(double latitude, double longitude) {
        if (!isValidCoordinate(latitude, longitude)) {
            return null;
        }

        // Try Google Maps
        if (googleMapsApiKey != null && !googleMapsApiKey.isEmpty()) {
            try {
                String url = String.format("%s?latlng=%f,%f&key=%s",
                        GEOCODE_URL, latitude, longitude, googleMapsApiKey);

                var response = restTemplate.getForObject(url, Map.class);

                if (response != null && "OK".equals(response.get("status"))) {
                    var results = (java.util.List<?>) response.get("results");
                    if (results != null && !results.isEmpty()) {
                        var firstResult = (Map<?, ?>) results.get(0);
                        return (String) firstResult.get("formatted_address");
                    }
                }
            } catch (Exception e) {
                log.warn("Reverse geocoding failed: {}", e.getMessage());
            }
        }

        return "Nigeria";
    }

    // ==================== HELPER METHODS ====================

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
            case "SEDAN":
                speedKmPerHour = 45.0;
                break;
            case "TRUCK":
                speedKmPerHour = 40.0;
                break;
            default:
                speedKmPerHour = 45.0;
        }

        // Add traffic factor for Nigeria
        double trafficFactor = 0.75;
        double adjustedSpeed = speedKmPerHour * trafficFactor;

        if (adjustedSpeed <= 0) {
            return 0;
        }

        return Math.round((distanceKm / adjustedSpeed) * 60);
    }

    @Override
    public boolean isAddressValid(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }

        if (address.trim().length() < 5) {
            return false;
        }

        String[] parts = address.split(",");
        return parts.length >= 2;
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    private double[] generateFallbackCoordinates(String address) {
        String lowerAddress = address.toLowerCase().trim();

        // Check full city names
        for (Map.Entry<String, double[]> entry : CITY_COORDINATES.entrySet()) {
            if (lowerAddress.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Check individual words
        String[] words = lowerAddress.split("[,\\s]+");
        for (String word : words) {
            if (CITY_COORDINATES.containsKey(word)) {
                return CITY_COORDINATES.get(word);
            }
        }

        // Default to Lagos
        log.warn("No coordinates found for '{}', defaulting to Lagos", address);
        return new double[]{6.5244, 3.3792};
    }
}