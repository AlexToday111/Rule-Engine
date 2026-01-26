package com.service.engine.controllers;

import com.service.engine.model.Rule;
import com.service.engine.repository.RuleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
public class RulesController {

    private final RuleRepository ruleRepository;

    public RulesController(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @GetMapping
    public List<Rule> list(@RequestParam(required = false) String decisionType) {
        if (decisionType == null || decisionType.isBlank()) {
            return ruleRepository.findAll();
        }
        return ruleRepository.findByDecisionTypeOrderByPriorityAsc(decisionType);
    }

    @PostMapping
    public Rule create(@RequestBody Rule rule) {
        rule.setId(null); // чтобы случайно не перезаписать
        return ruleRepository.save(rule);
    }

    @PutMapping("/{id}")
    public Rule update(@PathVariable Long id, @RequestBody Rule incoming) {
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
