package com.poweroutage.outage.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOutageReportRequest(
        @NotBlank @Size(max = 30) @Pattern(regexp = "^[A-Z0-9-]+$") String customerCode,
        @NotBlank @Size(max = 30) @Pattern(regexp = "^[A-Z0-9-]+$") String servicePointCode,
        @NotBlank @Size(max = 200) String reporterName,
        @NotBlank @Pattern(regexp = "^(0|\\+84)[0-9]{9}$") String phoneNumber,
        @NotBlank @Size(max = 500) String address,
        @Size(max = 1000) String description) {
}
