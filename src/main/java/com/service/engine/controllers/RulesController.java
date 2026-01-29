package com.service.engine.controllers;

import com.service.engine.model.Rule;
import com.service.engine.repository.RuleRepository;
import com.service.engine.service.DecisionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
public class RulesController {

    private final RuleRepository ruleRepository;
    private final DecisionService decisionService;

    public RulesController(RuleRepository ruleRepository, DecisionService decisionService) {
        this.ruleRepository = ruleRepository;
        this.decisionService = decisionService;
    }

    @GetMapping
    public List<Rule> list(@RequestParam(required = false) String decisionType) {
        if (decisionType == null || decisionType.isBlank()) {
            return ruleRepository.findAll();
        }
        return ruleRepository.findByDecisionTypeOrderByPriorityAsc(decisionType);
    }

    @PostMapping
    public Rule create(@Valid @RequestBody Rule rule) {
        rule.setId(null);
        return ruleRepository.save(rule);
    }

    @PostMapping
    public void evictCache() {
        decisionService.evictCache();
    }

    @PutMapping("/{id}")
    public Rule update(@Valid @PathVariable Long id, @RequestBody Rule incoming) {
        Rule existing = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));

        existing.setDecisionType(incoming.getDecisionType());
        existing.setPriority(incoming.getPriority());
        existing.setEnabled(incoming.isEnabled());
        existing.setCondition(incoming.getCondition());
        existing.setEffect(incoming.getEffect());
        existing.setDescription(incoming.getDescription());

        return ruleRepository.save(existing);
    }

    @PatchMapping("/{id}/enable")
    public Rule enable(@PathVariable Long id) {
        Rule r = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));
        r.setEnabled(true);
        return ruleRepository.save(r);
    }

    @PatchMapping("/{id}/disable")
    public Rule disable(@PathVariable Long id) {
        Rule r = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));
        r.setEnabled(false);
        return ruleRepository.save(r);
    }
}
