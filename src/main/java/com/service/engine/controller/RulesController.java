package com.service.engine.controller;

import com.service.engine.dto.RuleCreateRequest;
import com.service.engine.dto.RuleResponse;
import com.service.engine.dto.RuleUpdateRequest;
import com.service.engine.mapper.RuleMapper;
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
    public List<RuleResponse> list(@RequestParam(required = false) String decisionType) {
        List<Rule> rules;
        if (decisionType == null || decisionType.isBlank()) {
            rules = ruleRepository.findAll();
        } else {
            rules = ruleRepository.findByDecisionTypeOrderByPriorityAsc(decisionType);
        }
        return rules.stream().map(RuleMapper::toResponse).toList();
    }

    @PostMapping
    public RuleResponse create(@Valid @RequestBody RuleCreateRequest req) {
        Rule rule = RuleMapper.toEntity(req);
        Rule saved = ruleRepository.save(rule);
        return RuleMapper.toResponse(saved);
    }

    @PostMapping("/evict-cache")
    public void evictCache() {
        decisionService.evictCache();
    }

    @PutMapping("/{id}")
    public RuleResponse update(@PathVariable Long id, @Valid @RequestBody RuleUpdateRequest req) {
        Rule existing = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));

        RuleMapper.applyUpdate(existing, req);
        Rule saved = ruleRepository.save(existing);
        return RuleMapper.toResponse(saved);
    }

    @PatchMapping("/{id}/enable")
    public RuleResponse enable(@PathVariable Long id) {
        Rule r = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));
        r.setEnabled(true);
        return RuleMapper.toResponse(ruleRepository.save(r));
    }

    @PatchMapping("/{id}/disable")
    public RuleResponse disable(@PathVariable Long id) {
        Rule r = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));
        r.setEnabled(false);
        return RuleMapper.toResponse(ruleRepository.save(r));
    }
}