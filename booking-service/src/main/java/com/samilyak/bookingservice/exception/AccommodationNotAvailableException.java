package com.samilyak.bookingservice.exception;

public class AccommodationNotAvailableException extends RuntimeException {
    public AccommodationNotAvailableException(String message) {
        super(message);
    }
}
