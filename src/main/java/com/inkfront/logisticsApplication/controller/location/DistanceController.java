package com.inkfront.logisticsApplication.controller.location;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.location.CoordinatesDTO;
import com.inkfront.logisticsApplication.dto.response.location.TravelTimeDTO;
import com.inkfront.logisticsApplication.service.interfaces.DistanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for distance calculation, geocoding, and travel time estimation.
 * All endpoints return {@link ApiResponseDTO} wrapping the actual data.
 */
@Slf4j
@RestController
@RequestMapping("/api/distance")
@RequiredArgsConstructor
@Tag(name = "Distance Management", description = "Distance calculation, geocoding and travel estimation endpoints")
public class DistanceController {

    private final DistanceService distanceService;

    /**
     * Calculates the direct (crow‑fly) distance between two geographic coordinates.
     *
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return distance in kilometers
     */
    @GetMapping("/calculate")
    @Operation(summary = "Calculate distance using coordinates")
    public ResponseEntity<ApiResponseDTO<Double>> calculateDistance(
            @RequestParam double lat1,
            @RequestParam double lon1,
            @RequestParam double lat2,
            @RequestParam double lon2) {

        log.info("Calculating distance between coordinates ({}, {}) and ({}, {})", lat1, lon1, lat2, lon2);
        double distance = distanceService.calculateDistance(lat1, lon1, lat2, lon2);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, distance)
        );
    }

    /**
     * Calculates the distance between two addresses by geocoding them first.
     *
     * @param address1 first address
     * @param address2 second address
     * @return distance in kilometers
     */
    @GetMapping("/calculate-address")
    @Operation(summary = "Calculate distance between two addresses")
    public ResponseEntity<ApiResponseDTO<Double>> calculateDistanceByAddress(
            @RequestParam String address1,
            @RequestParam String address2) {

        log.info("Calculating distance between addresses: '{}' and '{}'", address1, address2);
        double distance = distanceService.calculateDistance(address1, address2);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, distance)
        );
    }

    /**
     * Returns the distance in kilometers between two coordinates.
     *
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return distance in kilometers
     */
    @GetMapping("/km")
    @Operation(summary = "Distance in kilometers")
    public ResponseEntity<ApiResponseDTO<Double>> getDistanceInKm(
            @RequestParam double lat1,
            @RequestParam double lon1,
            @RequestParam double lat2,
            @RequestParam double lon2) {

        log.info("Calculating distance in km between ({}, {}) and ({}, {})", lat1, lon1, lat2, lon2);
        double distance = distanceService.getDistanceInKm(lat1, lon1, lat2, lon2);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, distance)
        );
    }

    /**
     * Returns the distance in miles between two coordinates.
     *
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return distance in miles
     */
    @GetMapping("/miles")
    @Operation(summary = "Distance in miles")
    public ResponseEntity<ApiResponseDTO<Double>> getDistanceInMiles(
            @RequestParam double lat1,
            @RequestParam double lon1,
            @RequestParam double lat2,
            @RequestParam double lon2) {

        log.info("Calculating distance in miles between ({}, {}) and ({}, {})", lat1, lon1, lat2, lon2);
        double distance = distanceService.getDistanceInMiles(lat1, lon1, lat2, lon2);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, distance)
        );
    }

    /**
     * Geocodes an address to a formatted address string.
     *
     * @param address the address to geocode
     * @return formatted address string
     */
    @GetMapping("/geocode")
    @Operation(summary = "Geocode an address")
    public ResponseEntity<ApiResponseDTO<String>> geocodeAddress(
            @RequestParam String address) {

        log.info("Geocoding address: '{}'", address);
        String geocodedAddress = distanceService.geocodeAddress(address);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, geocodedAddress)
        );
    }

    /**
     * Converts an address to its geographic coordinates.
     *
     * @param address the address to convert
     * @return {@link CoordinatesDTO} containing latitude and longitude
     */
    @GetMapping("/coordinates")
    @Operation(summary = "Convert address to coordinates")
    public ResponseEntity<ApiResponseDTO<CoordinatesDTO>> geocodeCoordinates(
            @RequestParam String address) {

        log.info("Geocoding address to coordinates: '{}'", address);
        double[] coords = distanceService.geocodeAddressToCoordinates(address);

        CoordinatesDTO response = CoordinatesDTO.builder()
                .latitude(coords[0])
                .longitude(coords[1])
                .build();

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    /**
     * Performs reverse geocoding – converts coordinates to a human‑readable address.
     *
     * @param latitude  latitude of the location
     * @param longitude longitude of the location
     * @return formatted address string
     */
    @GetMapping("/reverse-geocode")
    @Operation(summary = "Reverse geocode coordinates")
    public ResponseEntity<ApiResponseDTO<String>> reverseGeocode(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        log.info("Reverse geocoding coordinates: ({}, {})", latitude, longitude);
        String address = distanceService.reverseGeocode(latitude, longitude);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, address)
        );
    }

    /**
     * Estimates travel time for a given distance and vehicle type.
     *
     * @param distanceKm the distance in kilometers
     * @param vehicleType the type of vehicle (e.g., "CAR", "BIKE", "TRUCK")
     * @return {@link TravelTimeDTO} containing distance, estimated minutes, and vehicle type
     */
    @GetMapping("/travel-time")
    @Operation(summary = "Estimate travel time")
    public ResponseEntity<ApiResponseDTO<TravelTimeDTO>> estimateTravelTime(
            @RequestParam double distanceKm,
            @RequestParam String vehicleType) {

        log.info("Estimating travel time for {} km with vehicle '{}'", distanceKm, vehicleType);
        long estimatedMinutes = distanceService.estimateTravelTime(distanceKm, vehicleType);

        TravelTimeDTO response = TravelTimeDTO.builder()
                .distanceKm(distanceKm)
                .estimatedMinutes(estimatedMinutes)
                .vehicleType(vehicleType)
                .build();

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    /**
     * Validates whether an address is recognised and can be geocoded.
     *
     * @param address the address to validate
     * @return {@code true} if the address is valid, otherwise {@code false}
     */
    @GetMapping("/validate-address")
    @Operation(summary = "Validate address")
    public ResponseEntity<ApiResponseDTO<Boolean>> validateAddress(
            @RequestParam String address) {

        log.info("Validating address: '{}'", address);
        boolean isValid = distanceService.isAddressValid(address);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, isValid)
        );
    }
}