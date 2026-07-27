package com.inkfront.logisticsApplication.service.interfaces;

public interface DistanceService {

    double calculateDistance(double lat1, double lon1, double lat2, double lon2);

    double calculateDistance(String address1, String address2);

    double getDistanceInKm(double lat1, double lon1, double lat2, double lon2);

    double getDistanceInMiles(double lat1, double lon1, double lat2, double lon2);

    String geocodeAddress(String address);

    double[] geocodeAddressToCoordinates(String address);

    String reverseGeocode(double latitude, double longitude);

    long estimateTravelTime(double distanceKm, String vehicleType);

    boolean isAddressValid(String address);
}