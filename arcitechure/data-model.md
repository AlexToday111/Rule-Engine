# Модель данных

## Таблица rules

| Поле | Тип | Описание |
|---|---|---|
| id | BIGSERIAL | первичный ключ |
| decision_type | VARCHAR(100) | тип решения |
| priority | INT | порядок выполнения (меньше = раньше) |
| enabled | BOOLEAN | признак активности |
| condition | TEXT | условие правила |
| effect | TEXT | эффект правила |
| description | VARCHAR(255) | человеко-читаемое имя |

Индекс: `idx_rules_decision_enabled_priority` по `(decision_type, enabled, priority)`.

## Миграции

Миграции Flyway находятся в `src/main/java/resources/db/migration`:
- `V1__create_rules_table.sql` — таблица и индекс.
- `V2__seed_rules.sql` — стартовые правила.
