#!/usr/bin/env python3
"""Static contract check for additive Room migrations.

Not a replacement for MigrationTestHelper; it catches the common release blocker where
an Entity field is added but the CREATE TABLE migration or addMigrations registration
is missing.
"""
from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1]
entity = (root / "IndoLearn/app/src/main/java/com/indolearn/data/local/entity/QuestionAttemptEntity.kt").read_text()
module = (root / "IndoLearn/app/src/main/java/com/indolearn/di/AppModule.kt").read_text()
database = (root / "IndoLearn/app/src/main/java/com/indolearn/data/local/AppDatabase.kt").read_text()

expected = {
    "id": "INTEGER",
    "questionId": "INTEGER",
    "question": "TEXT",
    "userAnswer": "TEXT",
    "correctAnswer": "TEXT",
    "explanation": "TEXT",
    "isCorrect": "INTEGER",
    "questionType": "TEXT",
    "category": "TEXT",
    "languageCode": "TEXT",
    "attemptedAt": "INTEGER",
}
errors = []

match = re.search(r"CREATE TABLE IF NOT EXISTS question_attempts \((.*?)\)\s*\"\"\"", module, re.S)
if not match:
    errors.append("MIGRATION_5_6 does not create question_attempts")
    sql = ""
else:
    sql = match.group(1)

for name, sql_type in expected.items():
    if not re.search(rf"(?m)^\s*{re.escape(name)}\s+{sql_type}\b", sql):
        errors.append(f"migration column missing or wrong: {name} {sql_type}")
    if not re.search(rf"\bval\s+{re.escape(name)}\s*:", entity):
        errors.append(f"entity field missing: {name}")

sql_columns = set(re.findall(r"(?m)^\s*(\w+)\s+(?:INTEGER|TEXT|REAL)\b", sql))
extra = sql_columns - expected.keys()
if extra:
    errors.append(f"unexpected migration columns: {sorted(extra)}")

checks = {
    "database version is 6": "version = 6" in database,
    "entity registered": "QuestionAttemptEntity::class" in database,
    "DAO exposed": "questionAttemptDao(): QuestionAttemptDao" in database,
    "migration registered": ".addMigrations(MIGRATION_4_5, MIGRATION_5_6)" in module,
    "attempt index created": "index_question_attempts_languageCode_isCorrect_attemptedAt" in module,
    "question index created": "index_question_attempts_questionId_languageCode" in module,
}
for label, ok in checks.items():
    if not ok:
        errors.append(label)

print("Room migration contract 5→6")
if errors:
    for error in errors:
        print("  ✗", error)
    sys.exit(1)
for label in checks:
    print("  ✓", label)
print(f"  ✓ {len(expected)} entity columns match migration SQL")
