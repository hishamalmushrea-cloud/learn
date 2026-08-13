#!/usr/bin/env bash
# يشغّل اختبار محلل Markdown على ملفات الموسوعة الحقيقية، بلا Gradle.
set -euo pipefail
JAVA_HOME="${JAVA_HOME:-/tmp/jvenv/lib/python3.11/site-packages/jdk4py/java-runtime}"
KOTLIN_BIN="${KOTLIN_BIN:-/tmp/kt/node_modules/kotlin-compiler/bin}"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$KOTLIN_BIN:$PATH"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${OUT:-/tmp/indolearn-md-spec.jar}"
echo "==> compiling markdown spec"
kotlinc "$ROOT/tools/MarkdownSpec.kt" -include-runtime -d "$OUT" 2>&1 \
  | grep -iv 'warning\|restricted\|deprecated\|sun\.misc\|consider reporting\|native-access' || true
echo "==> running"
exec java -jar "$OUT" "$ROOT/IndoLearn/app/src/main/assets/library"
