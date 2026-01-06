package com.samilyak.accommodationservice.exception;

public class OptimisticLockingFailureException extends RuntimeException{
    public OptimisticLockingFailureException(String message) {
        super(message);
    }
}
