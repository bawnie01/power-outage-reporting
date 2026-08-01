package com.poweroutage.outage.common;

import java.util.UUID;

public class OutageReportNotFoundException extends RuntimeException {

    public OutageReportNotFoundException(UUID id) {
        super("Outage report %s was not found.".formatted(id));
    }
}
