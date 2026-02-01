package com.service.engine.controller;

import com.service.engine.dto.DecisionRequest;
import com.service.engine.dto.DecisionResponse;
import com.service.engine.service.DecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/decisions")
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @Operation(
            summary = "Evaluate decision",
            description = "Evaluates decision rules and returns the decision result with trace"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Decision evaluated"),
            @ApiResponse(responseCode = "400", description = "Validation or condition syntax error")
    })
    @PostMapping("/evaluate")
    public DecisionResponse evaluate(@Valid @RequestBody DecisionRequest request) {
        return decisionService.evaluate(request);
    }
}
