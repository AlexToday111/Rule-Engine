package com.service.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RuleUpdateRequest(
        @Schema(description = "Decision type", example = "CREDIT")
        @NotBlank String decisionType,

        @Schema(description = "Priority. Lower value = higher priority", example = "10")
        @NotNull Integer priority,

        @Schema(description = "Rule is active", example = "true")
        @NotNull Boolean enabled,

        @Schema(description = "Condition expression", example = "age >= 18 && score > 600")
        @NotBlank String condition,

        @Schema(description = "Effect in ACTION:REASON form", example = "DENY:UNDERAGE")
        @NotBlank String effect,

        @Schema(description = "Human-readable rule name", example = "MIN_AGE")
        @NotBlank String description
) {}