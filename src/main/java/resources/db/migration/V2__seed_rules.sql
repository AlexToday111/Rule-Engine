INSERT INTO rules (decision_type, priority, enabled, condition, effect, description)
VALUES

-- Минимальный возраст
(
 'CREDIT_ELIGIBILITY',
 1,
 true,
 'age < 18',
 'DENY:UNDERAGE',
 'MIN_AGE'
),

-- Проверка страны
(
 'CREDIT_ELIGIBILITY',
 2,
 true,
 'country != DE',
 'REVIEW:FOREIGN_COUNTRY',
 'COUNTRY_CHECK'
),

-- Дефолтное разрешение
(
 'CREDIT_ELIGIBILITY',
 100,
 true,
 '',
 'ALLOW',
 'DEFAULT_ALLOW'
);