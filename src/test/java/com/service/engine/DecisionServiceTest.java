package com.service.engine;

import com.service.engine.dto.DecisionRequest;
import com.service.engine.dto.DecisionResponse;
import com.service.engine.model.Rule;
import com.service.engine.repository.RuleRepository;
import com.service.engine.service.DecisionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DecisionServiceTest {

    @Mock
    RuleRepository ruleRepository;

    @InjectMocks
    DecisionService decisionService;

    @Test
    void denyShouldStopChain() {
        Rule r1 = ruleEntity(1L, "TEST", 1, true, "age >= 18", "ALLOW", "ADULT_ALLOW");
        Rule r2 = ruleEntity(2L, "TEST", 2, true, "age < 18", "DENY:UNDERAGE", "MIN_AGE");
        Rule r3 = ruleEntity(3L, "TEST", 3, true, "true == true", "ALLOW", "SHOULD_NOT_RUN");

        when(ruleRepository.findByDecisionTypeAndEnabledTrueOrderByPriorityAsc("TEST"))
                .thenReturn(List.of(r1, r2, r3));

        DecisionRequest request = request("TEST", "user-1", Map.of("age", 17));

        DecisionResponse response = decisionService.evaluate(request);

        assertEquals(DecisionResponse.Decision.DENY, response.getDecision());
        assertNotNull(response.getTriggeredRules());
        assertEquals(2, response.getTriggeredRules().size(), "Chain should stop after DENY");
        assertTrue(response.getExecutionTimeMs() >= 0);
    }

    @Test
    void shouldReturnAllowWhenNoRulesMatched() {
        Rule r1 = ruleEntity(1L, "TEST", 1, true, "age >= 18", "ALLOW", "ADULT_ALLOW");
        Rule r2 = ruleEntity(2L, "TEST", 2, true, "country == DE", "REVIEW:MANUAL", "COUNTRY_DE");

        when(ruleRepository.findByDecisionTypeAndEnabledTrueOrderByPriorityAsc("TEST"))
                .thenReturn(List.of(r1, r2));

        DecisionRequest request = request("TEST", "user-1", Map.of("age", 17, "country", "FR"));

        DecisionResponse response = decisionService.evaluate(request);

        assertEquals(DecisionResponse.Decision.ALLOW, response.getDecision(), "Default decision should be ALLOW");
        assertEquals(2, response.getTriggeredRules().size());
        assertFalse(response.getTriggeredRules().get(0).isMatched());
        assertFalse(response.getTriggeredRules().get(1).isMatched());
    }

    @Test
    void reviewShouldNotStopChain(){
        Rule r1 = ruleEntity(1L, "TEST", 1, true, "country != DE", "REVIEW:FOREIGN", "COUNTRY_CHECK");
        Rule r2 = ruleEntity(2L, "TEST", 2, true, "age >= 18", "ALLOW", "ADULT_ALLOW");

        when(ruleRepository.findByDecisionTypeAndEnabledTrueOrderByPriorityAsc("TEST"))
                .thenReturn(List.of(r1, r2));

        DecisionRequest request = request("TEST", "user-1", Map.of("age", 20, "country", "FR"));
        DecisionResponse response = decisionService.evaluate(request);

        assertEquals(DecisionResponse.Decision.REVIEW, response.getDecision());
        assertEquals(2, response.getTriggeredRules().size(), "REVIEW must not stop chain");
        assertTrue(response.getTriggeredRules().get(0).isMatched());
        assertTrue(response.getTriggeredRules().get(1).isMatched());

    }

    @Test
    void invalidEffectShouldThrow(){
        Rule r = ruleEntity(1L, "TEST", 1, true, "true == true", "BANANA", "BAD_EFFECT");
        when(ruleRepository.findByDecisionTypeAndEnabledTrueOrderByPriorityAsc("TEST"))
                .thenReturn(List.of(r));
        DecisionRequest request = request("TEST", "user-1", Map.of());
        assertThrows(IllegalArgumentException.class, () -> decisionService.evaluate(request));
    }

    @Test
    void quotedStringShouldWork() {
        Rule r = ruleEntity(1L, "TEST", 1, true, "country == \"DE\"", "DENY:X", "QUOTED");
        when(ruleRepository.findByDecisionTypeAndEnabledTrueOrderByPriorityAsc("TEST"))
                .thenReturn(List.of(r));

        DecisionResponse response = decisionService.evaluate(request("TEST", "u", Map.of("country", "DE")));
        assertEquals(DecisionResponse.Decision.DENY, response.getDecision());
    }

    private DecisionRequest request(String decisionType, String subjectId, Map<String, Object> attrs) {
        DecisionRequest r = new DecisionRequest();
        r.setDecisionType(decisionType);
        r.setSubjectId(subjectId);
        r.setAttributes(new LinkedHashMap<>(attrs));
        return r;
    }

    private static Rule ruleEntity(Long id,
                                   String decisionType,
                                   int priority,
                                   boolean enabled,
                                   String condition,
                                   String effect,
                                   String description ){
        Rule r = new Rule();
        r.setId(id);
        r.setDecisionType(decisionType);
        r.setPriority(priority);
        r.setEnabled(enabled);
        r.setCondition(condition);
        r.setEffect(effect);
        r.setDescription(description);
        return r;

    }
}
