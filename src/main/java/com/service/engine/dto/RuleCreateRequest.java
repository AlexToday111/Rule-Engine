package com.service.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RuleCreateRequest(
        @NotBlank
        @Schema(description = "Decision type", example = "CREDIT")
        String decisionType,
        @NotBlank
        @Schema(description = "Priority. Lower value = higher priority", example = "10")
        Integer priority,
        @NotNull
        @Schema(description = "Rule is active", example = "true")
        Boolean enabled,
        @NotBlank
        @Schema(description = "Condition expression", example = "age >= 18 && score > 600")
        String condition,
        @NotBlank
        @Schema(description = "Effect in ACTION:REASON form", example = "DENY:UNDERAGE")
        String effect,
        @NotBlank
        @Schema(description = "Human-readable rule name", example = "MIN_AGE")
        String description
) {
}