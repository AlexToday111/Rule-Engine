package com.service.engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.engine.dto.DecisionRequest;
import com.service.engine.dto.DecisionResponse;
import com.service.engine.service.DecisionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import java.util.Map;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DecisionControllerTest.class)
public class DecisionControllerTest {
    @Resource
    MockMvc mockMvc;

    @Resource
    ObjectMapper objectMapper;

    @MockBean
    DecisionService decisionService;

    @Test
    void shouldReturn400WhenRequestInvalid() throws Exception {
        DecisionRequest req = new DecisionRequest();
        req.setDecisionType(""); // invalid
        req.setSubjectId("user-1");
        req.setAttributes(Map.of("age", 17));

        mockMvc.perform(post("/api/v1/decisions/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenRequestValid() throws Exception {
        DecisionResponse res = new DecisionResponse();
        res.setDecision(DecisionResponse.Decision.ALLOW);
        res.setScore(null);
        res.setExecutionTimeMs(5);

        DecisionResponse.TriggeredRule tr = new DecisionResponse.TriggeredRule();
        tr.setRuleId("1");
        tr.setRuleName("MIN_AGE");
        tr.setMatched(false);
        tr.setReason(null);
        res.setTriggeredRules(List.of(tr));

        when(decisionService.evaluate(any())).thenReturn(res);

        DecisionRequest req = new DecisionRequest();
        req.setDecisionType("TEST");
        req.setSubjectId("user-1");
        req.setAttributes(Map.of("age", 20));

        mockMvc.perform(post("/api/v1/decisions/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("ALLOW"))
                .andExpect(jsonPath("$.executionTimeMs").value(5));
    }
}
