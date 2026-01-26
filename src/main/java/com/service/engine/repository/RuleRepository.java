package com.service.engine.repository;


import com.service.engine.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findByDecisionTypeAndEnabledTrueOrderByPriorityAsc(String decisionType);
    List<Rule> findByDecisionTypeOrderByPriorityAsc(String decisionType);
}
