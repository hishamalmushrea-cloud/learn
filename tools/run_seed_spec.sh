#!/usr/bin/env bash
# يشغّل اختبار منطق SeedManager (بذر مرة واحدة، ذرّي، بلا سباق) بلا Gradle.
set -euo pipefail
JAVA_HOME="${JAVA_HOME:-/tmp/jvenv/lib/python3.11/site-packages/jdk4py/java-runtime}"
KOTLIN_HOME="${KOTLIN_HOME:-/tmp/kt/node_modules/kotlin-compiler}"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$KOTLIN_HOME/bin:$PATH"
CO="$KOTLIN_HOME/lib/kotlinx-coroutines-core-jvm.jar"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${OUT:-/tmp/indolearn-seed-spec.jar}"
echo "==> compiling seed spec"
kotlinc -cp "$CO" "$ROOT/tools/SeedSpec.kt" -include-runtime -d "$OUT" 2>&1 \
  | grep -iv 'warning\|restricted\|deprecated\|sun\.misc\|consider reporting\|native-access' || true
echo "==> running"
exec java -cp "$OUT:$CO" tools.SeedSpecKt
