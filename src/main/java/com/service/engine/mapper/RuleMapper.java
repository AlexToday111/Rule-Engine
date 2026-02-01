package com.service.engine.mapper;

import com.service.engine.dto.RuleCreateRequest;
import com.service.engine.dto.RuleResponse;
import com.service.engine.dto.RuleUpdateRequest;
import com.service.engine.model.Rule;

public class RuleMapper {
    private RuleMapper(){}

    public static Rule toEntity(RuleCreateRequest req) {
        Rule r = new Rule();
        r.setDecisionType(req.decisionType());
        r.setPriority(req.priority());
        r.setEnabled(req.enabled());
        r.setCondition(req.condition());
        r.setEffect(req.effect());
        r.setDescription(req.description());
        return r;
    }

    public static void applyUpdate(Rule entity, RuleUpdateRequest req){
        entity.setDecisionType(req.decisionType());
        entity.setPriority(req.priority());
        entity.setEnabled(req.enabled());
        entity.setCondition(req.condition());
        entity.setEffect(req.effect());
        entity.setDescription(req.description());
    }

    public static RuleResponse toResponse(Rule r) {
        return new RuleResponse(
                r.getId(),
                r.getDecisionType(),
                r.getPriority(),
                r.isEnabled(),
                r.getCondition(),
                r.getEffect(),
                r.getDescription()
        );
    }
}
