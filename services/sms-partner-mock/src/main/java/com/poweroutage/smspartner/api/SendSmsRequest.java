package com.poweroutage.smspartner.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record SendSmsRequest(
        @NotBlank @Pattern(regexp = "^(0|\\+84)[0-9]{9}$") String phoneNumber,
        @NotBlank @Size(max = 100) String templateCode,
        @NotEmpty Map<String, String> parameters) {
}
