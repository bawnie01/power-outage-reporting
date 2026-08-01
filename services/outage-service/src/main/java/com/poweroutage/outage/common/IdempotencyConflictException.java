package com.poweroutage.outage.common;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException() {
        super("The idempotency key was reused with different request data.");
    }
}
