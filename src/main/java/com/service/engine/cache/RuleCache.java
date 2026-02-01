package com.service.engine.cache;

import com.service.engine.model.Rule;
import com.service.engine.repository.RuleRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RuleCache {
    private final RuleRepository ruleRepository;
    private final MeterRegistry meterRegistry;

    private final ConcurrentHashMap<String, CachedRules> rulesCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 30_000;

    private record CachedRules(List<Rule> rules, long loadedAtMs) {}

    public RuleCache(RuleRepository ruleRepository, MeterRegistry meterRegistry) {
        this.ruleRepository = ruleRepository;
        this.meterRegistry = meterRegistry;
    }

    public List<Rule> getRules(String decisionType) {
        long now = System.currentTimeMillis();
        CachedRules cached = rulesCache.get(decisionType);

        if (cached != null && (now - cached.loadedAtMs()) < CACHE_TTL_MS) {
            Counter.builder("rules_cache_hit_total")
                    .tag("decisionType", decisionType)
                    .register(meterRegistry)
                    .increment();
            return cached.rules();
        }

        Counter.builder("rules_cache_miss_total")
                .tag("decisionType", decisionType)
                .register(meterRegistry)
                .increment();

        List<Rule> fresh = ruleRepository.findByDecisionTypeAndEnabledTrueOrderByPriorityAsc(decisionType);
        rulesCache.put(decisionType, new CachedRules(fresh, now));
        return fresh;
    }

    public void evictAll() {
        rulesCache.clear();
    }

    public void evict(String decisionType) {
        rulesCache.remove(decisionType);
    }
}