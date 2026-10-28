package com.sirji.homecare.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistanceCalculatorTest {
    private final DistanceCalculator calculator = new DistanceCalculator();

    @Test
    void returnsZeroForTheSameCoordinate() {
        assertEquals(0.0, calculator.distanceKm(12.9716, 77.5946, 12.9716, 77.5946), 0.000001);
    }

    @Test
    void calculatesOneDegreeAtTheEquator() {
        assertEquals(111.195, calculator.distanceKm(0, 0, 0, 1), 0.02);
    }

    @Test
    void handlesAntipodalPointsWithoutFloatingPointError() {
        assertEquals(20015.1, calculator.distanceKm(0, 0, 0, 180), 1.0);
    }
}
