package com.service.engine.controller;

import com.service.engine.cache.RuleCache;
import com.service.engine.dto.RuleCreateRequest;
import com.service.engine.dto.RuleResponse;
import com.service.engine.dto.RuleUpdateRequest;
import com.service.engine.exception.InvalidRuleException;
import com.service.engine.exception.RuleNotFoundException;
import com.service.engine.mapper.RuleMapper;
import com.service.engine.model.Rule;
import com.service.engine.repository.RuleRepository;
import com.service.engine.service.DecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
public class RulesController {

    private final RuleRepository ruleRepository;
    private final DecisionService decisionService;
    private final RuleCache ruleCache;

    public RulesController(RuleRepository ruleRepository, DecisionService decisionService, RuleCache ruleCache) {
        this.ruleRepository = ruleRepository;
        this.decisionService = decisionService;
        this.ruleCache = ruleCache;
    }

    @Operation(
            summary = "List rules",
            description = "Returns all rules or only rules for a specific decision"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rules list return")
    })
    @GetMapping
    public List<RuleResponse> list(@RequestParam(required = false) String decisionType) {
        List<Rule> rules;
        if (decisionType == null || decisionType.isBlank()) {
            rules = ruleRepository.findAll();
        } else {
            rules = ruleRepository.findByDecisionTypeOrderByPriorityAsc(decisionType);
        }
        return rules.stream().map(RuleMapper::toResponse).toList();
    }

    @Operation(
            summary = "Create rule",
            description = "Creates a new decision rule"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule created"),
            @ApiResponse(responseCode = "400", description = "Validation or business error")
    })
    @PostMapping
    public RuleResponse create(@Valid @RequestBody RuleCreateRequest req) {
        validateRuleBusiness(req);
        Rule rule = RuleMapper.toEntity(req);
        Rule saved = ruleRepository.save(rule);
        return RuleMapper.toResponse(saved);
    }

    @Operation(
            summary = "Evict rules cache",
            description = "Clears all cached rules"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cache cleared")
    })
    @PostMapping("/evict-cache")
    public void evictCache() {
        ruleCache.evictAll();
    }

    @Operation(
            summary = "Update rule",
            description = "Updates an existing rule by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule updated"),
            @ApiResponse(responseCode = "400", description = "Validation a rule"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PutMapping("/{id}")
    public RuleResponse update(@PathVariable Long id, @Valid @RequestBody RuleUpdateRequest req) {
        validateRuleBusiness(req);
        Rule existing = ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException(id));

        RuleMapper.applyUpdate(existing, req);
        Rule saved = ruleRepository.save(existing);
        return RuleMapper.toResponse(saved);
    }

    @Operation(
            summary = "Enable rule",
            description = "Enables a rule by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule enabled"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PatchMapping("/{id}/enable")
    public RuleResponse enable(@PathVariable Long id) {
        Rule r = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));
        r.setEnabled(true);
        return RuleMapper.toResponse(ruleRepository.save(r));
    }

    @Operation(
            summary = "Disable rule",
            description = "Disables a rule by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule disabled"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PatchMapping("/{id}/disable")
    public RuleResponse disable(@PathVariable Long id) {
        Rule r = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));
        r.setEnabled(false);
        return RuleMapper.toResponse(ruleRepository.save(r));
    }

    private void validateRuleBusiness(RuleCreateRequest req) {
        if (req.priority() != null && req.priority() < 0) {
            throw new InvalidRuleException("priority must be >= 0");
        }
        if (!isValidEffect(req.effect())) {
            throw new InvalidRuleException("effect must start with ALLOW/DENY/REVIEW");
        }
    }

    private void validateRuleBusiness(RuleUpdateRequest req) {
        if (req.priority() != null && req.priority() < 0) {
            throw new InvalidRuleException("priority must be >= 0");
        }
        if (!isValidEffect(req.effect())) {
            throw new InvalidRuleException("effect must start with ALLOW/DENY/REVIEW");
        }
    }

    private boolean isValidEffect(String effect) {
        if (effect == null || effect.isBlank()) return false;
        String action = effect.split(":", 2)[0].trim().toUpperCase();
        return action.equals("ALLOW") || action.equals("DENY") || action.equals("REVIEW");
    }
}