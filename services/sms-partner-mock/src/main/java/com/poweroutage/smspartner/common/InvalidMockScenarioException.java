package com.poweroutage.smspartner.common;

public class InvalidMockScenarioException extends RuntimeException {

    public InvalidMockScenarioException(String scenario) {
        super("Unsupported mock scenario: " + scenario);
    }
}
