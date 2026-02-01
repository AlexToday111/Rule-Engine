package com.service.engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.engine.model.Rule;
import com.service.engine.repository.RuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RulesController.class)
class RulesControllerTest {

    @Resource
    MockMvc mockMvc;

    @Resource
    ObjectMapper objectMapper;

    @MockBean
    RuleRepository ruleRepository;

    @Test
    void shouldReturn400WhenRuleInvalid() throws Exception {
        Rule rule = new Rule();
        rule.setDecisionType(""); // invalid if @NotBlank стоит на entity
        rule.setPriority(1);
        rule.setEnabled(true);
        rule.setCondition("age < 18");
        rule.setEffect("DENY:UNDERAGE");
        rule.setDescription("MIN_AGE");

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rule)))
                .andExpect(status().isBadRequest());
    }
}
