package com.poweroutage.smspartner.common;

public class PartnerUnavailableException extends RuntimeException {

    public PartnerUnavailableException() {
        super("The simulated SMS partner is unavailable.");
    }
}
