package com.service.engine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RuleCreateRequest(
        @NotBlank String decisionType,
        @NotBlank Integer priority,
        @NotNull Boolean enabled,
        @NotBlank String condition,
        @NotBlank String effect,
        @NotBlank String description
) {}
