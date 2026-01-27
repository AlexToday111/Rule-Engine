package com.service.engine.dto;

import jakarta.validation.constraints.*;

import java.util.Map;

public class DecisionRequest {

    @NotBlank
    private String decisionType;
    @NotBlank
    private String subjectId;
    @NotNull
    private Map<String, Object> attributes;

    public String getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(String decisionType) {
        this.decisionType = decisionType;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
