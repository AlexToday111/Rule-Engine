# Rule Engine / Decision Engine

Rule Engine — backend-сервис на Spring Boot для принятия решений на основе набора правил, хранящихся в базе данных.
Сервис принимает входной контекст (атрибуты) и прогоняет его через цепочку правил, отсортированных по приоритету.

Подходит для:
- eligibility-проверок
- антифрода
- скоринга
- feature-gating
- бизнес-валидаций

---

## Как работает

Каждое правило содержит:
- `condition` — условие (простые выражения и `&&`)
- `effect` — эффект при совпадении (`ALLOW`, `DENY:<reason>`, `REVIEW:<reason>`)
- `priority` — порядок выполнения (по возрастанию)
- `enabled` — включено ли правило

Поведение цепочки:
- правила выполняются по приоритету
- если правило **не совпало** — попадает в трассировку как `matched=false`
- если правило **совпало**:
  - выставляется решение согласно `effect`
  - при `DENY` выполнение **останавливается**
  - при `REVIEW` выполнение **продолжается**
- если ни одно правило не совпало — решение по умолчанию: **ALLOW**

В ответе возвращается:
- итоговое решение
- `triggeredRules` (трассировка выполнения/совпадений)
- время выполнения

---

## API

Базовый префикс: `/api/v1`

### `POST /api/v1/decisions/evaluate`

#### Request

```json
{
  "decisionType": "CREDIT_ELIGIBILITY",
  "subjectId": "user-123",
  "attributes": {
    "age": 17,
    "country": "DE",
    "balance": 1200
  }
}
```

#### Response (пример)

```json
{
  "decision": "DENY",
  "score": null,
  "executionTimeMs": 12,
  "triggeredRules": [
    {
      "ruleId": "1",
      "ruleName": "MIN_AGE",
      "matched": true,
      "reason": "UNDERAGE"
    }
  ]
}
```

---

## Условия (condition)

Поддерживается:
- сравнения: `==`, `!=`, `>`, `>=`, `<`, `<=`
- логическое И: `&&`
- строковые литералы в кавычках: `"DE"` или `'DE'`
- булевы литералы: `true`, `false`

Примеры:
- `age >= 18`
- `country == "DE"`
- `age >= 18 && country != "DE"`

---

## Эффекты (effect)

Формат:
- `ALLOW`
- `DENY:<reason>`
- `REVIEW:<reason>`

Примеры:
- `DENY:UNDERAGE`
- `REVIEW:MANUAL`

---

## Хранилище правил и миграции

Правила лежат в БД, миграции выполняются через Flyway (см. `src/main/resources/db/migration`):
- `V1__create_rules_table.sql`
- `V2__seed_rules.sql`

---

## Запуск

Требования:
- Java 17
- Maven
- PostgreSQL

Типовой запуск:
1) Подними PostgreSQL и пропиши настройки в `src/main/resources/application.yml`
2) Запусти приложение:

```bash
mvn spring-boot:run
```

---

## Тесты

```bash
mvn test
```


