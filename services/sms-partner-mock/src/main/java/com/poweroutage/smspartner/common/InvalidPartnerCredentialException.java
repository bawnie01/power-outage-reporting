package com.poweroutage.smspartner.common;

public class InvalidPartnerCredentialException extends RuntimeException {

    public InvalidPartnerCredentialException() {
        super("The partner API key is missing or invalid.");
    }
}
