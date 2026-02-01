package com.service.engine.dto;

public record RuleResponse (
        Long id,
        String decisionType,
        Integer priority,
        Boolean enabled,
        String condition,
        String effect,
        String description
) {}
