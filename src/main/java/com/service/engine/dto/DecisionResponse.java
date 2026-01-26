package com.service.engine.dto;

import java.util.List;

public class DecisionResponse {

    private Decision decision;
    private Integer score;
    private List<TriggeredRule> triggeredRules;
    private long executionTimeMs;

    public enum Decision {
        ALLOW,
        DENY,
        REVIEW
    }

    public static class TriggeredRule{
        private String ruleId;
        private String ruleName;
        private boolean matched;
        private String reason;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public List<TriggeredRule> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(List<TriggeredRule> triggeredRules) {
        this.triggeredRules = triggeredRules;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}
