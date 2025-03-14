package com.samilyak.bookingservice.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
