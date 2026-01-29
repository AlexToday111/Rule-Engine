package com.service.engine.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.ObjectError;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class DecisionContext {
    private final String subjectId;
    private final String decisionType;
    private final Map<String, Object> attributes;

    public DecisionContext(String subjectId, String decisionType, Map<String, Object> attributes) {
        this.subjectId = subjectId;
        this.decisionType = decisionType;
        this.attributes = attributes;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getDecisionType() {
        return decisionType;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Map<String, Object> attributes(){
        return attributes;
    }

    public boolean has(String key){
        return attributes != null && attributes.containsKey(key) && attributes.get(key) != null;
    }

    public Object get(String key){
        return attributes == null ? null : attributes.get(key);
    }

    public String getString(String key){
        Object v = get(key);
        return v == null ? null : String.valueOf(v);
    }

    public Integer getInt(String key){
        Object v = get(key);

        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return l.intValue();
        if (v instanceof Double d) return d.intValue();
        if (v instanceof BigDecimal bd) return bd.intValue();
        if (v instanceof String s) return Integer.parseInt(s);
        throw new IllegalArgumentException("Attribute " + key + " is not deciamal: " + v.getClass());
    }

    public Boolean getBoolean(String key) {
        Object v = get(key);
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s);
        throw new IllegalArgumentException("Attribute '" + key + "' is not a boolean: " + v.getClass());
    }
}
