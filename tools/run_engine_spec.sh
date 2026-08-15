#!/usr/bin/env bash
# تشغيل اختبارات محرك التعلم بلا Gradle.
#
# لماذا: مستودعات Gradle / Google Maven محجوبة في بيئة التطوير المستخدمة،
# فتعذّر تشغيل `./gradlew test`. هذا السكربت يجمّع طبقة الـ domain النقية
# (Kotlin بلا اعتماد على Android) ويشغّل التأكيدات فعلياً.
#
# المتطلبات (تُثبَّت من قنوات متاحة، لا حاجة لـ Maven):
#   python3 -m venv /tmp/jvenv && /tmp/jvenv/bin/pip install jdk4py
#   mkdir -p /tmp/kt && cd /tmp/kt && npm install kotlin-compiler@2.2.20
#
# ملاحظة: kotlin-compiler 2.0.21 يفشل على JDK 25 بخطأ
# "IllegalArgumentException: 25.0.2" — استخدم 2.2.20 أو JDK 17.

set -euo pipefail

JAVA_HOME="${JAVA_HOME:-/tmp/jvenv/lib/python3.11/site-packages/jdk4py/java-runtime}"
KOTLIN_BIN="${KOTLIN_BIN:-/tmp/kt/node_modules/kotlin-compiler/bin}"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$KOTLIN_BIN:$PATH"

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${OUT:-/tmp/indolearn-spec.jar}"

echo "==> compiling domain + spec"
kotlinc \
  "$ROOT/IndoLearn/app/src/main/java/com/indolearn/domain/srs/SpacedRepetition.kt" \
  "$ROOT/IndoLearn/app/src/main/java/com/indolearn/domain/coach/DailyCoach.kt" \
  "$ROOT/IndoLearn/app/src/main/java/com/indolearn/domain/quiz/AnswerEvaluator.kt" \
  "$ROOT/tools/EngineSpec.kt" \
  -include-runtime -d "$OUT" 2>&1 \
  | grep -iv 'warning\|restricted\|deprecated\|sun\.misc\|consider reporting\|native-access' || true

echo "==> running"
exec java -jar "$OUT"
