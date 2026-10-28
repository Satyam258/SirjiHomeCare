package com.sirji.homecare.service;

import org.springframework.stereotype.Component;

@Component
public class DistanceCalculator {
    private static final double EARTH_RADIUS_KM = 6371.0088;

    public double distanceKm(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
        double latitudeDelta = Math.toRadians(latitudeB - latitudeA);
        double longitudeDelta = Math.toRadians(longitudeB - longitudeA);
        double startLatitude = Math.toRadians(latitudeA);
        double endLatitude = Math.toRadians(latitudeB);

        double haversine = Math.pow(Math.sin(latitudeDelta / 2), 2)
            + Math.cos(startLatitude) * Math.cos(endLatitude)
            * Math.pow(Math.sin(longitudeDelta / 2), 2);
        double centralAngle = 2 * Math.atan2(Math.sqrt(Math.min(1, haversine)), Math.sqrt(Math.max(0, 1 - haversine)));
        return EARTH_RADIUS_KM * centralAngle;
    }
}
