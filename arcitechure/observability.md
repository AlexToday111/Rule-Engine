# Наблюдаемость

## Метрики

Регистрируются через Micrometer:

- `decision_execution_time` (timer)
  - tag: `type` — `decisionType`
- `decision_requests_total` (counter)
  - tags: `type`, `decision`
- `rules_cache_hit_total` (counter)
  - tag: `decisionType`
- `rules_cache_miss_total` (counter)
  - tag: `decisionType`

## Actuator endpoints

В `application.yml` включены:
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`

Теги метрик содержат `application=rule-engine`.
