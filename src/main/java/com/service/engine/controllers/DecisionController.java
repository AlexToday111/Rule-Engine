package com.service.engine.controllers;

import com.service.engine.dto.DecisionRequest;
import com.service.engine.dto.DecisionResponse;
import com.service.engine.service.DecisionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/decisions")
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @PostMapping("/evaluate")
    public DecisionResponse evaluate(@RequestBody DecisionRequest request) {
        return decisionService.evaluate(request);
    }
}
