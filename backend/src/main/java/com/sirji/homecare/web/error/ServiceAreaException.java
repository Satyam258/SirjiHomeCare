package com.sirji.homecare.web.error;

public class ServiceAreaException extends RuntimeException {
    public ServiceAreaException() {
        super("The delivery address is outside our current service area.");
    }
}
