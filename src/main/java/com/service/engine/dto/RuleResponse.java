package com.service.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RuleResponse(
        @Schema(description = "Rule id", example = "123")
        Long id,
        @Schema(description = "Decision type", example = "CREDIT")
        String decisionType,
        @Schema(description = "Priority", example = "10")
        Integer priority,
        @Schema(description = "Rule is active", example = "true")
        Boolean enabled,
        @Schema(description = "Condition expression", example = "age >= 18")
        String condition,
        @Schema(description = "Effect", example = "ALLOW:OK")
        String effect,
        @Schema(description = "Description", example = "MIN_AGE")
        String description
) {}
