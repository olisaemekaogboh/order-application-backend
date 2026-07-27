package com.inkfront.logisticsApplication.util;

public class DistanceUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double EARTH_RADIUS_MILES = 3959.0;

    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static double calculateDistanceInMiles(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_MILES * c;
    }

    public static double kmToMiles(double km) {
        return km * 0.621371;
    }

    public static double milesToKm(double miles) {
        return miles * 1.60934;
    }

    public static long calculateEstimatedTime(double distanceKm, double speedKmPerHour) {
        if (speedKmPerHour <= 0) {
            return 0;
        }
        return Math.round(distanceKm / speedKmPerHour * 60); // Return minutes
    }

    public static long calculateEstimatedTimeByVehicle(double distanceKm, String vehicleType) {
        double speed;
        switch (vehicleType.toUpperCase()) {
            case "MOTORCYCLE":
                speed = 50.0;
                break;
            case "MINI_VAN":
                speed = 60.0;
                break;
            case "STANDARD":
                speed = 45.0;
                break;
            case "TRUCK":
                speed = 40.0;
                break;
            default:
                speed = 45.0;
        }
        return calculateEstimatedTime(distanceKm, speed);
    }

    public static boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusKm) {
        return calculateDistance(lat1, lon1, lat2, lon2) <= radiusKm;
    }

    public static double[] getMidpoint(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double bx = Math.cos(lat2Rad) * Math.cos(dLon);
        double by = Math.cos(lat2Rad) * Math.sin(dLon);

        double lat3 = Math.atan2(Math.sin(lat1Rad) + Math.sin(lat2Rad),
                Math.sqrt((Math.cos(lat1Rad) + bx) * (Math.cos(lat1Rad) + bx) + by * by));
        double lon3 = Math.toRadians(lon1) + Math.atan2(by, Math.cos(lat1Rad) + bx);

        return new double[]{Math.toDegrees(lat3), Math.toDegrees(lon3)};
    }

    public static boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            return String.format("%.0f m", distanceKm * 1000);
        } else if (distanceKm < 10) {
            return String.format("%.1f km", distanceKm);
        } else {
            return String.format("%.0f km", distanceKm);
        }
    }
}