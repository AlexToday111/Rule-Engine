package com.service.engine.service;

import com.service.engine.dto.DecisionRequest;
import com.service.engine.dto.DecisionResponse;
import com.service.engine.model.Rule;
import com.service.engine.repository.RuleRepository;
import org.springframework.stereotype.Service;



import java.util.ArrayList;
import java.util.List;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;


@Service
public class DecisionService {
    private final RuleRepository ruleRepository;
    private final MeterRegistry meterRegistry;

    public DecisionService(RuleRepository ruleRepository, MeterRegistry meterRegistry) {
        this.ruleRepository = ruleRepository;
        this.meterRegistry = meterRegistry;
    }

    public DecisionResponse evaluate(DecisionRequest request) {

        Timer.Sample sample = Timer.start(meterRegistry);

        long start = System.nanoTime();

        List<Rule> rules = ruleRepository.findByDecisionTypeAndEnabledTrueOrderByPriorityAsc(request.getDecisionType());

        DecisionContext ctx = new DecisionContext(
                request.getSubjectId(),
                request.getDecisionType(),
                request.getAttributes()
        );

        DecisionResponse response = new DecisionResponse();
        response.setDecision(DecisionResponse.Decision.ALLOW);
        response.setScore(null);

        List<DecisionResponse.TriggeredRule> trace = new ArrayList<>();

        for (Rule rule : rules) {
            boolean matched = matches(rule.getCondition(), ctx);

            DecisionResponse.TriggeredRule tr = new DecisionResponse.TriggeredRule();
            tr.setRuleId(String.valueOf(rule.getId()));
            tr.setRuleName(rule.getDescription());
            tr.setMatched(matched);

            if (!matched) {
                tr.setReason(null);
                trace.add(tr);
                continue;
            }

            Effect effect = parseEffect(rule.getEffect());
            tr.setReason(effect.reason);

            trace.add(tr);

            if (effect.decision != null) {
                response.setDecision(effect.decision);
                if (effect.decision == DecisionResponse.Decision.DENY) {
                    break;
                }
            }
        }
        response.setTriggeredRules(trace);

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        response.setExecutionTimeMs(elapsedMs);

        sample.stop(Timer.builder("decision_execution_time")
                .tag("type", request.getDecisionType())
                .register(meterRegistry));

        Counter.builder("decision_requests_total")
                .tag("type", request.getDecisionType())
                .tag("decision", response.getDecision().name())
                .register(meterRegistry)
                .increment();

        return response;
    }

    private boolean matches(String condtion, DecisionContext ctx) {
        if (condtion == null || condtion.isBlank()) {
            return false;
        }
        String[] parts = condtion.split("&&");
        for (String raw : parts) {
            String expr = raw.trim();
            if (expr.isEmpty()) continue;
            if (!evalSingle(expr, ctx)) return false;
        }
        return true;
    }

    private boolean evalSingle(String expr, DecisionContext ctx) {
        String op = detectOp(expr);
        if (op == null) {
            throw new IllegalArgumentException("Unsupported expression: " + expr);
        }
        String[] tokens = expr.split("\\Q" + op + "\\E", 2);
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Bad expression: " + expr);
        }
        String field = tokens[0].trim();
        String rawValue = tokens[1].trim();

        Object left = ctx.get(field);
        Object right = parseLiteral(rawValue);

        if (left instanceof Number || right instanceof Number) {
            double l = toDouble(left);
            double r = toDouble(right);
            return switch (op) {
                case "==" -> Double.compare(l, r) == 0;
                case "!=" -> Double.compare(l, r) != 0;
                case ">" -> l > r;
                case ">=" -> l >= r;
                case "<" -> l < r;
                case "<=" -> l <= r;
                default -> throw new IllegalArgumentException("Unsupported op: " + op);
            };
        }

        String ls = left == null ? null : String.valueOf(left);
        String rs = right == null ? null : String.valueOf(right);

        return switch (op) {
            case "==" -> safeEquals(ls, rs);
            case "!=" -> !safeEquals(ls, rs);
            default -> throw new IllegalArgumentException("Operator '" + op + "' is only numeric in v1: " + expr);
        };
    }

    private String detectOp(String expr) {
        if (expr.contains(">=")) return ">=";
        if (expr.contains("<=")) return "<=";
        if (expr.contains("==")) return "==";
        if (expr.contains("!=")) return "!=";
        if (expr.contains("<")) return "<";
        if (expr.contains(">")) return ">";
        return null;
    }

    private double toDouble(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) return Double.parseDouble(s);
        throw new IllegalArgumentException("Not a number + " + v);
    }

    private Object parseLiteral(String raw) {
        if ((raw.startsWith("\"") && raw.endsWith("\"")) || (raw.startsWith("'") && raw.endsWith("'"))) {
            return raw.substring(1, raw.length() - 1);
        }

        if ("true".equalsIgnoreCase(raw)) return true;
        if ("false".equalsIgnoreCase(raw)) return false;

        try {
            if (!raw.contains(".")) return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
        }

        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ignored) {
        }

        return raw;
    }

    private boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private Effect parseEffect(String effect) {
        if (effect == null || effect.isBlank()) {
            return new Effect(null, null);
        }

        String[] parts = effect.split(":", 2);
        String action = parts[0].trim().toUpperCase();
        String reason = parts.length == 2 ? parts[1].trim() : null;

        DecisionResponse.Decision decision = switch (action) {
            case "ALLOW" -> DecisionResponse.Decision.ALLOW;
            case "DENY" -> DecisionResponse.Decision.DENY;
            case "REVIEW" -> DecisionResponse.Decision.REVIEW;
            default -> throw new IllegalArgumentException("Unknown effect action: " + action);
        };

        return new Effect(decision, reason);
    }

    private record Effect(DecisionResponse.Decision decision, String reason) {
    }
}
