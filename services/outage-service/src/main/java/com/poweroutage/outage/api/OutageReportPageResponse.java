package com.poweroutage.outage.api;

import org.springframework.data.domain.Page;

import java.util.List;

public record OutageReportPageResponse(
        List<OutageReportResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static OutageReportPageResponse from(Page<com.poweroutage.outage.domain.OutageReport> result) {
        return new OutageReportPageResponse(
                result.getContent().stream().map(OutageReportResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }
}
